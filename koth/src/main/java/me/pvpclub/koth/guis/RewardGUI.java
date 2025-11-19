package me.pvpclub.koth.guis;

import me.pvpclub.koth.Koth;
import me.pvpclub.koth.objects.KothArea;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RewardGUI implements Listener {

    private final Koth plugin;
    private final KothArea area;
    private final Inventory inventory;
    private static final Map<UUID, RewardGUI> activeGuis = new HashMap<>();

    public RewardGUI(Koth plugin, KothArea area) {
        this.plugin = plugin;
        this.area = area;
        this.inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Rewards: " + area.getName());

        setupItems();
        loadExistingRewards();
    }

    private void setupItems() {
        ItemStack save = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "SAVE");
        saveMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to save rewards"
        ));
        save.setItemMeta(saveMeta);

        inventory.setItem(53, save);
    }

    private void loadExistingRewards() {
        List<Map<?, ?>> rewardMaps = plugin.getConfigManager().getRewards()
                .getMapList("rewards." + area.getName());

        if (rewardMaps == null || rewardMaps.isEmpty()) {
            return;
        }

        int slot = 0;
        for (Map<?, ?> map : rewardMaps) {
            if (slot >= 53) break;
            ItemStack item = ItemStack.deserialize((Map<String, Object>) map);
            inventory.setItem(slot, item);
            slot++;
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
        activeGuis.put(player.getUniqueId(), this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getSlot() == 53) {
            event.setCancelled(true);
            saveRewards(player);
            return;
        }
    }

    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            activeGuis.remove(event.getPlayer().getUniqueId());
            HandlerList.unregisterAll(this);
        }
    }

    private void saveRewards(Player player) {
        List<Map<String, Object>> rewards = new ArrayList<>();

        for (int i = 0; i < 53; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                rewards.add(item.serialize());
            }
        }

        plugin.getConfigManager().getRewards().set("rewards." + area.getName(), rewards);
        plugin.getConfigManager().saveRewards();

        player.sendMessage(plugin.getConfigManager().getMessage("rewards-saved")
                .replace("%name%", area.getName()));
        player.closeInventory();
    }
}