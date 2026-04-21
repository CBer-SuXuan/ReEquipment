package org.reuac.reequipment.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.reuac.reequipment.ReEquipment;
import org.reuac.reequipment.model.EquipmentType;
import org.reuac.reequipment.model.Level;
import org.reuac.reequipment.model.LevelEffect;
import org.reuac.reequipment.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageListener implements Listener {

    private final ReEquipment plugin;

    public EntityDamageListener(ReEquipment plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true,priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        // 如果是实体造成的伤害
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;

            // 攻击者是玩家 (近战攻击)
            if (damageByEntityEvent.getDamager() instanceof Player) {
                Player damager = (Player) damageByEntityEvent.getDamager();

                // 计算玩家的伤害加成 (无论被攻击者是谁), 近战攻击排除远程武器
                double damageBonus = calculateDamageBonus(damager, false);
                damageByEntityEvent.setDamage(damageByEntityEvent.getDamage() + damageBonus);

                // 如果被攻击者是玩家，则计算防御减免
                if (damageByEntityEvent.getEntity() instanceof Player) {
                    Player player = (Player) damageByEntityEvent.getEntity();
                    double defense = calculateDefense(player);
                    double finalDamage = damageByEntityEvent.getDamage() - defense;
                    damageByEntityEvent.setDamage(finalDamage > 0 ? finalDamage : 0);
                }
            }
            // 攻击者是抛射物
            else if (damageByEntityEvent.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) damageByEntityEvent.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    Player shooter = (Player) projectile.getShooter();

                    // 计算玩家的伤害加成 (无论被攻击者是谁), 远程攻击包含远程武器
                    double damageBonus = calculateDamageBonus(shooter, true);
                    damageByEntityEvent.setDamage(damageByEntityEvent.getDamage() + damageBonus);

                    // 如果被攻击者是玩家，则计算防御减免
                    if (damageByEntityEvent.getEntity() instanceof Player) {
                        Player player = (Player) damageByEntityEvent.getEntity();
                        double defense = calculateDefense(player);
                        double finalDamage = damageByEntityEvent.getDamage() - defense;
                        damageByEntityEvent.setDamage(finalDamage > 0 ? finalDamage : 0);
                    }
                }
            }
        } else {
            // 如果受伤者是玩家, 则计算防御力
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                double defense = calculateDefense(player);
                double finalDamage = event.getDamage() - defense;
                event.setDamage(finalDamage > 0 ? finalDamage : 0);
            }
        }
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
            Level level = ReEquipment.levels.get(levelNumber);
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
            Level level = ReEquipment.levels.get(levelNumber);
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