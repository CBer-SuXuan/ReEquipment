package org.reuac.reequipment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.commands.subcommands.AdminStartCommand;
import org.reuac.reequipment.commands.subcommands.GiveCommand;
import org.reuac.reequipment.commands.subcommands.StartCommand;

import java.util.ArrayList;
import java.util.List;

public class ReclCommand implements CommandExecutor {

	private final List<SubCommand> subCommands = new ArrayList<>();
	private final ReEquipment plugin;

	public ReclCommand(ReEquipment plugin) {
		this.plugin = plugin;
		// 在这里注册所有的子命令
		subCommands.add(new StartCommand());
		subCommands.add(new AdminStartCommand());
		subCommands.add(new GiveCommand());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "仅允许玩家使用该指令.");
			return true;
		}

		Player player = (Player) sender;

		if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
			sendHelpMessage(player);
			return true;
		}

		// reload 命令比较简单，直接在这里处理即可，无需单独成类
		if (args[0].equalsIgnoreCase("reload")) {
			if (player.hasPermission("reequipment.reload")) {
				plugin.reloadPlugin();
				player.sendMessage(ChatColor.GREEN + "配置重载成功!");
			} else {
				player.sendMessage(ChatColor.RED + "你没有权限使用该指令.");
			}
			return true;
		}

		// 遍历匹配子命令
		for (SubCommand subCommand : subCommands) {
			if (args[0].equalsIgnoreCase(subCommand.getName())) {
				if (subCommand.getPermission() != null && !player.hasPermission(subCommand.getPermission())) {
					player.sendMessage(ChatColor.RED + "你没有权限使用该指令.");
					return true;
				}
				subCommand.perform(player, args);
				return true;
			}
		}

		// 如果输入的子命令不存在
		sendHelpMessage(player);
		return true;
	}

	private void sendHelpMessage(Player player) {
		player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
		player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "ReEquipment " + ChatColor.GRAY + "- " + ChatColor.GREEN + "装备淬炼插件");
		player.sendMessage(ChatColor.YELLOW + "/recl start " + ChatColor.GRAY + "- " + ChatColor.GREEN + "开始淬炼");
		player.sendMessage(ChatColor.YELLOW + "/recl adminstart " + ChatColor.GRAY + "- " + ChatColor.GREEN + "管理员强制淬炼");
		player.sendMessage(ChatColor.YELLOW + "/recl reload " + ChatColor.GRAY + "- " + ChatColor.GREEN + "重载配置");
		player.sendMessage(ChatColor.YELLOW + "/recl give t [数量] " + ChatColor.GRAY + "- " + ChatColor.GREEN + "获取淬炼石");
		player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
	}
}