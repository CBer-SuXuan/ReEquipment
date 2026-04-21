package org.reuac.reequipment.model;

import java.util.Map;

public class Level {
    private int money;
    private int materialAmount;
    private int successRate;
    private int downgradeRate;
    private Map<String, LevelEffect> effect;
    private Map<String, LevelLores> lores;

    // Constructor, getters, and setters

    public Level(int money, int materialAmount, int successRate, int downgradeRate, Map<String, LevelEffect> effect, Map<String, LevelLores> lores) {
        this.money = money;
        this.materialAmount = materialAmount;
        this.successRate = successRate;
        this.downgradeRate = downgradeRate;
        this.effect = effect;
        this.lores = lores;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getMaterialAmount() {
        return materialAmount;
    }

    public void setMaterialAmount(int materialAmount) {
        this.materialAmount = materialAmount;
    }

    public int getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(int successRate) {
        this.successRate = successRate;
    }

    public int getDowngradeRate() {
        return downgradeRate;
    }

    public void setDowngradeRate(int downgradeRate) {
        this.downgradeRate = downgradeRate;
    }

    public Map<String, LevelEffect> getEffect() {
        return effect;
    }

    public void setEffect(Map<String, LevelEffect> effect) {
        this.effect = effect;
    }

    public Map<String, LevelLores> getLores() {
        return lores;
    }

    public void setLores(Map<String, LevelLores> lores) {
        this.lores = lores;
    }
}