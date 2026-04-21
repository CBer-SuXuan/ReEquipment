package org.reuac.reequipment;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.reuac.reequipment.commands.ReclCommand;
import org.reuac.reequipment.listeners.EntityDamageListener;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.utils.ConfigUtils;
import org.reuac.reequipment.utils.ItemUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ReEquipment extends JavaPlugin implements Listener {

    public static FileConfiguration config;
    public static String prefix;
    public static Map<String, Sound> sounds = new HashMap<>();
    public static Map<String, String> messages = new HashMap<>();
    public static boolean broadcastEnabled;
    public static List<String> broadcastMessages;
    public static ItemStack temperingTool;
    public static ItemStack luckyTool;
    public static int luckValue;
    public static Map<String, String> equipmentTypes = new HashMap<>();
    public static Map<String, List<String>> equipmentTypeMaterials = new HashMap<>();
    public static TreeMap<Integer, Level> levels = new TreeMap<>();
    public static Map<String, String> itemNames = new HashMap<>();
    public static int broadcastAllowLevel;

    public static List<String> bottomLores = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfiguration();
        getCommand("recl").setExecutor(new ReclCommand(this));
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getLogger().info("reEquipment 启动!");
    }

    @Override
    public void onDisable() {
        getLogger().info("reEquipment 关闭!");
    }

    public void reloadConfiguration() {
        reloadConfig();
        config = getConfig();

        prefix = ChatColor.translateAlternateColorCodes('&', config.getString("Prefix", "&7[&d淬炼&7] "));

        sounds.clear();
        sounds.put("downgrade", ConfigUtils.getSound(config.getString("Sound.downgrade")));
        sounds.put("defeat", ConfigUtils.getSound(config.getString("Sound.defeat")));

        bottomLores.clear();
        bottomLores = config.getStringList("bottomLores");

        messages.clear();
        messages.put("openTempering", prefix + ChatColor.translateAlternateColorCodes('&', config.getString("Messages.openTempering")));
        messages.put("startTempering", prefix + ChatColor.translateAlternateColorCodes('&', config.getString("Messages.startTempering")));
        messages.put("cannotTempering", prefix + ChatColor.translateAlternateColorCodes('&', config.getString("Messages.cannotTempering")));
        messages.put("emptyItem", prefix + ChatColor.translateAlternateColorCodes('&', config.getString("Messages.emptyItem")));
        messages.put("noType", prefix + ChatColor.translateAlternateColorCodes('&', config.getString("Messages.noType")));
        messages.put("noMoney", prefix + ChatColor.translateAlternateColorCodes('&', config.getString("Messages.noMoney")));
        messages.put("noMaterial", prefix + ChatColor.translateAlternateColorCodes('&', config.getString("Messages.noMaterial")));
        messages.put("success", prefix + ChatColor.translateAlternateColorCodes('&', config.getString("Messages.success")));
        messages.put("drop", prefix + ChatColor.translateAlternateColorCodes('&', config.getString("Messages.drop")));
        messages.put("defeat", prefix + ChatColor.translateAlternateColorCodes('&', config.getString("Messages.defeat")));

        broadcastEnabled = config.getBoolean("BroadCast.enable", true);
        broadcastMessages = config.getStringList("BroadCast.messages").stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());

        broadcastAllowLevel = config.getInt("BroadCast.allowLevel", 0);

        if (config.getBoolean("Article.temperingTools.setNormal")){
            String materialType = config.getString("Article.temperingTools.materialType");

            try {
                temperingTool = new ItemStack(Material.valueOf(materialType));
            } catch (IllegalArgumentException e) {
                getLogger().warning("未知的物品类型: " + materialType);
                temperingTool = new ItemStack(Material.STONE);
            }
        }else {
            temperingTool = loadItemStackFromConfig(config, "Article.temperingTools");
        }

        if (config.getBoolean("Article.LuckyTools.setNormal")){
            String materialType = config.getString("Article.LuckyTools.materialType");

            try {
                luckyTool = new ItemStack(Material.valueOf(materialType));
            } catch (IllegalArgumentException e) {
                getLogger().warning("未知的物品类型: " + materialType);
                luckyTool = new ItemStack(Material.STONE);
            }
        }else {
            luckyTool = loadItemStackFromConfig(config, "Article.LuckyTools");
        }

        luckValue = config.getInt("Article.LuckyTools.luckValue", 0);

        equipmentTypes.clear();
        equipmentTypeMaterials.clear();
        ConfigurationSection typeSection = config.getConfigurationSection("Type");
        if (typeSection != null) {
            for (String typeName : typeSection.getKeys(false)) {
                String effectiveArea = typeSection.getString(typeName + ".effectiveArea", "MainHand");
                List<String> materials = typeSection.getStringList(typeName + ".types");
                equipmentTypes.put(typeName, effectiveArea);
                equipmentTypeMaterials.put(typeName, materials);
            }
        }

        levels.clear();
        for (String levelKey : config.getConfigurationSection("Level").getKeys(false)) {
            int levelNumber;
            try {
                levelNumber = Integer.parseInt(levelKey);
            } catch (NumberFormatException e) {
                getLogger().warning("无效的等级: " + levelKey);
                continue;
            }
            levels.put(levelNumber, ConfigUtils.loadLevelFromConfig(config, levelNumber));
        }

        // 加载 ItemName 配置
        itemNames.clear();
        ConfigurationSection itemNameSection = config.getConfigurationSection("ItemName");
        if (itemNameSection != null) {
            for (String materialName : itemNameSection.getKeys(false)) {
                itemNames.put(materialName, ChatColor.translateAlternateColorCodes('&', itemNameSection.getString(materialName)));
            }
        }
    }

    public void playSound(Player player, String soundKey) {
        Sound sound = sounds.get(soundKey);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }


    private ItemStack loadItemStackFromConfig(FileConfiguration config, String path) {
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
            getLogger().warning("未知的物品类型: " + materialType);
            itemStack = new ItemStack(Material.STONE);
        }

        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lores);

        if (glow) {
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }
}