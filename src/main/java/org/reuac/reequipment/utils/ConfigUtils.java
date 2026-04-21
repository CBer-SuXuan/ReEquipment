package org.reuac.reequipment.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

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

		String materialType = config.getString(path + ".materialType", "STONE");
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
			MiniMessage mm = MiniMessage.miniMessage();

			// 解析 DisplayName, 并强制取消原版默认的斜体 (false)
			String displayNameRaw = config.getString(path + ".displayName", "");
			if (!displayNameRaw.isEmpty()) {
				Component displayName = mm.deserialize(displayNameRaw).decoration(TextDecoration.ITALIC, false);
				meta.displayName(displayName); // 使用 Paper API 的 Component 方法
			}

			// 解析 Lores, 同样取消默认斜体
			List<Component> lores = new ArrayList<>();
			for (String line : config.getStringList(path + ".lores")) {
				lores.add(mm.deserialize(line).decoration(TextDecoration.ITALIC, false));
			}
			if (!lores.isEmpty()) {
				meta.lore(lores); // 使用 Paper API 的 Component 方法
			}

			if (glow) {
				meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			itemStack.setItemMeta(meta);
		}
		return itemStack;
	}
}