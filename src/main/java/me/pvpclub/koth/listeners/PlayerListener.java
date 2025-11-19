package me.pvpclub.koth.listeners;

import me.pvpclub.koth.Koth;
import me.pvpclub.koth.objects.KothSession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final Koth plugin;

    public PlayerListener(Koth plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        KothSession session = plugin.getKothManager().getActiveSession();
        if (session != null && session.isActive()) {
            if (session.getArea().contains(event.getPlayer().getLocation())) {
                session.addPlayer(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        KothSession session = plugin.getKothManager().getActiveSession();
        if (session != null && session.isActive()) {
            session.removePlayer(event.getPlayer());
        }
    }
}