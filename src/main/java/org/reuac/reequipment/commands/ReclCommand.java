package org.reuac.reequipment.commands;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.model.EquipmentType;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.model.NumberFormat;
import org.reuac.reequipment.utils.FireworkMaker;
import org.reuac.reequipment.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReclCommand implements CommandExecutor {

    private static final Map<Enchantment, String> chineseEnchantmentNames = new java.util.HashMap<>();

    // Helper function to safely put Enchantment into map with version compatibility check
    private static void putEnchantmentIfPresent(String enchantmentFieldName, String chineseName) {
        try {
            Field field = Enchantment.class.getField(enchantmentFieldName);
            Enchantment enchantment = (Enchantment) field.get(null); // Static field, so instance is null
            if (enchantment != null) {
                chineseEnchantmentNames.put(enchantment, chineseName);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Field not found in this version, or access issue - ignore and continue
            // This is expected for version compatibility
        }
    }

    static {
        putEnchantmentIfPresent("ARROW_DAMAGE", "力量");
        putEnchantmentIfPresent("ARROW_FIRE", "火焰附加");
        putEnchantmentIfPresent("ARROW_INFINITE", "无限");
        putEnchantmentIfPresent("ARROW_KNOCKBACK", "冲击");
        putEnchantmentIfPresent("BINDING_CURSE", "诅咒绑定");
        putEnchantmentIfPresent("CHANNELING", "引雷");
        putEnchantmentIfPresent("DAMAGE_ALL", "锋利");
        putEnchantmentIfPresent("DAMAGE_ARTHROPODS", "节肢杀手");
        putEnchantmentIfPresent("DAMAGE_UNDEAD", "亡灵杀手");
        putEnchantmentIfPresent("DEPTH_STRIDER", "深海疾行");
        putEnchantmentIfPresent("DIG_SPEED", "效率");
        putEnchantmentIfPresent("DURABILITY", "耐久");
        putEnchantmentIfPresent("FIRE_ASPECT", "火焰附加");
        putEnchantmentIfPresent("FROST_WALKER", "冰霜行者");
        putEnchantmentIfPresent("IMPALING", "穿刺");
        putEnchantmentIfPresent("KNOCKBACK", "击退");
        putEnchantmentIfPresent("LOOT_BONUS_BLOCKS", "时运");
        putEnchantmentIfPresent("LOOT_BONUS_MOBS", "掠夺");
        putEnchantmentIfPresent("LOYALTY", "忠诚");
        putEnchantmentIfPresent("LUCK_OF_THE_SEA", "海之眷顾");
        putEnchantmentIfPresent("LURE", "饵钓");
        putEnchantmentIfPresent("MENDING", "经验修补");
        putEnchantmentIfPresent("MULTISHOT", "多重射击");
        putEnchantmentIfPresent("PIERCING", "穿透");
        putEnchantmentIfPresent("PROTECTION_ENVIRONMENTAL", "保护");
        putEnchantmentIfPresent("PROTECTION_EXPLOSIONS", "爆炸保护");
        putEnchantmentIfPresent("PROTECTION_FALL", "摔落保护");
        putEnchantmentIfPresent("PROTECTION_FIRE", "火焰保护");
        putEnchantmentIfPresent("PROTECTION_PROJECTILE", "弹射物保护");
        putEnchantmentIfPresent("QUICK_CHARGE", "快速装填");
        putEnchantmentIfPresent("RIPTIDE", "激流");
        putEnchantmentIfPresent("SILK_TOUCH", "精准采集");
        putEnchantmentIfPresent("SWEEPING_EDGE", "横扫之刃");
        putEnchantmentIfPresent("THORNS", "荆棘");
        putEnchantmentIfPresent("VANISHING_CURSE", "诅咒消失");
        putEnchantmentIfPresent("WATER_WORKER", "水下呼吸");
        putEnchantmentIfPresent("SWIFT_SNEAK", "迅捷潜行");
        putEnchantmentIfPresent("SOUL_SPEED", "灵魂疾行");
        putEnchantmentIfPresent("FISHING_SPEED", "急速水流");
        putEnchantmentIfPresent("SWEEPING", "横扫");

        // ---  水下武器专属附魔 (1.13 新增) ---
        putEnchantmentIfPresent("LOYALTY", "忠诚"); // 三叉戟
        putEnchantmentIfPresent("RIPTIDE", "激流"); // 三叉戟
        putEnchantmentIfPresent("CHANNELING", "引雷"); // 三叉戟
        putEnchantmentIfPresent("IMPALING", "穿刺"); // 三叉戟

        // ---  盔甲专属附魔 ---
        putEnchantmentIfPresent("PROTECTION_ENVIRONMENTAL", "保护");
        putEnchantmentIfPresent("PROTECTION_FIRE", "火焰保护");
        putEnchantmentIfPresent("PROTECTION_FALL", "摔落保护");
        putEnchantmentIfPresent("PROTECTION_EXPLOSIONS", "爆炸保护");
        putEnchantmentIfPresent("PROTECTION_PROJECTILE", "弹射物保护");
        putEnchantmentIfPresent("THORNS", "荆棘");
        putEnchantmentIfPresent("DEPTH_STRIDER", "深海疾行");
        putEnchantmentIfPresent("FROST_WALKER", "冰霜行者");
        putEnchantmentIfPresent("BINDING_CURSE", "诅咒绑定");
        putEnchantmentIfPresent("VANISHING_CURSE", "诅咒消失");
        putEnchantmentIfPresent("MENDING", "经验修补");
        putEnchantmentIfPresent("DURABILITY", "耐久");
        putEnchantmentIfPresent("WATER_WORKER", "水下呼吸");
        putEnchantmentIfPresent("SWIFT_SNEAK", "迅捷潜行");
        putEnchantmentIfPresent("SOUL_SPEED", "灵魂疾行");

        // ---  武器专属附魔 ---
        putEnchantmentIfPresent("DAMAGE_ALL", "锋利");
        putEnchantmentIfPresent("DAMAGE_UNDEAD", "亡灵杀手");
        putEnchantmentIfPresent("DAMAGE_ARTHROPODS", "节肢杀手");
        putEnchantmentIfPresent("KNOCKBACK", "击退");
        putEnchantmentIfPresent("FIRE_ASPECT", "火焰附加");
        putEnchantmentIfPresent("LOOT_BONUS_MOBS", "掠夺");
        putEnchantmentIfPresent("SWEEPING_EDGE", "横扫之刃");

        // ---  工具专属附魔 ---
        putEnchantmentIfPresent("DIG_SPEED", "效率");
        putEnchantmentIfPresent("SILK_TOUCH", "精准采集");
        putEnchantmentIfPresent("LOOT_BONUS_BLOCKS", "时运");
        putEnchantmentIfPresent("DURABILITY", "耐久");
        putEnchantmentIfPresent("MENDING", "经验修补");

        // ---  弓/弩 专属附魔 ---
        putEnchantmentIfPresent("ARROW_DAMAGE", "力量");
        putEnchantmentIfPresent("ARROW_KNOCKBACK", "冲击");
        putEnchantmentIfPresent("ARROW_FIRE", "火焰附加");
        putEnchantmentIfPresent("ARROW_INFINITE", "无限");
        putEnchantmentIfPresent("QUICK_CHARGE", "快速装填");
        putEnchantmentIfPresent("MULTISHOT", "多重射击");
        putEnchantmentIfPresent("PIERCING", "穿透");

        // ---  钓鱼竿专属附魔 ---
        putEnchantmentIfPresent("LUCK_OF_THE_SEA", "海之眷顾");
        putEnchantmentIfPresent("LURE", "饵钓");
        putEnchantmentIfPresent("DURABILITY", "耐久");
        putEnchantmentIfPresent("MENDING", "经验修补");
        putEnchantmentIfPresent("FISHING_SPEED", "急速水流");

        // ---  通用 (可用于多种物品类型) ---
        putEnchantmentIfPresent("DURABILITY", "耐久");
        putEnchantmentIfPresent("MENDING", "经验修补");
        putEnchantmentIfPresent("VANISHING_CURSE", "诅咒消失");
        putEnchantmentIfPresent("BINDING_CURSE", "诅咒绑定");
    }

    private final ReEquipment plugin;
    private static Economy econ = null;
    private final Map<Player, Boolean> isProcessing = new HashMap<>();

    public ReclCommand(ReEquipment plugin) {
        this.plugin = plugin;
        if (!setupEconomy()) {
            plugin.getLogger().severe(String.format("[%s] - 未找到经济插件!", plugin.getDescription().getName()));
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "仅允许玩家使用该指令.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                handleStartCommand(player);
                break;
            case "give":
                handleGiveCommand(player, args);
                break;
            case "adminstart":
                handleAdminStartCommand(player);
                break;
            case "reload":
                if (player.hasPermission("reequipment.reload")) {
                    plugin.reloadConfiguration();
                    player.sendMessage(ChatColor.GREEN + "重载成功");
                    break;
                }
                player.sendMessage(ChatColor.RED + "你没有权限使用该指令.");
                break;
            case "help":
            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "ReEquipment " + ChatColor.GRAY + "- " + ChatColor.GREEN + "装备淬炼插件");
        player.sendMessage(ChatColor.YELLOW + "/recl start " + ChatColor.GRAY + "- " + ChatColor.GREEN + "开始淬炼");
        player.sendMessage(ChatColor.YELLOW + "/recl adminstart " + ChatColor.GRAY + "- " + ChatColor.GREEN + "管理员开始淬炼(100%成功)");
        player.sendMessage(ChatColor.YELLOW + "/recl reload " + ChatColor.GRAY + "- " + ChatColor.GREEN + "重载配置");
        player.sendMessage(ChatColor.YELLOW + "/recl give t [数量] " + ChatColor.GRAY + "- " + ChatColor.GREEN + "获取淬炼石");
        player.sendMessage(ChatColor.YELLOW + "/recl give l [数量] " + ChatColor.GRAY + "- " + ChatColor.GREEN + "获取幸运石");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
    }

    private void handleStartCommand(Player player) {
        if (isProcessing.getOrDefault(player, false)) {
            player.sendMessage(ReEquipment.prefix + ChatColor.YELLOW + "您操作过快了,请稍后再试。");
            return;
        }

        isProcessing.put(player, true);

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage(ReEquipment.messages.get("emptyItem"));
            isProcessing.put(player, false);
            return;
        }

        EquipmentType equipmentType = ItemUtils.getEquipmentType(itemInHand);
        if (equipmentType == null) {
            player.sendMessage(ReEquipment.messages.get("noType"));
            isProcessing.put(player, false);
            return;
        }

        int currentLevel = ItemUtils.getTemperingLevel(itemInHand);

        // 直接尝试升级到下一级，不需要判断 currentLevel == -1 的情况
        int nextLevelIndex = currentLevel + 1;

        // 检查是否达到最高等级
        if (nextLevelIndex > ReEquipment.levels.lastKey()) {
            player.sendMessage(ReEquipment.messages.get("cannotTempering"));
            isProcessing.put(player, false);
            return;
        }

        Level nextLevel = ReEquipment.levels.get(nextLevelIndex);

        if (nextLevel == null) {
            player.sendMessage(ReEquipment.messages.get("cannotTempering"));
            isProcessing.put(player, false);
            return;
        }

        if (econ.getBalance(player) < nextLevel.getMoney()) {
            player.sendMessage(ReEquipment.messages.get("noMoney").replace("{Money}", String.valueOf(nextLevel.getMoney())));
            isProcessing.put(player, false);
            return;
        }

        int temperingToolAmount = ItemUtils.countItemsInInventory(player, ReEquipment.temperingTool);
        if (temperingToolAmount < nextLevel.getMaterialAmount()) {
            player.sendMessage(ReEquipment.messages.get("noMaterial"));
            isProcessing.put(player, false);
            return;
        }

        int luckyToolAmount = ItemUtils.countItemsInInventory(player, ReEquipment.luckyTool);
        boolean useLuckyTool = luckyToolAmount > 0;

        EconomyResponse r = econ.withdrawPlayer(player, nextLevel.getMoney());
        if (!r.transactionSuccess()) {
            player.sendMessage(String.format("错误: %s", r.errorMessage));
            isProcessing.put(player, false);
            return;
        }

        ItemUtils.removeItemsFromInventory(player, ReEquipment.temperingTool, nextLevel.getMaterialAmount());

        if (useLuckyTool) {
            ItemUtils.removeItemsFromInventory(player, ReEquipment.luckyTool, 1);
        }

        int successRate = nextLevel.getSuccessRate();
        if (useLuckyTool) {
            successRate += ReEquipment.luckValue;
        }

        Random random = new Random();
        int roll = random.nextInt(100);
        if (roll < successRate) {
            // 成功
            ItemUtils.setTemperingLevel(itemInHand, nextLevelIndex, equipmentType);
            player.sendMessage(ReEquipment.messages.get("success"));
            plugin.playSound(player, "success");
            FireworkMaker.spawnSuccessFirework(player);

            // ... 广播逻辑保持不变 ...
//            if (ReEquipment.broadcastEnabled) {
//                ItemMeta meta = itemInHand.getItemMeta();
//
//                String itemName = meta.hasDisplayName()
//                        ? meta.getDisplayName()
//                        : itemInHand.getType().toString();
//
//                String firstColorCode = "";
//                if (meta.hasDisplayName()) {
//                    String displayName = meta.getDisplayName();
//                    Matcher matcher = Pattern.compile("(?i)" + ChatColor.COLOR_CHAR + "[0-9a-fk-or]").matcher(displayName);
//                    if (matcher.find()) {
//                        firstColorCode = matcher.group();
//                    }
//                }
//                if(firstColorCode == null || firstColorCode.isEmpty()) {
//                    firstColorCode = ChatColor.WHITE.toString();
//                }
//                String messageTemplate = ReEquipment.broadcastMessages.get(0);
//
//                String messageWithPlaceholders = messageTemplate
//                        .replace("%player%", player.getName())
//                        .replace("%level%", String.valueOf(nextLevelIndex))
//                        .replace("#auto_color#", firstColorCode);
//
//                String[] parts = messageWithPlaceholders.split("%item%");
//
//                TextComponent messageStart = new TextComponent(parts[0]);
//
//                TextComponent itemComponent = new TextComponent(itemName);
//
//                List<String> lores = meta.getLore();
//
//                StringBuilder hoverTextBuilder = new StringBuilder(itemName);
//                if (lores != null && !lores.isEmpty()) {
//                    for (String lore : lores) {
//                        hoverTextBuilder.append("\n").append(lore);
//                    }
//                }
//                BaseComponent[] hoverTextComponents = {new TextComponent(hoverTextBuilder.toString())};
//
//                itemComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverTextComponents));
//
//                TextComponent messageEnd = new TextComponent(parts[1]);
//
//                messageStart.addExtra(itemComponent);
//                messageStart.addExtra(messageEnd);
//
//                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
//                    onlinePlayer.spigot().sendMessage(messageStart);
//                }
//            }

            if (ReEquipment.broadcastEnabled && nextLevelIndex >= ReEquipment.broadcastAllowLevel) {
                ItemMeta meta = itemInHand.getItemMeta();

                String itemName = meta.hasDisplayName()
                        ? meta.getDisplayName()
                        : itemInHand.getType().toString();

                String firstColorCode = "";
                if (meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName();
                    Matcher matcher = Pattern.compile("(?i)" + ChatColor.COLOR_CHAR + "[0-9a-fk-or]").matcher(displayName);
                    if (matcher.find()) {
                        firstColorCode = matcher.group();
                    }
                }
                if(firstColorCode == null || firstColorCode.isEmpty()) {
                    firstColorCode = ChatColor.WHITE.toString();
                }
                String messageTemplate = ReEquipment.broadcastMessages.get(0);

                String messageWithPlaceholders = messageTemplate
                        .replace("%player%", player.getName())
                        .replace("%level%", String.valueOf(nextLevelIndex))
                        .replace("#auto_color#", firstColorCode);

                String[] parts = messageWithPlaceholders.split("%item%");

                TextComponent messageStart = new TextComponent(parts[0]);

                TextComponent itemComponent = new TextComponent(itemName);

                List<String> lores = meta.getLore();

                StringBuilder hoverTextBuilder = new StringBuilder();

                // **---  开始补充附魔和属性信息 (调整顺序，附魔放最前) ---**

                // 添加附魔信息 (放在最前面)
                if (meta.hasEnchants()) {
                    hoverTextBuilder.append(itemName);
                    meta.getEnchants().forEach((enchantment, level) -> {
                        String enchantName = chineseEnchantmentNames.getOrDefault(enchantment, enchantment.getKey().getKey());
                        hoverTextBuilder.append("\n").append(ChatColor.GRAY)
                                .append(enchantName)
                                .append(" ").append(NumberFormat.toRoman(level));
                    });
                } else {
                    hoverTextBuilder.append(itemName);
                }

                if (lores != null && !lores.isEmpty()) {
                    for (String lore : lores) {
                        hoverTextBuilder.append("\n").append(lore);
                    }
                }


                BaseComponent[] hoverTextComponents = {new TextComponent(hoverTextBuilder.toString())};

                itemComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverTextComponents));

                TextComponent messageEnd = new TextComponent(parts[1]);

                messageStart.addExtra(itemComponent);
                messageStart.addExtra(messageEnd);

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.spigot().sendMessage(messageStart);
                }
            }
        } else {
            // 失败
            int downgradeRoll = random.nextInt(100);
            if (downgradeRoll < nextLevel.getDowngradeRate()) {
                // 掉级, 但最低等级为 1
                int previousLevelIndex = Math.max(1, currentLevel - 1);
                ItemUtils.setTemperingLevel(itemInHand, previousLevelIndex, equipmentType);
                player.sendMessage(ReEquipment.messages.get("drop"));
                plugin.playSound(player, "downgrade");
            } else {
                // 不变
                player.sendMessage(ReEquipment.messages.get("defeat"));
                plugin.playSound(player, "defeat");
            }
        }

        isProcessing.put(player, false);
    }

    private void handleAdminStartCommand(Player player) {
        if (!player.hasPermission("reequipment.adminstart")) {
            player.sendMessage(ChatColor.RED + "你没有权限使用该指令.");
            return;
        }

        if (isProcessing.getOrDefault(player, false)) {
            player.sendMessage(ReEquipment.prefix + ChatColor.YELLOW + "您操作过快了,请稍后再试。");
            return;
        }

        isProcessing.put(player, true);

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage(ReEquipment.messages.get("emptyItem"));
            isProcessing.put(player, false);
            return;
        }

        EquipmentType equipmentType = ItemUtils.getEquipmentType(itemInHand);
        if (equipmentType == null) {
            player.sendMessage(ReEquipment.messages.get("noType"));
            isProcessing.put(player, false);
            return;
        }

        int currentLevel = ItemUtils.getTemperingLevel(itemInHand);

        int nextLevelIndex = currentLevel + 1;

        if (nextLevelIndex > ReEquipment.levels.lastKey()) {
            player.sendMessage(ReEquipment.messages.get("cannotTempering"));
            isProcessing.put(player, false);
            return;
        }

        Level nextLevel = ReEquipment.levels.get(nextLevelIndex);

        if (nextLevel == null) {
            player.sendMessage(ReEquipment.messages.get("cannotTempering"));
            isProcessing.put(player, false);
            return;
        }
        ItemUtils.setTemperingLevel(itemInHand, nextLevelIndex, equipmentType);
        player.sendMessage(ReEquipment.prefix + ChatColor.GREEN + "管理员强化成功!");
        plugin.playSound(player, "success");
        FireworkMaker.spawnSuccessFirework(player);

        isProcessing.put(player, false);
    }

    private void handleGiveCommand(Player player, String[] args) {
        if (!player.hasPermission("reequipment.give")) {
            player.sendMessage(ChatColor.RED + "你没有权限使用该指令.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "使用方法: /recl give <t/l> [数量]");
            return;
        }

        ItemStack itemToGive;
        int amount = 1;
        switch (args[1].toLowerCase()) {
            case "t":
                itemToGive = ReEquipment.temperingTool.clone();
                break;
            case "l":
                itemToGive = ReEquipment.luckyTool.clone();
                break;
            default:
                player.sendMessage(ChatColor.RED + "请使用 't' 或 'l'.");
                return;
        }

        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "此处仅允许输入数字.");
                return;
            }
        }

        itemToGive.setAmount(amount);
        player.getInventory().addItem(itemToGive);
    }
}