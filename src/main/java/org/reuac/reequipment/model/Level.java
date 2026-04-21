package org.reuac.reequipment.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Level {
	private int money;
	private int materialAmount;
	private int successRate;
	private Map<String, LevelEffect> effect;
	private Map<String, LevelLores> lores;
    
	public Level(int money, int materialAmount, int successRate, Map<String, LevelEffect> effect, Map<String, LevelLores> lores) {
		this.money = money;
		this.materialAmount = materialAmount;
		this.successRate = successRate;
		this.effect = effect;
		this.lores = lores;
	}

}