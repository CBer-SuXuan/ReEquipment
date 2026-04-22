package org.reuac.reequipment.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.reuac.reequipment.utils.ItemUtils;

public class LoreUpdateListener implements Listener {

	// 1. 当玩家在快捷栏切换手持物品时，自动修复
	@EventHandler
	public void onItemHeld(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItem(event.getNewSlot());
		if (item != null) {
			ItemUtils.fixLoreOrder(item);
		}
	}

	// 2. 当玩家关闭背包、铁砧、附魔台时，扫描并修复身上所有的强化装备
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player player) {
			for (ItemStack item : player.getInventory().getContents()) {
				if (item != null) {
					ItemUtils.fixLoreOrder(item);
				}
			}
		}
	}
}