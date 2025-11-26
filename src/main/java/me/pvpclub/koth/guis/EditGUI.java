package me.pvpclub.koth.guis;

import me.pvpclub.koth.Koth;
import me.pvpclub.koth.objects.KothArea;
import me.pvpclub.koth.objects.KothMode;
import me.pvpclub.koth.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditGUI implements Listener {

    private final Koth plugin;
    private final KothArea area;
    private final Inventory inventory;
    private static final Map<UUID, EditGUI> activeGuis = new HashMap<>();

    public EditGUI(Koth plugin, KothArea area) {
        this.plugin = plugin;
        this.area = area;
        this.inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Edit " + area.getName());

        setupItems();
    }

    private void setupItems() {
        ItemStack captureTime = new ItemStack(Material.CLOCK);
        ItemMeta captureMeta = captureTime.getItemMeta();
        captureMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                MessageUtil.getMessageWithoutPrefix("gui-time-title")));
        captureMeta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&',
                        MessageUtil.getMessageWithoutPrefix("gui-time-lore")
                                .replace("%time%", String.valueOf(area.getCaptureTime()))).split("\\|")
        ));
        captureTime.setItemMeta(captureMeta);

        ItemStack mode = new ItemStack(Material.COMPARATOR);
        ItemMeta modeMeta = mode.getItemMeta();
        modeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                MessageUtil.getMessageWithoutPrefix("gui-mode-title")));
        modeMeta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&',
                        MessageUtil.getMessageWithoutPrefix("gui-mode-lore")
                                .replace("%mode%", area.getMode().name())).split("\\|")
        ));
        mode.setItemMeta(modeMeta);

        ItemStack rewards = new ItemStack(Material.EMERALD);
        ItemMeta rewardMeta = rewards.getItemMeta();
        rewardMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                MessageUtil.getMessageWithoutPrefix("gui-reward-title")));
        rewardMeta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&',
                        MessageUtil.getMessageWithoutPrefix("gui-reward-lore")).split("\\|")
        ));
        rewards.setItemMeta(rewardMeta);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(11, captureTime);
        inventory.setItem(13, mode);
        inventory.setItem(15, rewards);
    }

    public void open(Player player) {
        player.openInventory(inventory);
        activeGuis.put(player.getUniqueId(), this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        if (event.getSlot() == 11) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Enter the capture time in seconds:");
            waitForInput(player, InputType.CAPTURE_TIME);
        } else if (event.getSlot() == 13) {
            toggleMode(player);
        } else if (event.getSlot() == 15) {
            player.closeInventory();
            new RewardGUI(plugin, area).open(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            activeGuis.remove(event.getPlayer().getUniqueId());
            HandlerList.unregisterAll(this);
        }
    }

    private void toggleMode(Player player) {
        KothMode newMode = area.getMode() == KothMode.SOLO ? KothMode.CONTESTED : KothMode.SOLO;
        area.setMode(newMode);
        plugin.getDatabaseHandler().saveKoth(area);
        player.sendMessage(ChatColor.GREEN + "Mode changed to: " + ChatColor.YELLOW + newMode.name());
        new EditGUI(plugin, area).open(player);
    }

    private void waitForInput(Player player, InputType type) {
        new InputListener(plugin, player, type, area);
    }

    private enum InputType {
        CAPTURE_TIME
    }

    private static class InputListener implements Listener {

        private final Koth plugin;
        private final Player player;
        private final InputType type;
        private final KothArea area;

        public InputListener(Koth plugin, Player player, InputType type, KothArea area) {
            this.plugin = plugin;
            this.player = player;
            this.type = type;
            this.area = area;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            if (!event.getPlayer().equals(player)) {
                return;
            }

            event.setCancelled(true);
            HandlerList.unregisterAll(this);

            String input = event.getMessage();

            try {
                int value = Integer.parseInt(input);

                if (value <= 0) {
                    player.sendMessage(ChatColor.RED + "Value must be positive!");
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (type == InputType.CAPTURE_TIME) {
                        area.setCaptureTime(value);
                        plugin.getDatabaseHandler().saveKoth(area);
                        player.sendMessage(ChatColor.GREEN + "Capture time set to " + value + " seconds!");
                    }

                    new EditGUI(plugin, area).open(player);
                });
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number! Please try again.");
            }
        }
    }
}