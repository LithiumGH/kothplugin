package me.pvpclub.koth.utils;

import me.pvpclub.koth.Koth;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final Koth plugin;
    private FileConfiguration config;
    private FileConfiguration rewards;
    private FileConfiguration timers;
    private FileConfiguration koths;

    private File configFile;
    private File rewardsFile;
    private File timersFile;
    private File kothsFile;

    public ConfigManager(Koth plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        configFile = new File(plugin.getDataFolder(), "config.yml");

        rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
        timersFile = new File(plugin.getDataFolder(), "timer.yml");
        kothsFile = new File(plugin.getDataFolder(), "koths.yml");

        if (!rewardsFile.exists()) {
            plugin.saveResource("rewards.yml", false);
        }
        if (!timersFile.exists()) {
            plugin.saveResource("timer.yml", false);
        }
        if (!kothsFile.exists()) {
            try {
                kothsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        rewards = YamlConfiguration.loadConfiguration(rewardsFile);
        timers = YamlConfiguration.loadConfiguration(timersFile);
        koths = YamlConfiguration.loadConfiguration(kothsFile);
    }

    public void reloadConfigs() {
        config = YamlConfiguration.loadConfiguration(configFile);
        rewards = YamlConfiguration.loadConfiguration(rewardsFile);
        timers = YamlConfiguration.loadConfiguration(timersFile);
        koths = YamlConfiguration.loadConfiguration(kothsFile);
    }

    public void saveRewards() {
        try {
            rewards.save(rewardsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTimers() {
        try {
            timers.save(timersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveKoths() {
        try {
            koths.save(kothsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String path) {
        String prefix = ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix", ""));
        String message = config.getString("messages." + path, "&cMessage not found: " + path);
        return prefix + ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessageWithoutPrefix(String path) {
        String message = config.getString("messages." + path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getRewards() {
        return rewards;
    }

    public FileConfiguration getTimers() {
        return timers;
    }

    public FileConfiguration getKoths() {
        return koths;
    }
}