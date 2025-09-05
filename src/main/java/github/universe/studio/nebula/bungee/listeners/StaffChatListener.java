package github.universe.studio.nebula.bungee.listeners;

import github.universe.studio.nebula.bungee.BungeePlugin;
import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author DanielH131COL
 * @created 16/08/2025
 * @project nebula
 * @file StaffChatListener
 */
public class StaffChatListener implements Listener {

    private final BungeePlugin plugin;
    private final Set<UUID> toggledPlayers = new HashSet<>();

    public StaffChatListener(BungeePlugin plugin) {
        this.plugin = plugin;
    }

    public Set<UUID> getToggledPlayers() {
        return toggledPlayers;
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if (event.isCommand() || event.isProxyCommand()) {
            return;
        }

        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        if (!player.hasPermission("nubula.staff")) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (!toggledPlayers.contains(uuid)) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage();
        String staffMessageFormat = CC.translate(ConfigManager.getMessages().getString("messages.staffchat-format", "&c[StaffChat] &7%player% &8(%server%): &f%message%"));
        staffMessageFormat = staffMessageFormat.replace("%player%", player.getName())
                .replace("%server%", player.getServer().getInfo().getName())
                .replace("%message%", message);
        TextComponent staffMessage = new TextComponent(staffMessageFormat);

        for (ProxiedPlayer onlinePlayer : ProxyServer.getInstance().getPlayers()) {
            if (onlinePlayer.hasPermission("nubula.staff")) {
                onlinePlayer.sendMessage(staffMessage);
            }
        }
    }
}