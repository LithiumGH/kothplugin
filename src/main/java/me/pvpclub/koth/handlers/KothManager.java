package me.pvpclub.koth.handlers;

import me.pvpclub.koth.Koth;
import me.pvpclub.koth.objects.KothArea;
import me.pvpclub.koth.objects.KothMode;
import me.pvpclub.koth.objects.KothSession;
import me.pvpclub.koth.utils.MessageUtil;
import me.pvpclub.koth.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class KothManager {

    private final Koth plugin;
    private final Map<String, KothArea> koths;
    private final Map<String, KothSession> activeSessions;
    private BukkitTask borderTask;
    private BukkitTask sessionTask;

    public KothManager(Koth plugin) {
        this.plugin = plugin;
        this.koths = new HashMap<>();
        this.activeSessions = new HashMap<>();
    }

    public void createKoth(String name, Location pos1, Location pos2) {
        int defaultCaptureTime = plugin.getConfig().getInt("settings.default-capture-time", 300);
        String defaultMode = plugin.getConfig().getString("settings.default-mode", "SOLO");

        KothArea area = new KothArea(name, pos1, pos2);
        area.setCaptureTime(defaultCaptureTime);
        area.setMode(KothMode.valueOf(defaultMode));

        koths.put(name.toLowerCase(), area);
        plugin.getDatabaseHandler().saveKoth(area);
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
        KothArea area = getKoth(name);
        if (area == null) {
            return;
        }

        if (activeSessions.containsKey(name.toLowerCase())) {
            return;
        }

        boolean allowMultiple = plugin.getConfig().getBoolean("settings.allow-multiple-active", true);
        int maxActive = plugin.getConfig().getInt("settings.max-active-koths", 3);

        if (!allowMultiple && !activeSessions.isEmpty()) {
            return;
        }

        if (activeSessions.size() >= maxActive) {
            return;
        }

        KothSession session = new KothSession(area);
        activeSessions.put(name.toLowerCase(), session);

        Bukkit.broadcastMessage(MessageUtil.getMessage("koth-started")
                .replace("%name%", area.getName()));

        if (sessionTask == null || sessionTask.isCancelled()) {
            startSessionTask();
        }
    }

    private void startSessionTask() {
        sessionTask = new BukkitRunnable() {
            public void run() {
                if (activeSessions.isEmpty()) {
                    cancel();
                    sessionTask = null;
                    return;
                }

                for (KothSession session : new ArrayList<>(activeSessions.values())) {
                    if (session.isActive()) {
                        updateSession(session);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateSession(KothSession session) {
        KothArea area = session.getArea();
        List<Player> playersInside = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (area.contains(player.getLocation())) {
                playersInside.add(player);
                session.addPlayer(player);
            } else {
                session.removePlayer(player);
            }
        }

        if (area.getMode() == KothMode.SOLO) {
            updateSoloMode(session, playersInside);
        } else {
            updateContestedMode(session, playersInside);
        }

        updateBossBar(session);
    }

    private void updateSoloMode(KothSession session, List<Player> playersInside) {
        Player currentHolderPlayer = session.getHolderPlayer();
        boolean holderStillInside = currentHolderPlayer != null && playersInside.contains(currentHolderPlayer);

        if (currentHolderPlayer != null && !holderStillInside) {
            Bukkit.broadcastMessage(MessageUtil.getMessage("player-left")
                    .replace("%player%", currentHolderPlayer.getName()));
            session.setCurrentHolder(null);
        }

        if (session.getCurrentHolder() == null && playersInside.size() >= 1) {
            Player newHolder = playersInside.get(0);
            session.setCurrentHolder(newHolder.getUniqueId());
            Bukkit.broadcastMessage(MessageUtil.getMessage("player-entered")
                    .replace("%player%", newHolder.getName()));
        }

        if (session.getCurrentHolder() != null) {
            session.decrementTime();

            if (session.getTimeLeft() <= 0) {
                Player winner = session.getHolderPlayer();
                if (winner != null) {
                    endKoth(session, winner);
                }
            }
        }
    }

    private void updateContestedMode(KothSession session, List<Player> playersInside) {
        if (playersInside.size() == 1) {
            Player holder = playersInside.get(0);

            if (session.getCurrentHolder() == null || !session.getCurrentHolder().equals(holder.getUniqueId())) {
                if (session.getCurrentHolder() != null) {
                    Player oldHolder = session.getHolderPlayer();
                    if (oldHolder != null) {
                        Bukkit.broadcastMessage(MessageUtil.getMessage("player-left")
                                .replace("%player%", oldHolder.getName()));
                    }
                }
                session.setCurrentHolder(holder.getUniqueId());
                session.setTimeLeft(session.getArea().getCaptureTime());
                Bukkit.broadcastMessage(MessageUtil.getMessage("player-entered")
                        .replace("%player%", holder.getName()));
            } else {
                session.decrementTime();

                if (session.getTimeLeft() <= 0) {
                    endKoth(session, holder);
                    return;
                }
            }
        } else if (playersInside.size() > 1) {
            if (session.getCurrentHolder() != null) {
                Player oldHolder = session.getHolderPlayer();
                if (oldHolder != null) {
                    Bukkit.broadcastMessage(MessageUtil.getMessage("koth-contested"));
                }
                session.setCurrentHolder(null);
                session.setTimeLeft(session.getArea().getCaptureTime());
            }
        } else {
            if (session.getCurrentHolder() != null) {
                session.setCurrentHolder(null);
                session.setTimeLeft(session.getArea().getCaptureTime());
            }
        }
    }

    private void updateBossBar(KothSession session) {
        String holderName = "None";
        if (session.getCurrentHolder() != null) {
            Player holder = session.getHolderPlayer();
            if (holder != null) {
                holderName = holder.getName();
            }
        }

        int timeLeft = session.getTimeLeft();
        String timeFormatted = formatTime(timeLeft);

        String title;
        if (session.getCurrentHolder() != null) {
            title = MessageUtil.getMessageWithoutPrefix("bossbar-title")
                    .replace("%name%", session.getArea().getName())
                    .replace("%holder%", holderName)
                    .replace("%time%", timeFormatted);
        } else {
            if (session.getArea().getMode() == KothMode.CONTESTED) {
                title = MessageUtil.getMessageWithoutPrefix("bossbar-contested")
                        .replace("%name%", session.getArea().getName())
                        .replace("%time%", timeFormatted);
            } else {
                title = MessageUtil.getMessageWithoutPrefix("bossbar-no-holder")
                        .replace("%name%", session.getArea().getName())
                        .replace("%time%", timeFormatted);
            }
        }

        double progress = (double) timeLeft / session.getArea().getCaptureTime();
        progress = Math.max(0.0, Math.min(1.0, progress));

        session.getBossBar().setTitle(title);
        session.getBossBar().setProgress(progress);
    }

    private void endKoth(KothSession session, Player winner) {
        Bukkit.broadcastMessage(MessageUtil.getMessage("koth-won")
                .replace("%player%", winner.getName())
                .replace("%name%", session.getArea().getName()));

        giveRewards(winner, session.getArea().getName());

        stopKoth(session.getArea().getName());
    }

    private void giveRewards(Player player, String kothName) {
        List<ItemStack> rewards = plugin.getDatabaseHandler().loadRewards(kothName);

        for (ItemStack item : rewards) {
            player.getInventory().addItem(item);
        }
    }

    public void stopKoth(String name) {
        KothSession session = activeSessions.remove(name.toLowerCase());
        if (session != null) {
            session.setActive(false);
        }
    }

    public void stopAllKoths() {
        for (KothSession session : new ArrayList<>(activeSessions.values())) {
            session.setActive(false);
        }
        activeSessions.clear();

        if (sessionTask != null) {
            sessionTask.cancel();
        }
        if (borderTask != null) {
            borderTask.cancel();
        }
    }

    public KothSession getActiveSession(String name) {
        return activeSessions.get(name.toLowerCase());
    }

    public Collection<KothSession> getActiveSessions() {
        return activeSessions.values();
    }

    public void startBorderTask() {
        if (borderTask != null && !borderTask.isCancelled()) {
            return;
        }

        long interval = plugin.getConfig().getLong("settings.border-particle-interval", 20L);

        borderTask = new BukkitRunnable() {
            public void run() {
                if (!plugin.getConfig().getBoolean("settings.show-border-particles", true)) {
                    return;
                }

                for (KothSession session : activeSessions.values()) {
                    if (session.isActive()) {
                        ParticleUtils.drawBorder(session.getArea());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    public void saveAllKoths() {
        for (KothArea area : koths.values()) {
            plugin.getDatabaseHandler().saveKoth(area);
        }
    }

    public void loadKoths() {
        koths.clear();
        koths.putAll(plugin.getDatabaseHandler().loadKoths());
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

    public void deleteKoth(String name) {
        koths.remove(name.toLowerCase());
        stopKoth(name);
        plugin.getDatabaseHandler().deleteKoth(name);
    }
}