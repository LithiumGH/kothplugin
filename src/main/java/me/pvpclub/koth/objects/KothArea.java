package me.pvpclub.koth.objects;

import org.bukkit.Location;
import org.bukkit.World;

public class KothArea {

    private String name;
    private Location pos1;
    private Location pos2;
    private int captureTime;
    private KothMode mode;

    public KothArea(String name, Location pos1, Location pos2) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.captureTime = 300;
        this.mode = KothMode.SOLO;
    }

    public boolean contains(Location location) {
        if (location == null || pos1 == null || pos2 == null) {
            return false;
        }

        if (!location.getWorld().equals(pos1.getWorld())) {
            return false;
        }

        double minX = Math.min(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public String getName() {
        return name;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public int getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(int captureTime) {
        this.captureTime = captureTime;
    }

    public KothMode getMode() {
        return mode;
    }

    public void setMode(KothMode mode) {
        this.mode = mode;
    }

    public World getWorld() {
        return pos1.getWorld();
    }
}