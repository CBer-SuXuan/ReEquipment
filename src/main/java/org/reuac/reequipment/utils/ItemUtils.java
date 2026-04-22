package org.reuac.reequipment.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.manager.DataManager;
import org.reuac.reequipment.model.EquipmentType;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.model.LevelLores;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getLogger;

public class ItemUtils {

	private static final DataManager dataManager = ReEquipment.getInstance().getDataManager();

	public static EquipmentType getEquipmentType(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return null;
		}
		String materialName = item.getType().toString();
		for (Map.Entry<String, List<String>> entry : dataManager.getEquipmentTypeMaterials().entrySet()) {
			if (entry.getValue().contains(materialName)) {
				String effectiveArea = dataManager.getEquipmentTypes().get(entry.getKey());
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
		if (lore == null || lore.isEmpty()) return 0;
		for (String line : lore) {
			for (Map.Entry<Integer, Level> entry : dataManager.getLevels().entrySet()) {
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
		List<String> lore;
		if (meta == null) return;

		lore = meta.getLore();
		if (lore == null || lore.isEmpty()) return;

		Level targetLevel = dataManager.getLevels().get(level);

		if (targetLevel == null) {
			getLogger().warning("尝试设置一个不存在的等级: " + level);
			return;
		}

		LevelLores newLores = targetLevel.getLores().get(type.getTypeName());

		if (newLores == null) {
			getLogger().warning("尝试设置一个不存在的Lores类型: " + type.getTypeName() + " 等级: " + level);
			return;
		}

		for (int i = 1; i <= dataManager.getLevels().lastKey(); i++) {
			Level existingLevel = dataManager.getLevels().get(i);
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
			for (String keyword : dataManager.getBottomLores()) {
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
			if (areItemStacksEqual(item, itemToRemove)) {
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
		String defaultName = dataManager.getItemNames().get(materialName);
		String displayName = meta.getDisplayName();

		// 如果没有自定义名称, 则使用物品的材料名作为 key
		if (defaultName == null) {
			defaultName = dataManager.getItemNames().get(materialName);
		}

		// 检查是否已有自定义名称
		if (displayName.isEmpty() || !meta.hasDisplayName()) {
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

	/**
	 * 获取指定目标等级的失败次数 (PDC 实现)
	 */
	public static int getFailCountForLevel(ItemStack item, int targetLevel) {
		if (item == null || !item.hasItemMeta()) return 0;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return 0;
		// 创建一个唯一的键，例如 "reequipment:prd_fail_11"
		NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(ReEquipment.class), "prd_fail_" + targetLevel);

		// 直接从 PDC 中读取 Integer，如果没有记录则默认返回 0
		return meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
	}

	/**
	 * 更新或清除指定目标等级的失败次数 (PDC 实现)
	 */
	public static void setFailCountForLevel(ItemStack item, int targetLevel, int newCount) {
		if (item == null || !item.hasItemMeta()) return;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(ReEquipment.class), "prd_fail_" + targetLevel);

		if (newCount <= 0) {
			// 如果次数归零（比如强化成功了），直接从 PDC 抹除这个键，节省数据空间
			meta.getPersistentDataContainer().remove(key);
		} else {
			// 否则将新的失败次数写入 PDC
			meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, newCount);
		}

		item.setItemMeta(meta);
	}

	/**
	 * 自动修复物品的 Lore 顺序
	 * 将强化词条强制移动到底部 (bottomLores 之上)
	 */
	public static void fixLoreOrder(ItemStack item) {
		if (item == null || !item.hasItemMeta()) return;
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasLore()) return;

		int level = getTemperingLevel(item);
		if (level == 0) return; // 不是强化装备，跳过

		EquipmentType type = getEquipmentType(item);
		if (type == null) return;

		Level targetLevel = dataManager.getLevels().get(level);
		if (targetLevel == null) return;

		LevelLores reLores = targetLevel.getLores().get(type.getTypeName());
		if (reLores == null) return;

		List<String> originalLore = meta.getLore();
		if (originalLore == null || originalLore.isEmpty()) return;

		// 复制一份进行模拟重排操作
		List<String> newLore = new ArrayList<>(originalLore);
		List<String> currentReLores = new ArrayList<>();

		// 1. 提取出所有的强化词条并从原列表中删除
		Iterator<String> iterator = newLore.iterator();
		while (iterator.hasNext()) {
			String line = iterator.next();
			if (reLores.getLores().contains(line)) {
				currentReLores.add(line);
				iterator.remove();
			}
		}

		if (currentReLores.isEmpty()) return; // 没找到强化词条

		// 2. 找到 BottomLores (如材质、皮肤) 的位置
		int insertionIndex = -1;
		for (int i = 0; i < newLore.size(); i++) {
			String currentLine = newLore.get(i);
			boolean foundKeyword = false;
			for (String keyword : dataManager.getBottomLores()) {
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

		// 3. 将强化词条插入到正确的位置
		if (insertionIndex != -1) {
			newLore.addAll(insertionIndex, currentReLores);
		} else {
			newLore.addAll(currentReLores); // 如果没有底部标签，直接放最后
		}

		// 4. 性能优化：只有当顺序真的发生错乱时，才更新物品 NBT，防止客户端画面闪烁
		if (!originalLore.equals(newLore)) {
			meta.setLore(newLore);
			item.setItemMeta(meta);
		}
	}
}