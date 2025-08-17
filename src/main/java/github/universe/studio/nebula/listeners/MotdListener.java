package github.universe.studio.nebula.listeners;

import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.utils.CC;
import github.universe.studio.nebula.utils.ConfigManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author DanielH131COL
 * @created 16/08/2025
 * @project nebula
 * @file MotdListener
 */
public class MotdListener implements Listener {

    private final Nebula plugin;

    public MotdListener(Nebula plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerPing(ProxyPingEvent event) {
        net.md_5.bungee.config.Configuration config = ConfigManager.getConfig();
        if (!config.getBoolean("motd.enabled", true)) {
            return;
        }

        if (config.getBoolean("maintenance", false)) {
            String line1 = config.getString("motd.maintenance.line1");
            String line2 = config.getString("motd.maintenance.line2");

            String motd = CC.translate(line1) + "\n" + CC.translate(line2);
            event.getResponse().setDescription(motd);
            return;
        }

        String line1 = config.getString("motd.normal.line1");
        String line2 = config.getString("motd.normal.line2");
        String eventName = config.getString("motd.normal.event_name");
        String eventDateStr = config.getString("motd.normal.event_date", LocalDateTime.now().plusDays(1).toString());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target;
        try {
            target = LocalDateTime.parse(eventDateStr);
        } catch (Exception e) {
            target = now.plusDays(1);
        }

        long seconds = ChronoUnit.SECONDS.between(now, target);
        String countdown;
        if (seconds < 0) {
            countdown = "El evento ha pasado!";
        } else {
            long days = seconds / 86400;
            long hours = (seconds % 86400) / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;

            List<String> timeParts = new ArrayList<>();
            if (days > 0) timeParts.add(days + "d");
            if (hours > 0) timeParts.add(hours + "h");
            if (minutes > 0) timeParts.add(minutes + "m");
            if (secs > 0 || timeParts.isEmpty()) timeParts.add(secs + "s");

            countdown = String.join(" ", timeParts);
        }

        int online = ProxyServer.getInstance().getOnlineCount();

        line1 = CC.translate(line1.replace("%event_name%", eventName)
                .replace("%countdown%", countdown)
                .replace("%online%", String.valueOf(online)));

        line2 = CC.translate(line2.replace("%event_name%", eventName)
                .replace("%countdown%", countdown)
                .replace("%online%", String.valueOf(online)));

        String motd = line1 + "\n" + line2;
        event.getResponse().setDescription(motd);
    }
}