package org.reuac.reequipment.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Level {
	private int money;
	private int materialAmount;
	private int baseRate;
	private int stepRate;
	private int maxCap;
	private Map<String, LevelEffect> effect;
	private Map<String, LevelLores> lores;

	public Level(int money, int materialAmount, int baseRate, int stepRate, int maxCap, Map<String, LevelEffect> effect, Map<String, LevelLores> lores) {
		this.money = money;
		this.materialAmount = materialAmount;
		this.baseRate = baseRate;
		this.stepRate = stepRate;
		this.maxCap = maxCap;
		this.effect = effect;
		this.lores = lores;
	}
}