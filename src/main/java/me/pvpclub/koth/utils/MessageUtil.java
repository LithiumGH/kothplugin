package me.pvpclub.koth.utils;

import me.pvpclub.koth.Koth;
import org.bukkit.ChatColor;

public class MessageUtil {

    private static Koth plugin = Koth.getInstance();

    public static String getMessage(String path) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", ""));
        String message = plugin.getConfig().getString("messages." + path, "&cMessage not found: " + path);
        if (message.equals("&cMessage not found: " + path)) {
            return prefix + message;
        }
        return prefix + ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getMessageWithoutPrefix(String path) {
        String message = plugin.getConfig().getString("messages." + path, "&cMessage not found: " + path);
        if (message.equals("&cMessage not found: " + path)) {
            return message;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}