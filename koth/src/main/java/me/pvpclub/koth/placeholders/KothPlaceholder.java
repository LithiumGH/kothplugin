package me.pvpclub.koth.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pvpclub.koth.Koth;
import me.pvpclub.koth.objects.KothSession;
import org.bukkit.entity.Player;

public class KothPlaceholder extends PlaceholderExpansion {

    private final Koth plugin;

    public KothPlaceholder(Koth plugin) {
        this.plugin = plugin;
    }

    public String getIdentifier() {
        return "koth";
    }

    public String getAuthor() {
        return "PvPClub";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public boolean persist() {
        return true;
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        KothSession session = plugin.getKothManager().getActiveSession();

        if (session == null || !session.isActive()) {
            if (identifier.equals("holder")) {
                return "None";
            }
            if (identifier.equals("time")) {
                return "0";
            }
            if (identifier.equals("n")) {
                return "None";
            }
            if (identifier.equals("active")) {
                return "false";
            }
            return null;
        }

        switch (identifier.toLowerCase()) {
            case "holder":
                if (session.getCurrentHolder() == null) {
                    return "None";
                }
                Player holder = session.getHolderPlayer();
                return holder != null ? holder.getName() : "None";

            case "time":
                return String.valueOf(session.getTimeLeft());

            case "time_formatted":
                return formatTime(session.getTimeLeft());

            case "n":
            case "koth_n":
                return session.getArea().getName();

            case "active":
                return "true";

            case "capture_time":
                return String.valueOf(session.getArea().getCaptureTime());

            default:
                return null;
        }
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}