package github.universe.studio.nebula.common;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AntiBot {

    private final VelocityPlugin plugin;
    private int joinCount = 0;
    private long lastReset = System.currentTimeMillis();
    private int floodThreshold;
    private Set<String> blockedIPs = new HashSet<>();
    private boolean lockdown = false;

    public AntiBot(VelocityPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        plugin.getServer().getScheduler().buildTask(plugin, this::resetCounter).repeat(1, TimeUnit.MINUTES).schedule();
    }

    private void loadConfig() {
        ConfigurationNode root = ConfigManager.getConfig();
        floodThreshold = root.node("antibot", "flood-threshold").getInt(100);
    }

    private void resetCounter() {
        joinCount = 0;
        lastReset = System.currentTimeMillis();
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        joinCount++;
        String ip = event.getConnection().getRemoteAddress().getHostString();

        if (joinCount > floodThreshold && (System.currentTimeMillis() - lastReset) < 60000) {
            lockdown = true;
            blockedIPs.add(ip);
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("AntiBot activo: Conexiones bloqueadas")));
            plugin.getServer().getAllPlayers().forEach(p -> p.sendMessage(Component.text("⚠ AntiBot activo - Conexiones bloqueadas: " + joinCount)));
        }

        if (lockdown && !isWhitelisted(event.getConnection())) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("Server in lockdown")));
        }
    }

    private boolean isWhitelisted(InboundConnection connection) {
        return false;
    }
}