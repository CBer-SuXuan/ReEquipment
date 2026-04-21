package org.reuac.reequipment.commands.subcommands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.commands.SubCommand;
import org.reuac.reequipment.model.EquipmentType;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.utils.FireworkMaker;
import org.reuac.reequipment.utils.ItemUtils;
import org.reuac.reequipment.utils.MessageUtils;

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

		if (nextLevelIndex > plugin.getDataManager().getLevels().lastKey()) {
			MessageUtils.sendMessage(player, "cannot-tempering");
			return;
		}

		Level nextLevel = plugin.getDataManager().getLevels().get(nextLevelIndex);

		if (nextLevel == null) {
			MessageUtils.sendMessage(player, "cannot-tempering");
			return;
		}
		ItemUtils.setTemperingLevel(itemInHand, nextLevelIndex, equipmentType);
		MessageUtils.sendMessage(player, "admin-success");
		plugin.playSound(player, "success");
		FireworkMaker.spawnSuccessFirework(player);
	}
}