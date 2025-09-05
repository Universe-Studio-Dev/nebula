package github.universe.studio.nebula.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file MotdListener
 */
public class MotdListener {

    private final VelocityPlugin plugin;
    private final ProxyServer server;
    private final CC cc;

    public MotdListener(VelocityPlugin plugin, ProxyServer server, CC cc) {
        this.plugin = plugin;
        this.server = server;
        this.cc = cc;
    }

    @Subscribe
    public void onServerPing(ProxyPingEvent event) {
        ConfigurationNode config = ConfigManager.getConfig();
        if (!config.node("motd", "enabled").getBoolean(true)) {
            return;
        }

        ServerPing currentPing = event.getPing();
        if (config.node("maintenance").getBoolean(false)) {
            String line1 = config.node("motd", "maintenance", "line1").getString("");
            String line2 = config.node("motd", "maintenance", "line2").getString("");

            String motd = CC.translate(line1) + "\n" + CC.translate(line2);
            event.setPing(new ServerPing(
                    currentPing.getVersion(),
                    currentPing.getPlayers().orElse(null),
                    LegacyComponentSerializer.legacyAmpersand().deserialize(motd),
                    currentPing.getFavicon().orElse(null)
            ));
            return;
        }

        String line1 = config.node("motd", "normal", "line1").getString("");
        String line2 = config.node("motd", "normal", "line2").getString("");
        String eventName = config.node("motd", "normal", "event_name").getString("");
        String eventDateStr = config.node("motd", "normal", "event_date").getString(LocalDateTime.now().plusDays(1).toString());

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

        int online = server.getPlayerCount();

        line1 = CC.translate(line1.replace("%event_name%", eventName)
                .replace("%countdown%", countdown)
                .replace("%online%", String.valueOf(online)));

        line2 = CC.translate(line2.replace("%event_name%", eventName)
                .replace("%countdown%", countdown)
                .replace("%online%", String.valueOf(online)));

        String motd = line1 + "\n" + line2;
        event.setPing(new ServerPing(
                currentPing.getVersion(),
                currentPing.getPlayers().orElse(null),
                LegacyComponentSerializer.legacyAmpersand().deserialize(motd),
                currentPing.getFavicon().orElse(null)
        ));
    }
}