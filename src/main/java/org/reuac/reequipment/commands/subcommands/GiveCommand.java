package org.reuac.reequipment.commands.subcommands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.commands.SubCommand;
import org.reuac.reequipment.utils.MessageUtils;

public class GiveCommand implements SubCommand {

	private final ReEquipment plugin = ReEquipment.getInstance();

	@Override
	public String getName() {
		return "give";
	}

	@Override
	public String getPermission() {
		return "reequipment.give";
	}

	@Override
	public void perform(Player player, String[] args) {
		if (args.length < 2 || !args[1].equalsIgnoreCase("t")) {
			MessageUtils.sendMessage(player, "give-usage");
			return;
		}

		int amount = 1;
		if (args.length >= 3) {
			try {
				amount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				MessageUtils.sendMessage(player, "invalid-number");
				return;
			}
		}

		ItemStack itemToGive = plugin.getDataManager().getTemperingTool().clone();
		itemToGive.setAmount(amount);
		player.getInventory().addItem(itemToGive);
	}
}