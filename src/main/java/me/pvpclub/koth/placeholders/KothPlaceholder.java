package me.pvpclub.koth.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.pvpclub.koth.Koth;
import me.pvpclub.koth.objects.KothSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class KothPlaceholder extends PlaceholderExpansion {

    private final Koth plugin;

    public KothPlaceholder(Koth plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "koth";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "PvPClub";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier.startsWith("koth_")) {
            String kothName = identifier.substring(5);
            String[] parts = kothName.split("_", 2);
            if (parts.length < 2) return null;

            String name = parts[0];
            String type = parts[1];

            KothSession session = plugin.getKothManager().getActiveSession(name);

            if (session == null || !session.isActive()) {
                return getDefaultValue(type);
            }

            return getSessionValue(session, type);
        }

        Collection<KothSession> sessions = plugin.getKothManager().getActiveSessions();
        if (sessions.isEmpty()) {
            return getDefaultValue(identifier);
        }

        KothSession firstSession = sessions.iterator().next();
        return getSessionValue(firstSession, identifier);
    }

    private String getDefaultValue(String type) {
        switch (type.toLowerCase()) {
            case "holder":
                return "None";
            case "time":
                return "0";
            case "time_formatted":
                return "0s";
            case "name":
                return "None";
            case "active":
                return "false";
            case "mode":
                return "NONE";
            default:
                return null;
        }
    }

    private String getSessionValue(KothSession session, String type) {
        switch (type.toLowerCase()) {
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

            case "name":
                return session.getArea().getName();

            case "active":
                return "true";

            case "mode":
                return session.getArea().getMode().name();

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