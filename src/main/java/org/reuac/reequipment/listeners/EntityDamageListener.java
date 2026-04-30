package org.reuac.reequipment.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.model.EquipmentType;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.model.LevelEffect;
import org.reuac.reequipment.utils.ItemUtils;

public class EntityDamageListener implements Listener {

	private final ReEquipment plugin;

	public EntityDamageListener(ReEquipment plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		double currentDamage = event.getDamage();
		double damageBonus = 0;
		double defense = 0;

		// 1. 如果是实体造成的伤害，尝试计算武器的强化加成
		if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {

			// 【核心需求实现】：只有当受击者也是玩家时，才计算武器伤害加成 (纯 PvP 生效)
			if (damageByEntityEvent.getEntity() instanceof Player) {

				// 攻击者是玩家 (近战)
				if (damageByEntityEvent.getDamager() instanceof Player damager) {
					damageBonus = calculateDamageBonus(damager, false);
				}
				// 攻击者是抛射物 (远程)
				else if (damageByEntityEvent.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
					damageBonus = calculateDamageBonus(shooter, true);
				}
			}
		}

		// 2. 无论伤害来源是什么，只要受击者是玩家，就计算防具的防御力减免
		// 【Bug 修复】：这样写顺便修复了原代码中“怪物打玩家(PvE)”时漏算防具防御力的问题！
		if (event.getEntity() instanceof Player victim) {
			defense = calculateDefense(victim);
		}

		// 3. 最终伤害统一结算
		// 如果是玩家打怪物：damageBonus 为 0，defense 为 0，即原版伤害。
		// 如果是怪物打玩家：damageBonus 为 0，defense 正常计算。
		// 如果是玩家打玩家：damageBonus 正常计算，defense 正常计算。
		double finalDamage = currentDamage + damageBonus - defense;

		// 保证最终伤害不为负数
		event.setDamage(Math.max(finalDamage, 0));
	}

	private static final java.util.Set<Material> RANGED_WEAPONS = java.util.Set.of(
			Material.BOW, Material.CROSSBOW
	);

	private double calculateDamageBonus(Player player, boolean isProjectile) {
		double totalDamageBonus = 0;
		// 检查主手
		ItemStack mainHand = player.getInventory().getItemInMainHand();
		// 近战攻击时跳过远程武器(弓、弩)的伤害加成
		if (isProjectile || !RANGED_WEAPONS.contains(mainHand.getType())) {
			totalDamageBonus += getDamageBonusFromItem(mainHand, "MainHand");
		}
		// 检查副手
		totalDamageBonus += getDamageBonusFromItem(player.getInventory().getItemInOffHand(), "OffHand");
		return totalDamageBonus;
	}

	private double calculateDefense(Player player) {
		double totalDefense = 0;
		// 检查防具
		for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
			totalDefense += getDefenseFromItem(armorPiece, "Armor");
		}
		return totalDefense;
	}

	private double getDamageBonusFromItem(ItemStack item, String effectiveArea) {
		if (item == null || item.getType() == Material.AIR) {
			return 0;
		}

		int levelNumber = ItemUtils.getTemperingLevel(item);
		if (levelNumber > 0) {
			Level level = plugin.getDataManager().getLevels().get(levelNumber);
			EquipmentType equipmentType = ItemUtils.getEquipmentType(item);
			if (level != null && equipmentType != null) {
				LevelEffect effect = level.getEffect().get(equipmentType.getTypeName());
				if (effect != null && effect.getEffectiveArea().equalsIgnoreCase(effectiveArea)) {
					return effect.getDamageBonus();
				}
			}
		}
		return 0;
	}

	private double getDefenseFromItem(ItemStack item, String effectiveArea) {
		if (item == null || item.getType() == Material.AIR) {
			return 0;
		}

		int levelNumber = ItemUtils.getTemperingLevel(item);
		if (levelNumber > 0) {
			Level level = plugin.getDataManager().getLevels().get(levelNumber);
			EquipmentType equipmentType = ItemUtils.getEquipmentType(item);
			if (level != null && equipmentType != null) {
				LevelEffect effect = level.getEffect().get(equipmentType.getTypeName());
				if (effect != null && effect.getEffectiveArea().equalsIgnoreCase(effectiveArea)) {
					return effect.getDefense();
				}
			}
		}
		return 0;
	}
}