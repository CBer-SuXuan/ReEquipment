package org.reuac.reequipment.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.reuac.reequipment.ReEquipment;

public class MessageUtils {
	// 实例化 MiniMessage 解析器
	private static final MiniMessage MM = MiniMessage.miniMessage();

	/**
	 * 发送带有 Prefix (前缀) 的消息
	 */
	public static void sendMessage(CommandSender sender, String langKey, TagResolver... placeholders) {
		FileConfiguration lang = ReEquipment.getInstance().getConfigManager().getLangConfig();

		String messageString = lang.getString(langKey);
		if (messageString == null || messageString.isEmpty()) return;

		String prefixString = lang.getString("prefix", "<gold>[海绵强化] ");

		// 解析并替换占位符
		Component message = MM.deserialize(prefixString + messageString, placeholders);
		sender.sendMessage(message);
	}

	/**
	 * 发送没有 Prefix (前缀) 的原始消息 (通常用于帮助菜单)
	 */
	public static void sendRawMessage(CommandSender sender, String langKey, TagResolver... placeholders) {
		FileConfiguration lang = ReEquipment.getInstance().getConfigManager().getLangConfig();

		String messageString = lang.getString(langKey);
		if (messageString == null || messageString.isEmpty()) return;

		sender.sendMessage(MM.deserialize(messageString, placeholders));
	}
}