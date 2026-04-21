package org.reuac.reequipment.commands;

import org.bukkit.entity.Player;

public interface SubCommand {
	/**
	 * 获取子命令名称 (如 "start")
	 */
	String getName();

	/**
	 * 获取权限节点 (如 "reequipment.adminstart")
	 */
	String getPermission();

	/**
	 * 执行具体逻辑
	 */
	void perform(Player player, String[] args);
}