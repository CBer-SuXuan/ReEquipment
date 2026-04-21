package org.reuac.reequipment.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigUtils {

	public static Sound getSound(String soundName) {
		if (soundName == null || soundName.equals("#")) return null;
		try {
			return Sound.valueOf(soundName.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static ItemStack loadItemStackFromConfig(FileConfiguration config, String path, Plugin plugin) {
		if (config.getBoolean(path + ".setNormal", false)) {
			try {
				return new ItemStack(Material.valueOf(config.getString(path + ".materialType", "STONE")));
			} catch (Exception e) {
				return new ItemStack(Material.STONE);
			}
		}

		String displayName = ChatColor.translateAlternateColorCodes('&', config.getString(path + ".displayName", ""));
		String materialType = config.getString(path + ".materialType", "STONE");
		List<String> lores = config.getStringList(path + ".lores").stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
		boolean glow = config.getBoolean(path + ".glow", false);

		ItemStack itemStack;
		try {
			itemStack = new ItemStack(Material.valueOf(materialType));
		} catch (IllegalArgumentException e) {
			plugin.getLogger().warning("未知的物品类型: " + materialType);
			itemStack = new ItemStack(Material.STONE);
		}

		ItemMeta meta = itemStack.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(displayName);
			meta.setLore(lores);
			if (glow) {
				meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			itemStack.setItemMeta(meta);
		}
		return itemStack;
	}
}