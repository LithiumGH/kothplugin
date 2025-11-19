package me.pvpclub.koth.objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class KothArea implements ConfigurationSerializable {

    private String name;
    private Location pos1;
    private Location pos2;
    private int timeBetween;
    private int captureTime;

    public KothArea(String name, Location pos1, Location pos2) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.timeBetween = 3600;
        this.captureTime = 300;
    }

    public KothArea(Map<String, Object> map) {
        this.name = (String) map.get("name");
        this.pos1 = (Location) map.get("pos1");
        this.pos2 = (Location) map.get("pos2");
        this.timeBetween = (Integer) map.get("timeBetween");
        this.captureTime = (Integer) map.get("captureTime");
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("pos1", pos1);
        map.put("pos2", pos2);
        map.put("timeBetween", timeBetween);
        map.put("captureTime", captureTime);
        return map;
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

    public int getTimeBetween() {
        return timeBetween;
    }

    public void setTimeBetween(int timeBetween) {
        this.timeBetween = timeBetween;
    }

    public int getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(int captureTime) {
        this.captureTime = captureTime;
    }

    public World getWorld() {
        return pos1.getWorld();
    }
}