package org.reuac.reequipment.commands.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.commands.SubCommand;

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
			player.sendMessage(ChatColor.RED + "使用方法: /recl give t [数量]");
			return;
		}

		int amount = 1;
		if (args.length >= 3) {
			try {
				amount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED + "此处仅允许输入数字.");
				return;
			}
		}

		ItemStack itemToGive = plugin.getDataManager().getTemperingTool().clone();
		itemToGive.setAmount(amount);
		player.getInventory().addItem(itemToGive);
	}
}