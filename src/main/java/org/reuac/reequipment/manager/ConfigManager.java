package org.reuac.reequipment.manager;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.reuac.reequipment.ReEquipment;

import java.io.File;

@Getter
public class ConfigManager {
	private final ReEquipment plugin;
	private FileConfiguration config;
	private FileConfiguration prdConfig;
	private FileConfiguration loreConfig;
	private FileConfiguration effectConfig;
	private FileConfiguration langConfig;

	public ConfigManager(ReEquipment plugin) {
		this.plugin = plugin;
		reload();
	}

	public void reload() {
		// 保存默认配置（如果不存在）
		plugin.saveDefaultConfig();
		saveResourceIfNotExists("prd.yml");
		saveResourceIfNotExists("lore.yml");
		saveResourceIfNotExists("effect.yml");
		saveResourceIfNotExists("lang.yml");

		// 重新加载所有文件
		plugin.reloadConfig();
		config = plugin.getConfig();
		prdConfig = loadConfig("prd.yml");
		loreConfig = loadConfig("lore.yml");
		effectConfig = loadConfig("effect.yml");
		langConfig = loadConfig("lang.yml");
	}

	private void saveResourceIfNotExists(String fileName) {
		File file = new File(plugin.getDataFolder(), fileName);
		if (!file.exists()) {
			plugin.saveResource(fileName, false);
		}
	}

	private FileConfiguration loadConfig(String fileName) {
		return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), fileName));
	}
}