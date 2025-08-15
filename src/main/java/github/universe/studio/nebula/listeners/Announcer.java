package github.universe.studio.nebula.listeners;

import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.utils.CC;
import github.universe.studio.nebula.utils.ConfigManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author DanielH131COL
 * @created 14/08/2025
 * @project nebula
 * @file Announcer
 */
public class Announcer {
    private final Nebula plugin;
    private final List<ScheduledTask> tasks = new ArrayList<>();

    public Announcer(Nebula plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        Configuration announcements = ConfigManager.getAnnouncements().getSection("announcements");
        if (announcements == null) return;

        for (String name : announcements.getKeys()) {
            Configuration group = announcements.getSection(name);
            List<String> messages = group.getStringList("messages");
            int interval = group.getInt("interval", 60);
            if (messages.isEmpty()) continue;

            int[] currentIndex = {0};
            ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
                String message = messages.get(currentIndex[0]);
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    String formattedMessage = message.replace("%player%", player.getName());
                    player.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.prefix") + formattedMessage));
                }
                currentIndex[0] = (currentIndex[0] + 1) % messages.size();
            }, 0, interval, TimeUnit.SECONDS);
            tasks.add(task);
        }
    }

    public void stop() {
        tasks.forEach(ScheduledTask::cancel);
        tasks.clear();
    }
}