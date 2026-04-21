package org.reuac.reequipment.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FireworkMaker {

    private static final Random RANDOM = new Random();
    private static final List<Color> FIREWORK_COLORS = Arrays.asList(
            Color.AQUA, Color.BLACK, Color.BLUE, Color.FUCHSIA, Color.GRAY, Color.GREEN,
            Color.LIME, Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE,
            Color.RED, Color.SILVER, Color.TEAL, Color.WHITE, Color.YELLOW
    );
    private static final List<FireworkEffect.Type> FIREWORK_TYPES = Arrays.asList(
            FireworkEffect.Type.BALL, FireworkEffect.Type.BALL_LARGE, FireworkEffect.Type.BURST,
            FireworkEffect.Type.CREEPER, FireworkEffect.Type.STAR
    );

    /**
     * 燃放烟花以庆祝强化成功。
     *
     * @param player 触发烟花效果的玩家。
     */
    public static void spawnSuccessFirework(Player player) {
        Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation().add(0.5, 0.5, 0.5), EntityType.FIREWORK_ROCKET);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        // 创建烟花效果
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(RANDOM.nextBoolean())
                .withColor(getRandomColor())
                .withFade(getRandomColor())
                .with(getRandomType())
                .trail(RANDOM.nextBoolean())
                .build();
        fireworkMeta.addEffect(effect);

        // 随机爆炸强度 (1-3)
        fireworkMeta.setPower(RANDOM.nextInt(3) + 1);
        firework.setFireworkMeta(fireworkMeta);
    }

    /**
     * 从预定义的颜色列表中随机选择一个颜色。
     *
     * @return 随机颜色。
     */
    private static Color getRandomColor() {
        return FIREWORK_COLORS.get(RANDOM.nextInt(FIREWORK_COLORS.size()));
    }

    /**
     * 从预定义的烟花类型列表中随机选择一个类型。
     *
     * @return 随机烟花类型。
     */
    private static FireworkEffect.Type getRandomType() {
        return FIREWORK_TYPES.get(RANDOM.nextInt(FIREWORK_TYPES.size()));
    }
}
