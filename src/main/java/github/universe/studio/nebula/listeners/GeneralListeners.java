package github.universe.studio.nebula.listeners;

import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.utils.CC;
import github.universe.studio.nebula.utils.ConfigManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.Random;

/**
 * @author DanielH131COL
 * @created 16/08/2025
 * @project nebula
 * @file GeneralListeners
 */
public class GeneralListeners implements Listener {

    private final Nebula plugin;
    private final Random random = new Random();
    private static final String TARGET_UUID = "74e34777-da27-4729-94ac-d8622407e4a3";

    public GeneralListeners(Nebula plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (ConfigManager.getConfig().getBoolean("maintenance", false)) {
            if (!player.hasPermission("nebula.admin")) {
                player.disconnect(TextComponent.fromLegacyText(CC.translate(ConfigManager.getMessages().getString("maintenance.kick"))));
            }
        }

        if (player.getUniqueId().toString().equals(TARGET_UUID)) {
            player.sendMessage(CC.translate("&b&lNEBULA"));
            player.sendMessage(CC.translate(" &7⇨ &fVersion: &a" + plugin.getDescription().getVersion()));
            player.sendMessage(CC.translate(" &7⇨ &fAuthor: &a" + plugin.getDescription().getAuthor()));
            player.sendMessage(CC.translate(" &7⇨ &fDescription: &aProxyCore with announcer and stream system and more."));
        }

        if (player.hasPermission("nubula.staff")) {
            String joinMessage = CC.translate(ConfigManager.getMessages().getString("messages.staff-join", "&e%player% &7se ha conectado al servidor &e%server%")).replace("%player%", player.getName()).replace("%server%", player.getServer() != null ? player.getServer().getInfo().getName() : "Desconocido");
            TextComponent message = new TextComponent(joinMessage);
            for (ProxiedPlayer onlinePlayer : ProxyServer.getInstance().getPlayers()) {
                if (onlinePlayer.hasPermission("nubula.staff")) {
                    onlinePlayer.sendMessage(message);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (player.hasPermission("nubula.staff")) {
            String leaveMessage = CC.translate(ConfigManager.getMessages().getString("messages.staff-leave", "&e%player% &7se ha desconectado")).replace("%player%", player.getName());
            TextComponent message = new TextComponent(leaveMessage);
            for (ProxiedPlayer onlinePlayer : ProxyServer.getInstance().getPlayers()) {
                if (onlinePlayer.hasPermission("nubula.staff")) {
                    onlinePlayer.sendMessage(message);
                }
            }
        }
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        String name = event.getConnection().getName().toLowerCase();
        if (ConfigManager.getConfig().contains("blacklist." + name)) {
            String reason = ConfigManager.getConfig().getString("blacklist." + name);
            event.setCancelReason(TextComponent.fromLegacyText(CC.translate(ConfigManager.getMessages().getString("blacklist.kick-message").replace("%reason%", reason))));
            event.setCancelled(true);
        }

        ConfigManager.getConfig().getBoolean("maintenance", false);
    }

    @EventHandler
    public void onServerKick(ServerKickEvent event) {
        ProxiedPlayer player = event.getPlayer();

        List<String> hubs = ConfigManager.getConfig().getStringList("hub");
        if (hubs == null || hubs.isEmpty()) return;

        String hubName = hubs.get(random.nextInt(hubs.size()));
        ServerInfo hub = ProxyServer.getInstance().getServerInfo(hubName);

        if (event.getKickedFrom() != null &&
                hub != null &&
                !event.getKickedFrom().getName().equalsIgnoreCase(hubName)) {

            event.setCancelServer(hub);
            event.setCancelled(true);

            String msg = ConfigManager.getConfig().getString("hub-kick");
            player.sendMessage(TextComponent.fromLegacyText(CC.translate(msg).replace("%hub%", hubName)));
        }
    }
}