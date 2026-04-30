package org.reuac.reequipment.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemUtils {

	private static final DataManager dataManager = ReEquipment.getInstance().getDataManager();
	private static final MiniMessage MM = MiniMessage.miniMessage();
	private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

	public static EquipmentType getEquipmentType(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return null;
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
		if (item == null || item.getType() == Material.AIR) return 0;
		ItemMeta meta = item.getItemMeta();
		if (meta == null || !meta.hasLore()) return 0;

		// 使用 Paper API 获取 Component 列表
		List<Component> lore = meta.lore();
		if (lore == null || lore.isEmpty()) return 0;

		for (Component lineComp : lore) {
			// 褪去颜色转为纯文本比对，避免格式干扰
			String plainLine = PLAIN.serialize(lineComp);
			for (Map.Entry<Integer, Level> entry : dataManager.getLevels().entrySet()) {
				Level level = entry.getValue();
				for (Map.Entry<String, LevelLores> loresEntry : level.getLores().entrySet()) {
					for (String configuredLore : loresEntry.getValue().getLores()) {
						String plainConfigured = PLAIN.serialize(MM.deserialize(configuredLore));
						if (plainLine.equals(plainConfigured)) {
							return entry.getKey();
						}
					}
				}
			}
		}
		return 0;
	}

	public static void setTemperingLevel(ItemStack item, int level, EquipmentType type) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		// 1. 获取当前 Lore (使用 Component 列表)
		List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
		if (lore == null) lore = new ArrayList<>();

		// 2. 移除所有旧等级的强化词条 (使用之前定义的 Iterator + PlainText 比对逻辑)
		Iterator<Component> loreIterator = lore.iterator();
		while (loreIterator.hasNext()) {
			Component lineComp = loreIterator.next();
			String plainLine = PLAIN.serialize(lineComp);
			boolean shouldRemove = false;

			for (Level existingLevel : dataManager.getLevels().values()) {
				if (existingLevel != null) {
					LevelLores existingLores = existingLevel.getLores().get(type.getTypeName());
					if (existingLores != null && existingLores.getLores() != null) {
						for (String oldLoreStr : existingLores.getLores()) {
							String plainOldLore = PLAIN.serialize(MM.deserialize(oldLoreStr));
							if (plainLine.equals(plainOldLore)) {
								shouldRemove = true;
								break;
							}
						}
					}
				}
				if (shouldRemove) break;
			}
			if (shouldRemove) loreIterator.remove();
		}

		// 3. 添加新等级的词条到列表末尾 (此时顺序可能是乱的，没关系)
		Level targetLevel = dataManager.getLevels().get(level);
		if (targetLevel != null) {
			LevelLores newLores = targetLevel.getLores().get(type.getTypeName());
			if (newLores != null) {
				for (String s : newLores.getLores()) {
					// 反序列化并取消斜体
					lore.add(MM.deserialize(s).decoration(TextDecoration.ITALIC, false));
				}
			}
		}

		// 4. 将更新后的 Lore 存回 Meta 并应用
		// 注意：必须先 setMeta，因为 fixLoreOrder 内部会通过 meta.lore() 读取当前状态
		meta.lore(lore);
		item.setItemMeta(meta);

		// 5. 更新名称后缀
		updateItemNameAndLevel(item, level);

		// 6. 【核心】调用重排方法
		// 它会根据你要求的顺序 (Other -> Tempering -> BottomList) 重新整理所有词条
		fixLoreOrder(item);
	}

	public static void updateItemNameAndLevel(ItemStack item, int level) {
		if (item == null || item.getType() == Material.AIR) return;
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		String materialName = item.getType().toString();
		String defaultNameStr = dataManager.getItemNames().get(materialName);
		if (defaultNameStr == null) defaultNameStr = "<white>" + materialName;

		Component currentDisplayName = meta.displayName();

		if (currentDisplayName == null) {
			// 没有自定义名字，赋予默认名字+等级后缀
			String newNameStr = defaultNameStr + " <red>+" + level;
			meta.displayName(MM.deserialize(newNameStr).decoration(TextDecoration.ITALIC, false));
		} else {
			// 将当前名字转为纯文本，用正则判断是否已经带有后缀
			String plainName = PLAIN.serialize(currentDisplayName);
			String suffixPattern = " \\+([0-9]+)$";
			Matcher matcher = Pattern.compile(suffixPattern).matcher(plainName);

			if (matcher.find()) {
				// 已有后缀，为防止 Component 标签错乱，基于默认名称重构
				String newNameStr = defaultNameStr + " <red>+" + level;
				meta.displayName(MM.deserialize(newNameStr).decoration(TextDecoration.ITALIC, false));
			} else {
				// 没有后缀（比如玩家自己在铁砧敲的名字），直接在名字末尾追加一个 Component
				Component suffix = MM.deserialize(" <red>+" + level).decoration(TextDecoration.ITALIC, false);
				meta.displayName(currentDisplayName.append(suffix));
			}
		}
		item.setItemMeta(meta);
	}

	public static void fixLoreOrder(ItemStack item) {
		if (item == null || !item.hasItemMeta()) return;
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasLore()) return;

		int level = getTemperingLevel(item);
		if (level == 0) return;

		EquipmentType type = getEquipmentType(item);
		if (type == null) return;

		Level targetLevel = dataManager.getLevels().get(level);
		if (targetLevel == null) return;

		LevelLores reLores = targetLevel.getLores().get(type.getTypeName());
		if (reLores == null) return;

		List<Component> originalLore = meta.lore();
		if (originalLore == null || originalLore.isEmpty()) return;

		// --- 开始重排逻辑 ---

		// 1. 准备容器
		List<Component> otherLore = new ArrayList<>();       // 第一部分：普通词条
		List<Component> temperingLoreLines = new ArrayList<>(); // 第二部分：强化词条
		// 第三部分：根据配置列表顺序存放的容器。Key 是配置里的关键词（纯文本），Value 是对应的 Component 列表
		Map<String, List<Component>> bottomGroups = new LinkedHashMap<>();

		// 预初始化底部容器，保证顺序和 config.yml 一致
		for (String keyword : dataManager.getBottomLores()) {
			String plainKeyword = PLAIN.serialize(MM.deserialize(keyword));
			bottomGroups.put(plainKeyword, new ArrayList<>());
		}

		// 2. 准备强化词条比对池
		List<String> expectedPlainLores = new ArrayList<>();
		for (String s : reLores.getLores()) {
			expectedPlainLores.add(PLAIN.serialize(MM.deserialize(s)));
		}

		// 3. 归类所有当前 Lore 行
		for (Component lineComp : originalLore) {
			String plainLine = PLAIN.serialize(lineComp);

			// A. 判断是否是强化词条
			if (expectedPlainLores.contains(plainLine)) {
				temperingLoreLines.add(lineComp);
				continue;
			}

			// B. 判断是否匹配 bottomLores 中的关键词
			boolean matchedBottom = false;
			for (String plainKeyword : bottomGroups.keySet()) {
				if (plainLine.contains(plainKeyword)) {
					bottomGroups.get(plainKeyword).add(lineComp);
					matchedBottom = true;
					break; // 匹配到第一个关键词即归类
				}
			}

			// C. 既不是强化也不是底部词条，归类为普通词条（如附魔、宝石）
			if (!matchedBottom) {
				otherLore.add(lineComp);
			}
		}

		// 4. 按顺序重新组装
		List<Component> finalLore = new ArrayList<>();

		// 第一阶段：其他词条（附魔等）
		finalLore.addAll(otherLore);

		// 第二阶段：强化词条
		finalLore.addAll(temperingLoreLines);

		// 第三阶段：根据 bottomLores 的配置顺序依次添加
		for (List<Component> group : bottomGroups.values()) {
			finalLore.addAll(group);
		}

		// 5. 更新物品（仅当顺序发生变化时）
		if (!originalLore.equals(finalLore)) {
			meta.lore(finalLore);
			item.setItemMeta(meta);
		}
	}

	public static int getFailCountForLevel(ItemStack item, int targetLevel) {
		if (item == null || !item.hasItemMeta()) return 0;
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return 0;
		NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(ReEquipment.class), "prd_fail_" + targetLevel);
		return meta.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
	}

	public static void setFailCountForLevel(ItemStack item, int targetLevel, int newCount) {
		if (item == null || !item.hasItemMeta()) return;
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(ReEquipment.class), "prd_fail_" + targetLevel);
		if (newCount <= 0) {
			meta.getPersistentDataContainer().remove(key);
		} else {
			meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, newCount);
		}
		item.setItemMeta(meta);
	}

	public static int countItemsInInventory(Player player, ItemStack itemToCount) {
		int count = 0;
		for (ItemStack item : player.getInventory().getContents()) {
			if (areItemStacksEqual(item, itemToCount)) count += item.getAmount();
		}
		return count;
	}

	public static boolean areItemStacksEqual(ItemStack item1, ItemStack item2) {
		if (item1 == null || item2 == null) return false;
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
				if (remaining == 0) break;
			}
		}
	}
}