package me.pvpclub.koth;

import me.pvpclub.koth.commands.KothCommand;
import me.pvpclub.koth.handlers.KothManager;
import me.pvpclub.koth.handlers.SelectionManager;
import me.pvpclub.koth.listeners.PlayerListener;
import me.pvpclub.koth.listeners.WandListener;
import me.pvpclub.koth.placeholders.KothPlaceholder;
import me.pvpclub.koth.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Koth extends JavaPlugin {

    private static Koth instance;
    private ConfigManager configManager;
    private KothManager kothManager;
    private SelectionManager selectionManager;

    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        kothManager = new KothManager(this);
        selectionManager = new SelectionManager(this);

        getCommand("koth").setExecutor(new KothCommand(this));
        getCommand("koth").setTabCompleter(new KothCommand(this));

        Bukkit.getPluginManager().registerEvents(new WandListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KothPlaceholder(this).register();
            getLogger().info("PlaceholderAPI hooked successfully!");
        }

        kothManager.loadKoths();
        kothManager.startBorderTask();

        getLogger().info("Koth plugin has been enabled!");
    }

    public void onDisable() {
        if (kothManager != null) {
            kothManager.stopAllKoths();
            kothManager.saveKoths();
        }
        getLogger().info("Koth plugin has been disabled!");
    }

    public static Koth getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public KothManager getKothManager() {
        return kothManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
}