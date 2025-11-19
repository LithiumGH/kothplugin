package me.pvpclub.koth.commands;

import me.pvpclub.koth.Koth;
import me.pvpclub.koth.guis.EditGUI;
import me.pvpclub.koth.guis.RewardGUI;
import me.pvpclub.koth.objects.KothArea;
import me.pvpclub.koth.objects.Selection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KothCommand implements CommandExecutor, TabCompleter {

    private final Koth plugin;

    public KothCommand(Koth plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("koth.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "wand":
                giveWand(player);
                break;

            case "create":
                if (args.length < 2) {
                    player.sendMessage(plugin.getConfigManager().getMessage("invalid-usage")
                            .replace("%usage%", "/koth create <name>"));
                    return true;
                }
                createKoth(player, args[1]);
                break;

            case "edit":
                if (args.length < 2) {
                    player.sendMessage(plugin.getConfigManager().getMessage("invalid-usage")
                            .replace("%usage%", "/koth edit <name>"));
                    return true;
                }
                openEditGUI(player, args[1]);
                break;

            case "reward":
                if (args.length < 3 || !args[1].equalsIgnoreCase("add")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("invalid-usage")
                            .replace("%usage%", "/koth reward add <name>"));
                    return true;
                }
                openRewardGUI(player, args[2]);
                break;

            case "start":
                if (args.length < 2) {
                    player.sendMessage(plugin.getConfigManager().getMessage("invalid-usage")
                            .replace("%usage%", "/koth start <name>"));
                    return true;
                }
                startKoth(player, args[1]);
                break;

            case "stop":
                stopKoth(player);
                break;

            case "list":
                listKoths(player);
                break;

            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void giveWand(Player player) {
        ItemStack wand = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lKOTH Wand"));
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Left click to set position 1",
                ChatColor.GRAY + "Right click to set position 2"
        ));
        wand.setItemMeta(meta);

        player.getInventory().addItem(wand);
        player.sendMessage(plugin.getConfigManager().getMessage("wand-given"));
    }

    private void createKoth(Player player, String name) {
        if (plugin.getKothManager().kothExists(name)) {
            player.sendMessage(plugin.getConfigManager().getMessage("koth-already-exists"));
            return;
        }

        Selection selection = plugin.getSelectionManager().getSelection(player);
        if (!selection.isComplete()) {
            player.sendMessage(ChatColor.RED + "You must select both positions first!");
            return;
        }

        plugin.getKothManager().createKoth(name, selection.getPos1(), selection.getPos2());
        player.sendMessage(plugin.getConfigManager().getMessage("koth-created")
                .replace("%name%", name));

        plugin.getSelectionManager().clearSelection(player);
    }

    private void openEditGUI(Player player, String name) {
        KothArea area = plugin.getKothManager().getKoth(name);
        if (area == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("koth-not-found")
                    .replace("%name%", name));
            return;
        }

        new EditGUI(plugin, area).open(player);
    }

    private void openRewardGUI(Player player, String name) {
        KothArea area = plugin.getKothManager().getKoth(name);
        if (area == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("koth-not-found")
                    .replace("%name%", name));
            return;
        }

        new RewardGUI(plugin, area).open(player);
    }

    private void startKoth(Player player, String name) {
        if (!plugin.getKothManager().kothExists(name)) {
            player.sendMessage(plugin.getConfigManager().getMessage("koth-not-found")
                    .replace("%name%", name));
            return;
        }

        if (plugin.getKothManager().getActiveSession() != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("koth-already-active"));
            return;
        }

        plugin.getKothManager().startKoth(name);
    }

    private void stopKoth(Player player) {
        if (plugin.getKothManager().getActiveSession() == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-active-koth"));
            return;
        }

        String name = plugin.getKothManager().getActiveSession().getArea().getName();
        plugin.getKothManager().stopKoth();
        player.sendMessage(plugin.getConfigManager().getMessage("koth-stopped")
                .replace("%name%", name));
    }

    private void listKoths(Player player) {
        if (plugin.getKothManager().getAllKoths().isEmpty()) {
            player.sendMessage(ChatColor.RED + "No KOTHs have been created yet!");
            return;
        }

        String list = plugin.getKothManager().getAllKoths().stream()
                .map(KothArea::getName)
                .collect(Collectors.joining(", "));

        player.sendMessage(plugin.getConfigManager().getMessage("koth-list")
                .replace("%list%", list));
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "KOTH Commands:");
        player.sendMessage(ChatColor.YELLOW + "/koth wand " + ChatColor.GRAY + "- Get the selection wand");
        player.sendMessage(ChatColor.YELLOW + "/koth create <name> " + ChatColor.GRAY + "- Create a new KOTH");
        player.sendMessage(ChatColor.YELLOW + "/koth edit <name> " + ChatColor.GRAY + "- Edit KOTH settings");
        player.sendMessage(ChatColor.YELLOW + "/koth reward add <name> " + ChatColor.GRAY + "- Set KOTH rewards");
        player.sendMessage(ChatColor.YELLOW + "/koth start <name> " + ChatColor.GRAY + "- Start a KOTH");
        player.sendMessage(ChatColor.YELLOW + "/koth stop " + ChatColor.GRAY + "- Stop the active KOTH");
        player.sendMessage(ChatColor.YELLOW + "/koth list " + ChatColor.GRAY + "- List all KOTHs");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("wand", "create", "edit", "reward", "start", "stop", "list"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("start")) {
                completions.addAll(plugin.getKothManager().getAllKoths().stream()
                        .map(KothArea::getName)
                        .collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("reward")) {
                completions.add("add");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("reward") && args[1].equalsIgnoreCase("add")) {
            completions.addAll(plugin.getKothManager().getAllKoths().stream()
                    .map(KothArea::getName)
                    .collect(Collectors.toList()));
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
