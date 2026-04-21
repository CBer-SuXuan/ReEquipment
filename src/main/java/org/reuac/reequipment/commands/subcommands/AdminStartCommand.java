package org.reuac.reequipment.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.commands.SubCommand;
import org.reuac.reequipment.model.EquipmentType;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.utils.FireworkMaker;
import org.reuac.reequipment.utils.ItemUtils;

public class AdminStartCommand implements SubCommand {

	private final ReEquipment plugin = ReEquipment.getInstance();

	@Override
	public String getName() {
		return "adminstart";
	}

	@Override
	public String getPermission() {
		return "reequipment.adminstart";
	}

	@Override
	public void perform(Player player, String[] args) {

		ItemStack itemInHand = player.getInventory().getItemInMainHand();
		if (itemInHand == null || itemInHand.getType() == Material.AIR) {
			player.sendMessage(plugin.getDataManager().getMessages().get("emptyItem"));
			return;
		}

		EquipmentType equipmentType = ItemUtils.getEquipmentType(itemInHand);
		if (equipmentType == null) {
			player.sendMessage(plugin.getDataManager().getMessages().get("noType"));
			return;
		}

		int currentLevel = ItemUtils.getTemperingLevel(itemInHand);

		int nextLevelIndex = currentLevel + 1;

		if (nextLevelIndex > plugin.getDataManager().getLevels().lastKey()) {
			player.sendMessage(plugin.getDataManager().getMessages().get("cannotTempering"));
			return;
		}

		Level nextLevel = plugin.getDataManager().getLevels().get(nextLevelIndex);

		if (nextLevel == null) {
			player.sendMessage(plugin.getDataManager().getMessages().get("cannotTempering"));
			return;
		}
		ItemUtils.setTemperingLevel(itemInHand, nextLevelIndex, equipmentType);
		player.sendMessage(plugin.getDataManager().getPrefix() + ChatColor.GREEN + "管理员强化成功!");
		plugin.playSound(player, "success");
		FireworkMaker.spawnSuccessFirework(player);
	}
}