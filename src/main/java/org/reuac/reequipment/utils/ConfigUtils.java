package org.reuac.reequipment.utils;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.model.LevelEffect;
import org.reuac.reequipment.model.LevelLores;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigUtils {

	public static Sound getSound(String soundName) {
		if (soundName == null || soundName.equals("#")) {
			return null;
		}
		try {
			return Sound.valueOf(soundName.toUpperCase());
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid sound name: " + soundName);
			return null;
		}
	}

	public static Level loadLevelFromConfig(FileConfiguration config, int levelNumber) {
		String path = "Level." + levelNumber;
		int money = config.getInt(path + ".money", 0);
		int materialAmount = config.getInt(path + ".materialAmount", 0);
		int successRate = config.getInt(path + ".successRate", 0);

		Map<String, LevelEffect> levelEffects = new HashMap<>();
		ConfigurationSection effectSection = config.getConfigurationSection(path + ".effect");
		if (effectSection != null) {
			for (String type : effectSection.getKeys(false)) {
				String effectPath = path + ".effect." + type;
				// 重新添加 effectiveArea 的读取,从 ReEquipment.equipmentTypes 获取
				String effectiveArea = ReEquipment.equipmentTypes.get(type);
				double damageBonus = config.getDouble(effectPath + ".damageBonus", 0);
				double defense = config.getDouble(effectPath + ".defense", 0);
				// 将 effectiveArea 传递给 LevelEffect 的构造函数
				levelEffects.put(type, new LevelEffect(damageBonus, defense, effectiveArea));
			}
		}

		Map<String, LevelLores> levelLores = new HashMap<>();
		ConfigurationSection loresSection = config.getConfigurationSection(path + ".lores");
		if (loresSection != null) {
			for (String type : loresSection.getKeys(false)) {
				List<String> lores = config.getStringList(path + ".lores." + type).stream()
						.map(s -> ChatColor.translateAlternateColorCodes('&', s))
						.collect(Collectors.toList());
				levelLores.put(type, new LevelLores(lores));
			}
		}

		return new Level(money, materialAmount, successRate, levelEffects, levelLores);
	}
}