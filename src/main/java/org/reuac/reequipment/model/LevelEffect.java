package org.reuac.reequipment.model;

public class LevelEffect {
    private double damageBonus;
    private double defense;
    // 重新添加 effectiveArea 属性
    private String effectiveArea;

    public LevelEffect(double damageBonus, double defense, String effectiveArea) {
        this.damageBonus = damageBonus;
        this.defense = defense;
        this.effectiveArea = effectiveArea;
    }

    public double getDamageBonus() {
        return damageBonus;
    }

    public void setDamageBonus(double damageBonus) {
        this.damageBonus = damageBonus;
    }

    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    public String getEffectiveArea() {
        return effectiveArea;
    }

    public void setEffectiveArea(String effectiveArea) {
        this.effectiveArea = effectiveArea;
    }
}