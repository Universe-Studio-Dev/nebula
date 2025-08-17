package github.universe.studio.nebula.listeners;

import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.utils.CC;
import github.universe.studio.nebula.utils.ConfigManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author DanielH131COL
 * @created 16/08/2025
 * @project nebula
 * @file GeneralListeners
 */
public class GeneralListeners implements Listener {

    private final Nebula plugin;
    private static final String TARGET_UUID = "74e34777-da27-4729-94ac-d8622407e4a3";

    public GeneralListeners(Nebula plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (player.getUniqueId().toString().equals(TARGET_UUID)) {
            player.sendMessage(CC.translate("&b&lNEBULA"));
            player.sendMessage(CC.translate(" &7⇨ &fVersion: &a" + plugin.getDescription().getVersion()));
            player.sendMessage(CC.translate(" &7⇨ &fAuthor: &a" + plugin.getDescription().getAuthor()));
            player.sendMessage(CC.translate(" &7⇨ &fDescription: &aProxyCore with announcer and stream system and more."));
        }

        if (player.hasPermission("nubula.staff")) {
            String joinMessage = CC.translate(ConfigManager.getMessages().getString("messages.staff-join", "&e%player% &7se ha conectado al servidor &e%server%"))
                    .replace("%player%", player.getName())
                    .replace("%server%", player.getServer() != null ? player.getServer().getInfo().getName() : "Desconocido");
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
            String leaveMessage = CC.translate(ConfigManager.getMessages().getString("messages.staff-leave", "&e%player% &7se ha desconectado"))
                    .replace("%player%", player.getName());
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

            event.setCancelReason(TextComponent.fromLegacyText(CC.translate(
                    ConfigManager.getMessages().getString("blacklist.kick-message")
                            .replace("%reason%", reason))));
            event.setCancelled(true);
        }
    }
}