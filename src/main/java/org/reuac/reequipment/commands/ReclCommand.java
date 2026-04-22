package org.reuac.reequipment.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.commands.subcommands.AdminStartCommand;
import org.reuac.reequipment.commands.subcommands.GiveCommand;
import org.reuac.reequipment.commands.subcommands.StartCommand;
import org.reuac.reequipment.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class ReclCommand implements CommandExecutor, TabCompleter {

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
		MessageUtils.sendRawMessage(player, "help-header");
		MessageUtils.sendRawMessage(player, "help-title");
		MessageUtils.sendRawMessage(player, "help-start");
		MessageUtils.sendRawMessage(player, "help-admin");
		MessageUtils.sendRawMessage(player, "help-reload");
		MessageUtils.sendRawMessage(player, "help-give");
		MessageUtils.sendRawMessage(player, "help-footer");
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (args.length == 1) {
			List<String> completions = new ArrayList<>();
			for (SubCommand subCommand : subCommands) {
				String permission = subCommand.getPermission();
				if (permission == null) {
					completions.add(subCommand.getName());
					continue;
				}
				if (sender.hasPermission(permission)) {
					completions.add(subCommand.getName());
				}
			}
			if (sender.hasPermission("reequipment.reload")) {
				completions.add("reload");
			}
			return completions;
		} else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
			return List.of("t");
		} else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
			return List.of("[数量]");
		}
		return List.of();
	}
}