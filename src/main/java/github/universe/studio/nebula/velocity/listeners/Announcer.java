package github.universe.studio.nebula.velocity.listeners;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file Announcer
 */
public class Announcer {

    private final VelocityPlugin plugin;
    private final List<ScheduledTask> tasks = new ArrayList<>();
    private final ProxyServer server;
    private final CC cc;

    public Announcer(VelocityPlugin plugin, ProxyServer server, CC cc) {
        this.plugin = plugin;
        this.server = server;
        this.cc = cc;
    }

    public void start() {
        stop();
        ConfigurationNode announcements = ConfigManager.getAnnouncements().node("announcements");
        if (announcements.empty()) return;

        for (ConfigurationNode node : announcements.childrenMap().values()) {
            List<String> messages;
            try {
                messages = node.node("messages").getList(String.class, new ArrayList<>());
            } catch (SerializationException e) {
                server.getConsoleCommandSource().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        CC.translate("&cError loading announcement messages: " + e.getMessage())
                ));
                continue;
            }
            int interval = node.node("interval").getInt(60);
            if (messages.isEmpty()) continue;

            int[] currentIndex = {0};
            ScheduledTask task = server.getScheduler().buildTask(plugin, () -> {
                String message = messages.get(currentIndex[0]);
                for (Player player : server.getAllPlayers()) {
                    String formattedMessage = message.replace("%player%", player.getUsername());
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(formattedMessage)));
                }
                currentIndex[0] = (currentIndex[0] + 1) % messages.size();
            }).delay(0, TimeUnit.SECONDS).repeat(interval, TimeUnit.SECONDS).schedule();
            tasks.add(task);
        }
    }

    public void stop() {
        tasks.forEach(ScheduledTask::cancel);
        tasks.clear();
    }
}