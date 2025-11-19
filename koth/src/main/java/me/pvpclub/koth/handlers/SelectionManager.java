package me.pvpclub.koth.handlers;

import me.pvpclub.koth.Koth;
import me.pvpclub.koth.objects.Selection;
import me.pvpclub.koth.utils.ParticleUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {

    private final Koth plugin;
    private final Map<UUID, Selection> selections;
    private BukkitTask particleTask;

    public SelectionManager(Koth plugin) {
        this.plugin = plugin;
        this.selections = new HashMap<>();
        startParticleTask();
    }

    public Selection getSelection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), k -> new Selection());
    }

    public void clearSelection(Player player) {
        selections.remove(player.getUniqueId());
    }

    public boolean hasSelection(Player player) {
        return selections.containsKey(player.getUniqueId());
    }

    private void startParticleTask() {
        particleTask = new BukkitRunnable() {
            public void run() {
                for (Map.Entry<UUID, Selection> entry : selections.entrySet()) {
                    Selection selection = entry.getValue();
                    Player player = plugin.getServer().getPlayer(entry.getKey());

                    if (player == null || !player.isOnline()) {
                        continue;
                    }

                    if (selection.getPos1() != null) {
                        ParticleUtils.spawnParticle(selection.getPos1(), player);
                    }

                    if (selection.getPos2() != null) {
                        ParticleUtils.spawnParticle(selection.getPos2(), player);
                    }

                    if (selection.isComplete()) {
                        ParticleUtils.drawSelectionBox(selection, player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public void shutdown() {
        if (particleTask != null) {
            particleTask.cancel();
        }
    }
}