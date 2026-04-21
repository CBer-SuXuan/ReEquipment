package org.reuac.reequipment.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.reuac.reequipment.ReEquipment;

import org.reuac.reequipment.model.EquipmentType;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.model.LevelLores;

import static org.bukkit.Bukkit.getLogger;

public class ItemUtils {
    public static EquipmentType getEquipmentType(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        String materialName = item.getType().toString();
        for (Map.Entry<String, List<String>> entry : ReEquipment.equipmentTypeMaterials.entrySet()) {
            if (entry.getValue().contains(materialName)) {
                String effectiveArea = ReEquipment.equipmentTypes.get(entry.getKey());
                return new EquipmentType(entry.getKey(), effectiveArea);
            }
        }
        return null;
    }

    public static int getTemperingLevel(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return 0;
        }

        List<String> lore = meta.getLore();
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            for (Map.Entry<Integer, Level> entry : ReEquipment.levels.entrySet()) {
                Level level = entry.getValue();
                for (Map.Entry<String, LevelLores> loresEntry : level.getLores().entrySet()) {
                    if (loresEntry.getValue().getLores().contains(line)) {
                        return entry.getKey();
                    }
                }
            }
        }
        return 0;
    }

    public static void setTemperingLevel(ItemStack item, int level, EquipmentType type) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();


        if (meta.hasLore()) {
            lore = meta.getLore();
        }

        Level targetLevel = ReEquipment.levels.get(level);

        if (targetLevel == null) {
            getLogger().warning("尝试设置一个不存在的等级: " + level);
            return;
        }

        LevelLores newLores = targetLevel.getLores().get(type.getTypeName());

        if (newLores == null) {
            getLogger().warning("尝试设置一个不存在的Lores类型: " + type.getTypeName() + " 等级: " + level);
            return;
        }

        for (int i = 1; i <= ReEquipment.levels.lastKey(); i++) {
            Level existingLevel = ReEquipment.levels.get(i);
            if (existingLevel != null) {
                LevelLores existingLores = existingLevel.getLores().get(type.getTypeName());
                if (existingLores != null) {
                    lore.removeAll(existingLores.getLores());
                }
            }
        }

        int insertionIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            String currentLine = lore.get(i);
            boolean foundKeyword = false;
            for (String keyword : ReEquipment.bottomLores) {
                if (keyword != null && currentLine != null && currentLine.contains(keyword)) {
                    foundKeyword = true;
                    break;
                }
            }
            if (foundKeyword) {
                insertionIndex = i;
                break;
            }
        }

        List<String> loresToAdd = newLores.getLores();
        if (loresToAdd == null) {
            loresToAdd = new ArrayList<>();
        }

        if (insertionIndex != -1) {
            lore.addAll(insertionIndex, loresToAdd);
        } else {
            lore.addAll(loresToAdd);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        updateItemNameAndLevel(item, level);
    }

    public static int countItemsInInventory(Player player, ItemStack itemToCount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (areItemStacksEqual(item, itemToCount)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static boolean areItemStacksEqual(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            return false;
        }
        return item1.isSimilar(item2);
    }

    public static void removeItemsFromInventory(Player player, ItemStack itemToRemove, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && areItemStacksEqual(item, itemToRemove)) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
                if (remaining == 0) {
                    break;
                }
            }
        }
    }

    // 新增：更新物品名称和等级后缀的方法
    public static void updateItemNameAndLevel(ItemStack item, int level) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        String materialName = item.getType().toString();
        String defaultName = ReEquipment.itemNames.get(materialName);
        String displayName = meta.getDisplayName();

        // 如果没有自定义名称, 则使用物品的材料名作为 key
        if (defaultName == null) {
            defaultName = ReEquipment.itemNames.get(materialName);
        }

        // 检查是否已有自定义名称
        if (displayName == null || displayName.isEmpty() || !meta.hasDisplayName()) {
            // 没有自定义名称，使用默认名称
            if (defaultName != null) {
                meta.setDisplayName(defaultName + " +%level%".replace("%level%", String.valueOf(level)));
            }
        } else {
            // 有自定义名称，更新或添加等级后缀
            String suffix = " \\+([0-9]+)$"; // 修改了正则表达式，只匹配最后一个 +n
            Pattern pattern = Pattern.compile(suffix);
            Matcher matcher = pattern.matcher(displayName);

            if (matcher.find()) {
                // 存在后缀，替换等级
                String newDisplayName = matcher.replaceFirst(" +" + level);
                meta.setDisplayName(newDisplayName);
            } else {
                // 不存在后缀，添加后缀
                meta.setDisplayName(displayName + " +" + level);
            }
        }

        item.setItemMeta(meta);
    }
}