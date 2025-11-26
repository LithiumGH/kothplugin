package me.pvpclub.koth.commands;

import me.pvpclub.koth.Koth;
import me.pvpclub.koth.guis.EditGUI;
import me.pvpclub.koth.guis.RewardGUI;
import me.pvpclub.koth.objects.KothArea;
import me.pvpclub.koth.objects.Selection;
import me.pvpclub.koth.utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Material;
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

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "wand":
                if (!hasPermission(player, "koth.wand")) return true;
                giveWand(player);
                break;

            case "create":
                if (!hasPermission(player, "koth.create")) return true;
                if (args.length < 2) {
                    player.sendMessage(MessageUtil.getMessage("invalid-usage")
                            .replace("%usage%", "/koth create <name>"));
                    return true;
                }
                createKoth(player, args[1]);
                break;

            case "delete":
                if (!hasPermission(player, "koth.delete")) return true;
                if (args.length < 2) {
                    player.sendMessage(MessageUtil.getMessage("invalid-usage")
                            .replace("%usage%", "/koth delete <name>"));
                    return true;
                }
                deleteKoth(player, args[1]);
                break;

            case "edit":
                if (!hasPermission(player, "koth.edit")) return true;
                if (args.length < 2) {
                    player.sendMessage(MessageUtil.getMessage("invalid-usage")
                            .replace("%usage%", "/koth edit <name>"));
                    return true;
                }
                openEditGUI(player, args[1]);
                break;

            case "reward":
                if (!hasPermission(player, "koth.reward")) return true;
                if (args.length < 3 || !args[1].equalsIgnoreCase("add")) {
                    player.sendMessage(MessageUtil.getMessage("invalid-usage")
                            .replace("%usage%", "/koth reward add <name>"));
                    return true;
                }
                openRewardGUI(player, args[2]);
                break;

            case "start":
                if (!hasPermission(player, "koth.start")) return true;
                if (args.length < 2) {
                    player.sendMessage(MessageUtil.getMessage("invalid-usage")
                            .replace("%usage%", "/koth start <name>"));
                    return true;
                }
                startKoth(player, args[1]);
                break;

            case "stop":
                if (!hasPermission(player, "koth.stop")) return true;
                if (args.length < 2) {
                    player.sendMessage(MessageUtil.getMessage("invalid-usage")
                            .replace("%usage%", "/koth stop <name>"));
                    return true;
                }
                stopKoth(player, args[1]);
                break;

            case "list":
                if (!hasPermission(player, "koth.list")) return true;
                listKoths(player);
                break;

            case "active":
                if (!hasPermission(player, "koth.list")) return true;
                listActiveKoths(player);
                break;

            case "reload":
                if (!hasPermission(player, "koth.reload")) return true;
                reloadPlugin(player);
                break;

            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private boolean hasPermission(Player player, String permission) {
        if (!player.hasPermission(permission) && !player.hasPermission("koth.admin")) {
            player.sendMessage(MessageUtil.getMessage("no-permission"));
            return false;
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
        player.sendMessage(MessageUtil.getMessage("wand-given"));
    }

    private void createKoth(Player player, String name) {
        if (plugin.getKothManager().kothExists(name)) {
            player.sendMessage(MessageUtil.getMessage("koth-already-exists"));
            return;
        }

        Selection selection = plugin.getSelectionManager().getSelection(player);
        if (!selection.isComplete()) {
            player.sendMessage(ChatColor.RED + "You must select both positions first!");
            return;
        }

        plugin.getKothManager().createKoth(name, selection.getPos1(), selection.getPos2());
        player.sendMessage(MessageUtil.getMessage("koth-created")
                .replace("%name%", name));

        plugin.getSelectionManager().clearSelection(player);
    }

    private void deleteKoth(Player player, String name) {
        if (!plugin.getKothManager().kothExists(name)) {
            player.sendMessage(MessageUtil.getMessage("koth-not-found")
                    .replace("%name%", name));
            return;
        }

        plugin.getKothManager().deleteKoth(name);
        player.sendMessage(ChatColor.GREEN + "KOTH " + ChatColor.YELLOW + name + ChatColor.GREEN + " has been deleted!");
    }

    private void openEditGUI(Player player, String name) {
        KothArea area = plugin.getKothManager().getKoth(name);
        if (area == null) {
            player.sendMessage(MessageUtil.getMessage("koth-not-found")
                    .replace("%name%", name));
            return;
        }

        new EditGUI(plugin, area).open(player);
    }

    private void openRewardGUI(Player player, String name) {
        KothArea area = plugin.getKothManager().getKoth(name);
        if (area == null) {
            player.sendMessage(MessageUtil.getMessage("koth-not-found")
                    .replace("%name%", name));
            return;
        }

        new RewardGUI(plugin, area).open(player);
    }

    private void startKoth(Player player, String name) {
        if (!plugin.getKothManager().kothExists(name)) {
            player.sendMessage(MessageUtil.getMessage("koth-not-found")
                    .replace("%name%", name));
            return;
        }

        if (plugin.getKothManager().getActiveSession(name) != null) {
            player.sendMessage(MessageUtil.getMessage("koth-already-active")
                    .replace("%name%", name));
            return;
        }

        int maxActive = plugin.getConfig().getInt("settings.max-active-koths", 3);
        if (plugin.getKothManager().getActiveSessions().size() >= maxActive) {
            player.sendMessage(MessageUtil.getMessage("max-koths-active"));
            return;
        }

        plugin.getKothManager().startKoth(name);
    }

    private void stopKoth(Player player, String name) {
        if (plugin.getKothManager().getActiveSession(name) == null) {
            player.sendMessage(MessageUtil.getMessage("no-active-koth"));
            return;
        }

        plugin.getKothManager().stopKoth(name);
        player.sendMessage(MessageUtil.getMessage("koth-stopped")
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

        player.sendMessage(MessageUtil.getMessage("koth-list")
                .replace("%list%", list));
    }

    private void listActiveKoths(Player player) {
        if (plugin.getKothManager().getActiveSessions().isEmpty()) {
            player.sendMessage(ChatColor.RED + "No KOTHs are currently active!");
            return;
        }

        String list = plugin.getKothManager().getActiveSessions().stream()
                .map(session -> session.getArea().getName())
                .collect(Collectors.joining(", "));

        player.sendMessage(MessageUtil.getMessage("active-koth-list")
                .replace("%list%", list));
    }

    private void reloadPlugin(Player player) {
        plugin.reloadConfig();
        plugin.getKothManager().loadKoths();
        player.sendMessage(MessageUtil.getMessage("config-reloaded"));
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "KOTH Commands:");
        player.sendMessage(ChatColor.YELLOW + "/koth wand " + ChatColor.GRAY + "- Get the selection wand");
        player.sendMessage(ChatColor.YELLOW + "/koth create <name> " + ChatColor.GRAY + "- Create a new KOTH");
        player.sendMessage(ChatColor.YELLOW + "/koth delete <name> " + ChatColor.GRAY + "- Delete a KOTH");
        player.sendMessage(ChatColor.YELLOW + "/koth edit <name> " + ChatColor.GRAY + "- Edit KOTH settings");
        player.sendMessage(ChatColor.YELLOW + "/koth reward add <name> " + ChatColor.GRAY + "- Set KOTH rewards");
        player.sendMessage(ChatColor.YELLOW + "/koth start <name> " + ChatColor.GRAY + "- Start a KOTH");
        player.sendMessage(ChatColor.YELLOW + "/koth stop <name> " + ChatColor.GRAY + "- Stop a KOTH");
        player.sendMessage(ChatColor.YELLOW + "/koth list " + ChatColor.GRAY + "- List all KOTHs");
        player.sendMessage(ChatColor.YELLOW + "/koth active " + ChatColor.GRAY + "- List active KOTHs");
        player.sendMessage(ChatColor.YELLOW + "/koth reload " + ChatColor.GRAY + "- Reload the plugin");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("wand", "create", "delete", "edit", "reward", "start", "stop", "list", "active", "reload"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("delete")) {
                completions.addAll(plugin.getKothManager().getAllKoths().stream()
                        .map(KothArea::getName)
                        .collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("stop")) {
                completions.addAll(plugin.getKothManager().getActiveSessions().stream()
                        .map(session -> session.getArea().getName())
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