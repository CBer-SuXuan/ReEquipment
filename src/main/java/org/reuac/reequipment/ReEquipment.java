package org.reuac.reequipment;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.reuac.reequipment.commands.ReclCommand;
import org.reuac.reequipment.listeners.EntityDamageListener;
import org.reuac.reequipment.listeners.LoreUpdateListener;
import org.reuac.reequipment.manager.ConfigManager;
import org.reuac.reequipment.manager.DataManager;

public class ReEquipment extends JavaPlugin {

	@Getter
	private static ReEquipment instance;
	@Getter
	private ConfigManager configManager;
	@Getter
	private DataManager dataManager;
	@Getter
	private Economy economy = null;

	@Override
	public void onEnable() {
		instance = this;

		// 初始化管理器
		configManager = new ConfigManager(this);
		dataManager = new DataManager();
		dataManager.load(configManager, this);

		// 挂载 Vault
		if (!setupEconomy()) {
			getLogger().severe("未找到 Vault 经济插件! 插件将被禁用.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		getCommand("recl").setExecutor(new ReclCommand(this));
		getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
		getServer().getPluginManager().registerEvents(new LoreUpdateListener(), this);
		getLogger().info("ReEquipment插件启动!");
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}

	@Override
	public void onDisable() {
		getLogger().info("ReEquipment插件关闭!");
	}

	public void reloadPlugin() {
		reloadConfig();
		configManager.reload();
		dataManager.load(configManager, this);
	}

	public void playSound(Player player, String soundKey) {
		Sound sound = dataManager.getSounds().get(soundKey);
		if (sound != null) {
			player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
		}
	}
}