package me.pvpclub.koth.listeners;

import me.pvpclub.koth.Koth;
import me.pvpclub.koth.objects.Selection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WandListener implements Listener {

    private final Koth plugin;

    public WandListener(Koth plugin) {
        this.plugin = plugin;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_AXE) {
            return false;
        }

        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return displayName.equals("KOTH Wand");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (!isWand(item)) {
            return;
        }

        if (!event.getPlayer().hasPermission("koth.admin")) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedBlock() == null) {
            return;
        }

        Selection selection = plugin.getSelectionManager().getSelection(event.getPlayer());

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.setPos1(event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage(plugin.getConfigManager().getMessage("first-position")
                    .replace("%x%", String.valueOf(event.getClickedBlock().getX()))
                    .replace("%y%", String.valueOf(event.getClickedBlock().getY()))
                    .replace("%z%", String.valueOf(event.getClickedBlock().getZ())));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selection.setPos2(event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage(plugin.getConfigManager().getMessage("second-position")
                    .replace("%x%", String.valueOf(event.getClickedBlock().getX()))
                    .replace("%y%", String.valueOf(event.getClickedBlock().getY()))
                    .replace("%z%", String.valueOf(event.getClickedBlock().getZ())));
        }
    }
}