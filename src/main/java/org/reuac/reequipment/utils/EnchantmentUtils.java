package org.reuac.reequipment.utils;

import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EnchantmentUtils {
	private static final Map<Enchantment, String> chineseEnchantmentNames = new HashMap<>();

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

	private static void putEnchantmentIfPresent(String enchantmentFieldName, String chineseName) {
		try {
			Field field = Enchantment.class.getField(enchantmentFieldName);
			Enchantment enchantment = (Enchantment) field.get(null);
			if (enchantment != null) {
				chineseEnchantmentNames.put(enchantment, chineseName);
			}
		} catch (NoSuchFieldException | IllegalAccessException ignored) {
		}
	}

	/**
	 * 获取附魔的中文名，如果没有则返回英文原名
	 */
	public static String getChineseName(Enchantment enchantment) {
		return chineseEnchantmentNames.getOrDefault(enchantment, enchantment.getKey().getKey());
	}
}