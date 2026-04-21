package org.reuac.reequipment.commands.subcommands;

// 导入占位符支持

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.commands.SubCommand;
import org.reuac.reequipment.manager.DataManager;
import org.reuac.reequipment.model.EquipmentType;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.utils.FireworkMaker;
import org.reuac.reequipment.utils.ItemUtils;
import org.reuac.reequipment.utils.MessageUtils; // 导入我们刚写的工具

import java.util.*;

public class StartCommand implements SubCommand {

	private final ReEquipment plugin = ReEquipment.getInstance();
	private final Map<Player, Boolean> isProcessing = new HashMap<>();

	@Override
	public String getName() {
		return "start";
	}

	@Override
	public String getPermission() {
		return null;
	}

	@Override
	public void perform(Player player, String[] args) {
		if (isProcessing.getOrDefault(player, false)) {
			MessageUtils.sendMessage(player, "too-fast");
			return;
		}

		isProcessing.put(player, true);

		try {
			DataManager data = plugin.getDataManager();
			ItemStack itemInHand = player.getInventory().getItemInMainHand();

			if (itemInHand.getType() == Material.AIR) {
				MessageUtils.sendMessage(player, "empty-item");
				return;
			}

			EquipmentType equipmentType = ItemUtils.getEquipmentType(itemInHand);
			if (equipmentType == null) {
				MessageUtils.sendMessage(player, "no-type");
				return;
			}

			int currentLevel = ItemUtils.getTemperingLevel(itemInHand);
			int nextLevelIndex = currentLevel + 1;

			if (nextLevelIndex > data.getLevels().lastKey()) {
				MessageUtils.sendMessage(player, "cannot-tempering");
				return;
			}

			Level nextLevel = data.getLevels().get(nextLevelIndex);
			if (nextLevel == null) {
				MessageUtils.sendMessage(player, "cannot-tempering");
				return;
			}

			if (plugin.getEconomy().getBalance(player) < nextLevel.getMoney()) {
				// 动态替换金额占位符 <money>
				MessageUtils.sendMessage(player, "no-money",
						Placeholder.unparsed("money", String.valueOf(nextLevel.getMoney())));
				return;
			}

			int temperingToolAmount = ItemUtils.countItemsInInventory(player, data.getTemperingTool());
			if (temperingToolAmount < nextLevel.getMaterialAmount()) {
				MessageUtils.sendMessage(player, "no-material");
				return;
			}

			ItemUtils.removeItemsFromInventory(player, data.getTemperingTool(), nextLevel.getMaterialAmount());

			int failCount = ItemUtils.getFailCountForLevel(itemInHand, nextLevelIndex);
			int actualSuccessRate = nextLevel.getBaseRate() + (failCount * nextLevel.getStepRate());
			actualSuccessRate = Math.min(actualSuccessRate, nextLevel.getMaxCap());

			Random random = new Random();
			int roll = random.nextInt(100);

			if (roll < actualSuccessRate) {
				// ======== 【强化成功】 ========
				ItemUtils.setTemperingLevel(itemInHand, nextLevelIndex, equipmentType);
				ItemUtils.setFailCountForLevel(itemInHand, nextLevelIndex, 0);

				MessageUtils.sendMessage(player, "success",
						Placeholder.unparsed("rate", String.valueOf(actualSuccessRate)));
				plugin.playSound(player, "success");
				FireworkMaker.spawnSuccessFirework(player);

				if (data.isBroadcastEnabled() && nextLevelIndex >= data.getBroadcastAllowLevel()) {
					MiniMessage mm = MiniMessage.miniMessage();

					Component itemNameComp = itemInHand.getItemMeta().hasDisplayName() ?
							itemInHand.getItemMeta().displayName() :
							Component.translatable(itemInHand.getType().translationKey());

					Component hoverItem = itemNameComp.hoverEvent(itemInHand.asHoverEvent());

					for (String msgTemplate : data.getBroadcastMessages()) {
						Component broadcast = mm.deserialize(msgTemplate,
								Placeholder.unparsed("player", player.getName()),
								Placeholder.component("item", hoverItem),   // 这里直接传入刚才做好的带悬浮的物品 Component
								Placeholder.unparsed("level", String.valueOf(nextLevelIndex))
						);
						// 向全服发送
						Bukkit.getServer().sendMessage(broadcast);
					}
				}

			} else {
				// ======== 【强化失败】 ========
				ItemUtils.setFailCountForLevel(itemInHand, nextLevelIndex, failCount + 1);
				int dropToLevel = currentLevel - 1;

				List<Integer> safeMilestones = data.getSafeMilestones();
				if (safeMilestones != null && !safeMilestones.isEmpty()) {
					safeMilestones.sort(Collections.reverseOrder());
					for (int milestone : safeMilestones) {
						if (currentLevel >= milestone && dropToLevel < milestone) {
							dropToLevel = milestone;
							break;
						}
					}
				}

				dropToLevel = Math.max(0, dropToLevel);
				ItemUtils.setTemperingLevel(itemInHand, dropToLevel, equipmentType);

				// 替换掉级提示 <drop_level>
				MessageUtils.sendMessage(player, "drop",
						Placeholder.unparsed("drop_level", "掉级至 +" + dropToLevel));
				plugin.playSound(player, "downgrade");

				int nextTimeRate = Math.min(nextLevel.getBaseRate() + ((failCount + 1) * nextLevel.getStepRate()), nextLevel.getMaxCap());
				// 多参数替换
				MessageUtils.sendMessage(player, "prd-compensation",
						Placeholder.unparsed("next_level", String.valueOf(nextLevelIndex)),
						Placeholder.unparsed("rate", String.valueOf(nextTimeRate)));
			}

		} finally {
			isProcessing.put(player, false);
		}
	}
}