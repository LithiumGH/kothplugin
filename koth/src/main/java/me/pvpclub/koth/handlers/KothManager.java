package me.pvpclub.koth.handlers;

import me.pvpclub.koth.Koth;
import me.pvpclub.koth.objects.KothArea;
import me.pvpclub.koth.objects.KothSession;
import me.pvpclub.koth.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class KothManager {

    private final Koth plugin;
    private final Map<String, KothArea> koths;
    private KothSession activeSession;
    private BukkitTask borderTask;
    private BukkitTask sessionTask;

    public KothManager(Koth plugin) {
        this.plugin = plugin;
        this.koths = new HashMap<>();
    }

    public void createKoth(String name, Location pos1, Location pos2) {
        KothArea area = new KothArea(name, pos1, pos2);
        koths.put(name.toLowerCase(), area);

        plugin.getConfigManager().getTimers().set("timers." + name + ".time-between", 3600);
        plugin.getConfigManager().getTimers().set("timers." + name + ".capture-time", 300);
        plugin.getConfigManager().saveTimers();

        saveKoths();
    }

    public KothArea getKoth(String name) {
        return koths.get(name.toLowerCase());
    }

    public Collection<KothArea> getAllKoths() {
        return koths.values();
    }

    public boolean kothExists(String name) {
        return koths.containsKey(name.toLowerCase());
    }

    public void startKoth(String name) {
        if (activeSession != null && activeSession.isActive()) {
            return;
        }

        KothArea area = getKoth(name);
        if (area == null) {
            return;
        }

        activeSession = new KothSession(area);

        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("koth-started")
                .replace("%name%", area.getName()));

        startSessionTask();
    }

    private void startSessionTask() {
        sessionTask = new BukkitRunnable() {
            public void run() {
                if (activeSession == null || !activeSession.isActive()) {
                    cancel();
                    return;
                }

                updateSession();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateSession() {
        KothArea area = activeSession.getArea();
        List<Player> playersInside = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (area.contains(player.getLocation())) {
                playersInside.add(player);
                activeSession.addPlayer(player);
            } else {
                activeSession.removePlayer(player);
            }
        }

        if (playersInside.size() == 1) {
            Player holder = playersInside.get(0);

            if (activeSession.getCurrentHolder() == null) {
                activeSession.setCurrentHolder(holder.getUniqueId());
                Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("player-entered")
                        .replace("%player%", holder.getName()));
            } else if (!activeSession.getCurrentHolder().equals(holder.getUniqueId())) {
                Player oldHolder = activeSession.getHolderPlayer();
                if (oldHolder != null) {
                    Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("player-left")
                            .replace("%player%", oldHolder.getName()));
                }
                activeSession.setCurrentHolder(holder.getUniqueId());
                Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("player-entered")
                        .replace("%player%", holder.getName()));
            }

            activeSession.decrementTime();

            if (activeSession.getTimeLeft() <= 0) {
                endKoth(holder);
            }
        } else {
            if (activeSession.getCurrentHolder() != null) {
                Player oldHolder = activeSession.getHolderPlayer();
                if (oldHolder != null) {
                    Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("player-left")
                            .replace("%player%", oldHolder.getName()));
                }
                activeSession.setCurrentHolder(null);
            }
        }

        updateBossBar();
    }

    private void updateBossBar() {
        String holderName = "None";
        if (activeSession.getCurrentHolder() != null) {
            Player holder = activeSession.getHolderPlayer();
            if (holder != null) {
                holderName = holder.getName();
            }
        }

        int timeLeft = activeSession.getTimeLeft();
        String timeFormatted = formatTime(timeLeft);

        String title;
        if (activeSession.getCurrentHolder() != null) {
            title = plugin.getConfigManager().getMessageWithoutPrefix("bossbar.title")
                    .replace("%holder%", holderName)
                    .replace("%time%", timeFormatted);
        } else {
            title = plugin.getConfigManager().getMessageWithoutPrefix("bossbar.no-holder-title")
                    .replace("%time%", timeFormatted);
        }

        double progress = (double) timeLeft / activeSession.getArea().getCaptureTime();
        progress = Math.max(0.0, Math.min(1.0, progress));

        activeSession.getBossBar().setTitle(title);
        activeSession.getBossBar().setProgress(progress);
    }

    private void endKoth(Player winner) {
        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("koth-won")
                .replace("%player%", winner.getName()));

        giveRewards(winner, activeSession.getArea().getName());

        stopKoth();
    }

    private void giveRewards(Player player, String kothName) {
        List<Map<?, ?>> rewardMaps = plugin.getConfigManager().getRewards()
                .getMapList("rewards." + kothName);

        if (rewardMaps == null || rewardMaps.isEmpty()) {
            return;
        }

        for (Map<?, ?> map : rewardMaps) {
            ItemStack item = ItemStack.deserialize((Map<String, Object>) map);
            player.getInventory().addItem(item);
        }
    }

    public void stopKoth() {
        if (activeSession != null) {
            activeSession.setActive(false);
            if (sessionTask != null) {
                sessionTask.cancel();
            }
            activeSession = null;
        }
    }

    public void stopAllKoths() {
        stopKoth();
        if (borderTask != null) {
            borderTask.cancel();
        }
    }

    public KothSession getActiveSession() {
        return activeSession;
    }

    public void startBorderTask() {
        long interval = plugin.getConfig().getLong("particle.border-interval", 20L);

        borderTask = new BukkitRunnable() {
            public void run() {
                for (KothArea area : koths.values()) {
                    ParticleUtils.drawBorder(area);
                }
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    public void saveKoths() {
        plugin.getConfigManager().getKoths().set("koths", null);

        for (Map.Entry<String, KothArea> entry : koths.entrySet()) {
            String path = "koths." + entry.getKey();
            KothArea area = entry.getValue();

            plugin.getConfigManager().getKoths().set(path + ".name", area.getName());
            plugin.getConfigManager().getKoths().set(path + ".pos1", area.getPos1());
            plugin.getConfigManager().getKoths().set(path + ".pos2", area.getPos2());
            plugin.getConfigManager().getKoths().set(path + ".timeBetween", area.getTimeBetween());
            plugin.getConfigManager().getKoths().set(path + ".captureTime", area.getCaptureTime());
        }

        plugin.getConfigManager().saveKoths();
    }

    public void loadKoths() {
        ConfigurationSection section = plugin.getConfigManager().getKoths().getConfigurationSection("koths");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            String path = "koths." + key;
            String name = plugin.getConfigManager().getKoths().getString(path + ".name");
            Location pos1 = (Location) plugin.getConfigManager().getKoths().get(path + ".pos1");
            Location pos2 = (Location) plugin.getConfigManager().getKoths().get(path + ".pos2");
            int timeBetween = plugin.getConfigManager().getKoths().getInt(path + ".timeBetween", 3600);
            int captureTime = plugin.getConfigManager().getKoths().getInt(path + ".captureTime", 300);

            KothArea area = new KothArea(name, pos1, pos2);
            area.setTimeBetween(timeBetween);
            area.setCaptureTime(captureTime);
            koths.put(name.toLowerCase(), area);
        }

        plugin.getLogger().info("Loaded " + koths.size() + " KOTHs");
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}