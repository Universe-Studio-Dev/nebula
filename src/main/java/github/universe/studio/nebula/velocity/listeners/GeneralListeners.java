package github.universe.studio.nebula.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import github.universe.studio.nebula.velocity.utils.ConnectionTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;

import com.velocitypowered.api.plugin.PluginContainer;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file GeneralListeners
 */
public class GeneralListeners {

    private final Map<String, Long> lastPing = new HashMap<>();
    private final int delay = 2000;
    private final VelocityPlugin plugin;
    private final ProxyServer server;
    private final Random random = new Random();
    private final CC cc;
    private final PluginContainer pluginContainer;

    public GeneralListeners(VelocityPlugin plugin, ProxyServer server, CC cc, PluginContainer pluginContainer) {
        this.plugin = plugin;
        this.server = server;
        this.cc = cc;
        this.pluginContainer = pluginContainer;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        ConfigurationNode config = ConfigManager.getConfig();

        if (config.node("maintenance").getBoolean(false)) {
            if (!player.hasPermission("nebula.admin")) {
                player.disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        CC.translate(ConfigManager.getMessages().node("maintenance", "kick").getString("Server in maintenance mode"))
                ));
            }
        }

        if (player.hasPermission("nubula.staff")) {
            String joinMessage = CC.translate(ConfigManager.getMessages().node("messages", "staff-join").getString("&e%player% &7se ha conectado al servidor &e%server%"))
                    .replace("%player%", player.getUsername())
                    .replace("%server%", player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Desconocido"));
            Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(joinMessage);
            for (Player onlinePlayer : server.getAllPlayers()) {
                if (onlinePlayer.hasPermission("nubula.staff")) {
                    onlinePlayer.sendMessage(message);
                }
            }
        }
    }

    @Subscribe
    public void onJoin(PostLoginEvent event) {
        ConnectionTracker.setLoginTime(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("nubula.staff")) {
            String leaveMessage = CC.translate(ConfigManager.getMessages().node("messages", "staff-leave").getString("&e%player% &7se ha desconectado"))
                    .replace("%player%", player.getUsername());
            Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(leaveMessage);
            for (Player onlinePlayer : server.getAllPlayers()) {
                if (onlinePlayer.hasPermission("nubula.staff")) {
                    onlinePlayer.sendMessage(message);
                }
            }
        }
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        ConfigurationNode config = ConfigManager.getConfig();
        String name = event.getPlayer().getUsername().toLowerCase();
        ConfigurationNode blacklistNode = config.node("blacklist", name);
        if (!blacklistNode.empty()) {
            String reason = blacklistNode.getString("");
            Component kickMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("blacklist", "kick-message").getString("You are blacklisted: %reason%").replace("%reason%", reason))
            );
            event.setResult(com.velocitypowered.api.event.ResultedEvent.ComponentResult.denied(kickMessage));
        }
    }

    @Subscribe
    public void onServerKick(KickedFromServerEvent event) throws SerializationException {
        Player player = event.getPlayer();
        ConfigurationNode config = ConfigManager.getConfig();

        List<String> hubs = config.node("hub").getList(String.class, new ArrayList<>());
        if (hubs.isEmpty()) return;

        String hubName = hubs.get(random.nextInt(hubs.size()));
        Optional<RegisteredServer> hub = server.getServer(hubName);

        if (event.getServer() != null &&
                hub.isPresent() &&
                !event.getServer().getServerInfo().getName().equalsIgnoreCase(hubName)) {

            event.setResult(KickedFromServerEvent.RedirectPlayer.create(hub.get()));
            String msg = config.node("hub-kick").getString("");
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(msg).replace("%hub%", hubName)));
        }
    }

    @Subscribe
    public void onPing(ProxyPingEvent e) {
        String ip = e.getConnection().getRemoteAddress().getAddress().getHostAddress();
        long now = System.currentTimeMillis();

        if (lastPing.containsKey(ip) && now - lastPing.get(ip) < delay) {
            ServerPing currentPing = e.getPing();
            e.setPing(new ServerPing(
                    currentPing.getVersion(),
                    currentPing.getPlayers().orElse(null),
                    LegacyComponentSerializer.legacyAmpersand().deserialize("§cPing blocked by AntiBot"),
                    currentPing.getFavicon().orElse(null)
            ));
        }
        lastPing.put(ip, now);
    }
}