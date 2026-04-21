package org.reuac.reequipment.commands.subcommands;

import org.bukkit.ChatColor;
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

import java.util.*;

public class StartCommand implements SubCommand {

	private final ReEquipment plugin = ReEquipment.getInstance();
	// 把防连点 Map 放到这里
	private final Map<Player, Boolean> isProcessing = new HashMap<>();

	@Override
	public String getName() {
		return "start";
	}

	@Override
	public String getPermission() {
		return null;
	} // 默认玩家可用，无特殊权限

	@Override
	public void perform(Player player, String[] args) {
		if (isProcessing.getOrDefault(player, false)) {
			player.sendMessage(plugin.getDataManager().getPrefix() + "您操作过快了,请稍后再试。");
			return;
		}

		isProcessing.put(player, true);

		try {
			if (isProcessing.getOrDefault(player, false)) {
				player.sendMessage(plugin.getDataManager().getPrefix() + ChatColor.YELLOW + "您操作过快了,请稍后再试。");
				return;
			}

			isProcessing.put(player, true);
			DataManager data = plugin.getDataManager();

			ItemStack itemInHand = player.getInventory().getItemInMainHand();
			if (itemInHand.getType() == Material.AIR) {
				player.sendMessage(data.getMessages().get("emptyItem"));
				isProcessing.put(player, false);
				return;
			}

			EquipmentType equipmentType = ItemUtils.getEquipmentType(itemInHand);
			if (equipmentType == null) {
				player.sendMessage(data.getMessages().get("noType"));
				isProcessing.put(player, false);
				return;
			}

			int currentLevel = ItemUtils.getTemperingLevel(itemInHand);
			int nextLevelIndex = currentLevel + 1;

			if (nextLevelIndex > data.getLevels().lastKey()) {
				player.sendMessage(data.getMessages().get("cannotTempering"));
				isProcessing.put(player, false);
				return;
			}

			Level nextLevel = data.getLevels().get(nextLevelIndex);
			if (nextLevel == null) {
				player.sendMessage(data.getMessages().get("cannotTempering"));
				isProcessing.put(player, false);
				return;
			}

			if (plugin.getEconomy().getBalance(player) < nextLevel.getMoney()) {
				player.sendMessage(data.getMessages().get("noMoney").replace("{Money}", String.valueOf(nextLevel.getMoney())));
				isProcessing.put(player, false);
				return;
			}

			int temperingToolAmount = ItemUtils.countItemsInInventory(player, data.getTemperingTool());
			if (temperingToolAmount < nextLevel.getMaterialAmount()) {
				player.sendMessage(data.getMessages().get("noMaterial"));
				isProcessing.put(player, false);
				return;
			}

			ItemUtils.removeItemsFromInventory(player, data.getTemperingTool(), nextLevel.getMaterialAmount());

			// ======== PRD 概率计算 ========
			int failCount = ItemUtils.getFailCountForLevel(itemInHand, nextLevelIndex);
			int actualSuccessRate = nextLevel.getBaseRate() + (failCount * nextLevel.getStepRate());
			actualSuccessRate = Math.min(actualSuccessRate, nextLevel.getMaxCap());

			Random random = new Random();
			int roll = random.nextInt(100);

			if (roll < actualSuccessRate) {
				// ======== 【强化成功】 ========
				ItemUtils.setTemperingLevel(itemInHand, nextLevelIndex, equipmentType);
				ItemUtils.setFailCountForLevel(itemInHand, nextLevelIndex, 0);

				player.sendMessage(data.getPrefix() + ChatColor.GREEN + " 强化成功！(本次成功率: " + actualSuccessRate + "%)");
				plugin.playSound(player, "success");
				FireworkMaker.spawnSuccessFirework(player);

			} else {
				// ======== 【强化失败】 ========
				ItemUtils.setFailCountForLevel(itemInHand, nextLevelIndex, failCount + 1);

				int dropToLevel = currentLevel - 1;

				// 安全里程碑判定
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

				player.sendMessage(data.getMessages().get("drop").replace("掉级", "掉级至 +" + dropToLevel));
				plugin.playSound(player, "downgrade");

				int nextTimeRate = Math.min(nextLevel.getBaseRate() + ((failCount + 1) * nextLevel.getStepRate()), nextLevel.getMaxCap());
				player.sendMessage(ChatColor.GRAY + "  [系统] 武器记住了你的汗水... 下次冲击 +" + nextLevelIndex + " 成功率提升至 " + nextTimeRate + "%");
			}

		} finally {
			isProcessing.put(player, false);
		}
	}
}