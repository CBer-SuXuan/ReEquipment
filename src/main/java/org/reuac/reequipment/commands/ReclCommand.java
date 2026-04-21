package org.reuac.reequipment.commands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.model.EquipmentType;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.utils.FireworkMaker;
import org.reuac.reequipment.utils.ItemUtils;

import java.lang.reflect.Field;
import java.util.*;

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

		ItemUtils.removeItemsFromInventory(player, ReEquipment.temperingTool, nextLevel.getMaterialAmount());

		// ======== PRD 概率计算核心 ========
		// 1. 从 prd.yml 中读取当前等级的 PRD 设定 (带有默认值以防写错)
		String prdPath = "Levels." + nextLevelIndex;
		int baseRate = ReEquipment.prdConfig.getInt(prdPath + ".baseRate", nextLevel.getSuccessRate());
		int stepRate = ReEquipment.prdConfig.getInt(prdPath + ".stepRate", 0);
		int maxCap = ReEquipment.prdConfig.getInt(prdPath + ".maxCap", 100);

		// 2. 读取物品记忆中的失败次数
		int failCount = ItemUtils.getFailCountForLevel(itemInHand, nextLevelIndex);

		// 3. 计算实际成功率并应用上限 (HardCap)
		int actualSuccessRate = baseRate + (failCount * stepRate);
		actualSuccessRate = Math.min(actualSuccessRate, maxCap); // 限制在最高概率内

		Random random = new Random();
		int roll = random.nextInt(100);

		if (roll < actualSuccessRate) {
			// ======== 【强化成功】 ========
			ItemUtils.setTemperingLevel(itemInHand, nextLevelIndex, equipmentType);

			// 成功后，清零该等级的失败记录
			ItemUtils.setFailCountForLevel(itemInHand, nextLevelIndex, 0);

			// 发送带概率的成功提示
			player.sendMessage(ReEquipment.prefix + ChatColor.GREEN + " 强化成功！(本次成功率: " + actualSuccessRate + "%)");
			plugin.playSound(player, "success");
			FireworkMaker.spawnSuccessFirework(player);

		} else {
			// ======== 【强化失败】 ========
			// 记录失败次数
			ItemUtils.setFailCountForLevel(itemInHand, nextLevelIndex, failCount + 1);

			int dropToLevel = currentLevel - 1; // 默认掉一级

			// 读取安全里程碑配置
			List<Integer> safeMilestones = ReEquipment.prdConfig.getIntegerList("SafeMilestones");

			// 里程碑判定：如果当前等级大于等于某个里程碑，且掉级后会低于它，就锁在里程碑上
			if (safeMilestones != null && !safeMilestones.isEmpty()) {
				// 从大到小排序，确保匹配最高的里程碑
				safeMilestones.sort(Collections.reverseOrder());
				for (int milestone : safeMilestones) {
					if (currentLevel >= milestone && dropToLevel < milestone) {
						dropToLevel = milestone;
						break;
					}
				}
			}

			// 防止掉落到 0 级以下
			dropToLevel = Math.max(0, dropToLevel);

			ItemUtils.setTemperingLevel(itemInHand, dropToLevel, equipmentType);
			player.sendMessage(ReEquipment.messages.get("drop").replace("掉级", "掉级至 +" + dropToLevel));
			plugin.playSound(player, "downgrade");

			// 情绪补偿提示
			int nextTimeRate = Math.min(baseRate + ((failCount + 1) * stepRate), maxCap);
			player.sendMessage(ChatColor.GRAY + "  [系统] 武器记住了你的汗水... 下次冲击 +" + nextLevelIndex + " 成功率提升至 " + nextTimeRate + "%");
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
			player.sendMessage(ChatColor.RED + "使用方法: /recl give t [数量]");
			return;
		}

		ItemStack itemToGive;
		int amount = 1;
		if (args[1].equalsIgnoreCase("t")) {
			itemToGive = ReEquipment.temperingTool.clone();
		} else {
			player.sendMessage(ChatColor.RED + "请使用 't'.");
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