package me.pvpclub.koth.utils;

import me.pvpclub.koth.objects.KothArea;
import me.pvpclub.koth.objects.Selection;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleUtils {

    public static void spawnParticle(Location location, Player player) {
        player.spawnParticle(Particle.HAPPY_VILLAGER, location.clone().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0);
    }

    public static void drawSelectionBox(Selection selection, Player player) {
        Location pos1 = selection.getPos1();
        Location pos2 = selection.getPos2();

        if (pos1 == null || pos2 == null) {
            return;
        }

        drawBox(pos1, pos2, player, Particle.WAX_ON);
    }

    public static void drawBorder(KothArea area) {
        Location pos1 = area.getPos1();
        Location pos2 = area.getPos2();

        if (pos1 == null || pos2 == null) {
            return;
        }

        drawBoxForAll(pos1, pos2, Particle.FLAME);
    }

    private static void drawBox(Location pos1, Location pos2, Player player, Particle particle) {
        double minX = Math.min(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
        double maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
        double maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;

        double step = 0.5;

        for (double x = minX; x <= maxX; x += step) {
            player.spawnParticle(particle, x, minY, minZ, 1, 0, 0, 0, 0);
            player.spawnParticle(particle, x, minY, maxZ, 1, 0, 0, 0, 0);
            player.spawnParticle(particle, x, maxY, minZ, 1, 0, 0, 0, 0);
            player.spawnParticle(particle, x, maxY, maxZ, 1, 0, 0, 0, 0);
        }

        for (double y = minY; y <= maxY; y += step) {
            player.spawnParticle(particle, minX, y, minZ, 1, 0, 0, 0, 0);
            player.spawnParticle(particle, minX, y, maxZ, 1, 0, 0, 0, 0);
            player.spawnParticle(particle, maxX, y, minZ, 1, 0, 0, 0, 0);
            player.spawnParticle(particle, maxX, y, maxZ, 1, 0, 0, 0, 0);
        }

        for (double z = minZ; z <= maxZ; z += step) {
            player.spawnParticle(particle, minX, minY, z, 1, 0, 0, 0, 0);
            player.spawnParticle(particle, minX, maxY, z, 1, 0, 0, 0, 0);
            player.spawnParticle(particle, maxX, minY, z, 1, 0, 0, 0, 0);
            player.spawnParticle(particle, maxX, maxY, z, 1, 0, 0, 0, 0);
        }
    }

    private static void drawBoxForAll(Location pos1, Location pos2, Particle particle) {
        double minX = Math.min(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
        double maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
        double maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;

        double step = 1.0;

        for (double x = minX; x <= maxX; x += step) {
            pos1.getWorld().spawnParticle(particle, x, minY, minZ, 1, 0, 0, 0, 0);
            pos1.getWorld().spawnParticle(particle, x, minY, maxZ, 1, 0, 0, 0, 0);
            pos1.getWorld().spawnParticle(particle, x, maxY, minZ, 1, 0, 0, 0, 0);
            pos1.getWorld().spawnParticle(particle, x, maxY, maxZ, 1, 0, 0, 0, 0);
        }

        for (double y = minY; y <= maxY; y += step) {
            pos1.getWorld().spawnParticle(particle, minX, y, minZ, 1, 0, 0, 0, 0);
            pos1.getWorld().spawnParticle(particle, minX, y, maxZ, 1, 0, 0, 0, 0);
            pos1.getWorld().spawnParticle(particle, maxX, y, minZ, 1, 0, 0, 0, 0);
            pos1.getWorld().spawnParticle(particle, maxX, y, maxZ, 1, 0, 0, 0, 0);
        }

        for (double z = minZ; z <= maxZ; z += step) {
            pos1.getWorld().spawnParticle(particle, minX, minY, z, 1, 0, 0, 0, 0);
            pos1.getWorld().spawnParticle(particle, minX, maxY, z, 1, 0, 0, 0, 0);
            pos1.getWorld().spawnParticle(particle, maxX, minY, z, 1, 0, 0, 0, 0);
            pos1.getWorld().spawnParticle(particle, maxX, maxY, z, 1, 0, 0, 0, 0);
        }
    }
}