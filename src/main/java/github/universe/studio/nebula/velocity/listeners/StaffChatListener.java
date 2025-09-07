package github.universe.studio.nebula.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file StaffChatListener
 */
public class StaffChatListener {

    private final VelocityPlugin plugin;
    private final ProxyServer server;
    private final CC cc;
    private final Set<UUID> toggledPlayers = new HashSet<>();

    public StaffChatListener(VelocityPlugin plugin, ProxyServer server, CC cc) {
        this.plugin = plugin;
        this.server = server;
        this.cc = cc;
    }

    public Set<UUID> getToggledPlayers() {
        return toggledPlayers;
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        if (event.getMessage().startsWith("/")) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission("nebula.staff")) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (!toggledPlayers.contains(uuid)) {
            return;
        }

        event.setResult(PlayerChatEvent.ChatResult.denied());

        String message = event.getMessage();
        String staffMessageFormat = CC.translate(ConfigManager.getMessages().node("messages", "staffchat-format").getString("&c[StaffChat] &7%player% &8(%server%): &f%message%"));
        staffMessageFormat = staffMessageFormat.replace("%player%", player.getUsername())
                .replace("%server%", player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Desconocido"))
                .replace("%message%", message);
        Component staffMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(staffMessageFormat);

        for (Player onlinePlayer : server.getAllPlayers()) {
            if (onlinePlayer.hasPermission("nebula.staff")) {
                onlinePlayer.sendMessage(staffMessage);
            }
        }
    }
}