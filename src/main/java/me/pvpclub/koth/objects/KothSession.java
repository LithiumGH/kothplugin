package me.pvpclub.koth.objects;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KothSession {

    private KothArea area;
    private UUID currentHolder;
    private int timeLeft;
    private BossBar bossBar;
    private boolean active;

    public KothSession(KothArea area) {
        this.area = area;
        this.currentHolder = null;
        this.timeLeft = area.getCaptureTime();
        this.active = true;
        this.bossBar = Bukkit.createBossBar("", BarColor.YELLOW, BarStyle.SOLID);
    }

    public KothArea getArea() {
        return area;
    }

    public UUID getCurrentHolder() {
        return currentHolder;
    }

    public void setCurrentHolder(UUID currentHolder) {
        this.currentHolder = currentHolder;
        if (currentHolder == null) {
            this.timeLeft = area.getCaptureTime();
        }
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public void decrementTime() {
        if (timeLeft > 0) {
            timeLeft--;
        }
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active && bossBar != null) {
            bossBar.removeAll();
        }
    }

    public Player getHolderPlayer() {
        if (currentHolder == null) {
            return null;
        }
        return Bukkit.getPlayer(currentHolder);
    }

    public void addPlayer(Player player) {
        if (bossBar != null && !bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }
    }

    public void removePlayer(Player player) {
        if (bossBar != null && bossBar.getPlayers().contains(player)) {
            bossBar.removePlayer(player);
        }
    }
}