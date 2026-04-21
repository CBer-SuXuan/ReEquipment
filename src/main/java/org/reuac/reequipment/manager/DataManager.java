package org.reuac.reequipment.manager;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.model.LevelEffect;
import org.reuac.reequipment.model.LevelLores;
import org.reuac.reequipment.utils.ConfigUtils;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class DataManager {

	private String prefix;
	private Map<String, Sound> sounds = new HashMap<>();
	private Map<String, String> messages = new HashMap<>();
	private Map<String, String> itemNames = new HashMap<>();
	private Map<String, String> equipmentTypes = new HashMap<>();
	private Map<String, List<String>> equipmentTypeMaterials = new HashMap<>();
	private TreeMap<Integer, Level> levels = new TreeMap<>();
	private List<Integer> safeMilestones = new ArrayList<>();
	private ItemStack temperingTool;
	private boolean broadcastEnabled;
	private int broadcastAllowLevel;
	private List<String> broadcastMessages;
	private List<String> bottomLores;

	public void load(ConfigManager configManager, ReEquipment plugin) {
		var config = configManager.getConfig();
		var typeConfig = configManager.getTypeConfig();
		var prdConfig = configManager.getPrdConfig();
		var loreConfig = configManager.getLoreConfig();
		var effectConfig = configManager.getEffectConfig();

		// 1. 加载基础设置 (config.yml)
		prefix = ChatColor.translateAlternateColorCodes('&', config.getString("Prefix", "&7[&d淬炼&7] "));

		sounds.clear();
		sounds.put("success", ConfigUtils.getSound(config.getString("Sound.success")));
		sounds.put("downgrade", ConfigUtils.getSound(config.getString("Sound.downgrade")));

		bottomLores = config.getStringList("bottomLores");

		messages.clear();
		ConfigurationSection msgSec = config.getConfigurationSection("Messages");
		if (msgSec != null) {
			for (String key : msgSec.getKeys(false)) {
				messages.put(key, prefix + ChatColor.translateAlternateColorCodes('&', msgSec.getString(key)));
			}
		}

		broadcastEnabled = config.getBoolean("BroadCast.enable", true);
		broadcastAllowLevel = config.getInt("BroadCast.allowLevel", 0);
		broadcastMessages = config.getStringList("BroadCast.messages").stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());

		itemNames.clear();
		ConfigurationSection itemNamesSec = config.getConfigurationSection("ItemName");
		if (itemNamesSec != null) {
			for (String material : itemNamesSec.getKeys(false)) {
				itemNames.put(material, ChatColor.translateAlternateColorCodes('&', itemNamesSec.getString(material)));
			}
		}

		temperingTool = ConfigUtils.loadItemStackFromConfig(config, "Article.temperingTools", plugin);

		int globalMoney = config.getInt("Cost.Money", 100);
		int globalMaterialAmount = config.getInt("Cost.MaterialAmount", 1);

		// 2. 加载装备类型 (type.yml)
		equipmentTypes.clear();
		equipmentTypeMaterials.clear();
		ConfigurationSection typeSec = typeConfig.getConfigurationSection("Type");
		if (typeSec != null) {
			for (String typeName : typeSec.getKeys(false)) {
				String effectiveArea = typeSec.getString(typeName + ".effectiveArea", "MainHand");
				List<String> materials = typeSec.getStringList(typeName + ".types");
				equipmentTypes.put(typeName, effectiveArea);
				equipmentTypeMaterials.put(typeName, materials);
			}
		}

		// 3. 加载里程碑 (prd.yml)
		safeMilestones = prdConfig.getIntegerList("SafeMilestones");

		// 4. 缝合加载 Level 数据 (lore.yml + effect.yml + prd.yml)
		levels.clear();
		ConfigurationSection loreSec = loreConfig.getConfigurationSection("Lore");
		if (loreSec != null) {
			for (String levelStr : loreSec.getKeys(false)) {
				int lvl;
				try {
					lvl = Integer.parseInt(levelStr);
				} catch (NumberFormatException e) {
					continue;
				}

				// 读 Lore
				Map<String, LevelLores> loresMap = new HashMap<>();
				ConfigurationSection typeLoreSec = loreSec.getConfigurationSection(levelStr);
				if (typeLoreSec != null) {
					for (String type : typeLoreSec.getKeys(false)) {
						List<String> lores = typeLoreSec.getStringList(type).stream()
								.map(s -> ChatColor.translateAlternateColorCodes('&', s))
								.collect(Collectors.toList());
						loresMap.put(type, new LevelLores(lores));
					}
				}

				// 读 Effect
				Map<String, LevelEffect> effectMap = new HashMap<>();
				ConfigurationSection typeEffectSec = effectConfig.getConfigurationSection("Effects." + lvl);
				if (typeEffectSec != null) {
					for (String type : typeEffectSec.getKeys(false)) {
						double dmg = typeEffectSec.getDouble(type + ".damageBonus", 0);
						double def = typeEffectSec.getDouble(type + ".defense", 0);
						String area = equipmentTypes.getOrDefault(type, "MainHand");
						effectMap.put(type, new LevelEffect(dmg, def, area));
					}
				}

				// 读 PRD
				int baseRate = prdConfig.getInt("Levels." + lvl + ".baseRate", 0);
				int stepRate = prdConfig.getInt("Levels." + lvl + ".stepRate", 0);
				int maxCap = prdConfig.getInt("Levels." + lvl + ".maxCap", 100);

				// 缝合为 Level 对象
				levels.put(lvl, new Level(globalMoney, globalMaterialAmount, baseRate, stepRate, maxCap, effectMap, loresMap));
			}
		}
	}
}