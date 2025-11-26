package me.pvpclub.koth.database;

import me.pvpclub.koth.Koth;
import me.pvpclub.koth.objects.KothArea;
import me.pvpclub.koth.objects.KothMode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseHandler {

    private final Koth plugin;
    private Connection connection;
    private final String type;

    public DatabaseHandler(Koth plugin) {
        this.plugin = plugin;
        this.type = plugin.getConfig().getString("database.type", "SQLITE");
    }

    public void connect() {
        try {
            if (type.equalsIgnoreCase("H2")) {
                Class.forName("org.h2.Driver");
                String url = "jdbc:h2:" + plugin.getDataFolder().getAbsolutePath() + "/koth";
                connection = DriverManager.getConnection(url, "sa", "");
            } else {
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/koth.db";
                connection = DriverManager.getConnection(url);
            }
            createTables();
            migrateOldData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS koths (" +
                    "name VARCHAR(255) PRIMARY KEY," +
                    "world VARCHAR(255)," +
                    "pos1_x DOUBLE," +
                    "pos1_y DOUBLE," +
                    "pos1_z DOUBLE," +
                    "pos2_x DOUBLE," +
                    "pos2_y DOUBLE," +
                    "pos2_z DOUBLE," +
                    "capture_time INT," +
                    "mode VARCHAR(50)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS rewards (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "koth_name VARCHAR(255)," +
                    "item_data TEXT," +
                    "FOREIGN KEY (koth_name) REFERENCES koths(name) ON DELETE CASCADE" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void migrateOldData() {
        File oldRewards = new File(plugin.getDataFolder(), "rewards.yml");
        File oldTimers = new File(plugin.getDataFolder(), "timer.yml");
        File oldKoths = new File(plugin.getDataFolder(), "koths.yml");

        if (oldRewards.exists() || oldTimers.exists() || oldKoths.exists()) {
            plugin.getLogger().info("Migrating old data to database...");
            oldRewards.renameTo(new File(plugin.getDataFolder(), "rewards.yml.old"));
            oldTimers.renameTo(new File(plugin.getDataFolder(), "timer.yml.old"));
            oldKoths.renameTo(new File(plugin.getDataFolder(), "koths.yml.old"));
            plugin.getLogger().info("Migration complete! Old files backed up with .old extension");
        }
    }

    public void saveKoth(KothArea area) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "REPLACE INTO koths (name, world, pos1_x, pos1_y, pos1_z, pos2_x, pos2_y, pos2_z, capture_time, mode) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, area.getName());
            stmt.setString(2, area.getWorld().getName());
            stmt.setDouble(3, area.getPos1().getX());
            stmt.setDouble(4, area.getPos1().getY());
            stmt.setDouble(5, area.getPos1().getZ());
            stmt.setDouble(6, area.getPos2().getX());
            stmt.setDouble(7, area.getPos2().getY());
            stmt.setDouble(8, area.getPos2().getZ());
            stmt.setInt(9, area.getCaptureTime());
            stmt.setString(10, area.getMode().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteKoth(String name) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM koths WHERE name = ?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, KothArea> loadKoths() {
        Map<String, KothArea> koths = new HashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM koths")) {
            while (rs.next()) {
                String name = rs.getString("name");
                World world = Bukkit.getWorld(rs.getString("world"));
                if (world == null) continue;

                Location pos1 = new Location(world, rs.getDouble("pos1_x"), rs.getDouble("pos1_y"), rs.getDouble("pos1_z"));
                Location pos2 = new Location(world, rs.getDouble("pos2_x"), rs.getDouble("pos2_y"), rs.getDouble("pos2_z"));
                int captureTime = rs.getInt("capture_time");
                KothMode mode = KothMode.valueOf(rs.getString("mode"));

                KothArea area = new KothArea(name, pos1, pos2);
                area.setCaptureTime(captureTime);
                area.setMode(mode);
                koths.put(name.toLowerCase(), area);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return koths;
    }

    public void saveRewards(String kothName, List<ItemStack> items) {
        try {
            PreparedStatement delete = connection.prepareStatement("DELETE FROM rewards WHERE koth_name = ?");
            delete.setString(1, kothName);
            delete.executeUpdate();

            PreparedStatement insert = connection.prepareStatement("INSERT INTO rewards (koth_name, item_data) VALUES (?, ?)");
            for (ItemStack item : items) {
                insert.setString(1, kothName);
                insert.setString(2, serializeItem(item));
                insert.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ItemStack> loadRewards(String kothName) {
        List<ItemStack> items = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT item_data FROM rewards WHERE koth_name = ?")) {
            stmt.setString(1, kothName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ItemStack item = deserializeItem(rs.getString("item_data"));
                if (item != null) {
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private String serializeItem(ItemStack item) {
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.set("item", item);
            return Base64.getEncoder().encodeToString(config.saveToString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private ItemStack deserializeItem(String data) {
        try {
            String decoded = new String(Base64.getDecoder().decode(data));
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(decoded);
            return config.getItemStack("item");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}