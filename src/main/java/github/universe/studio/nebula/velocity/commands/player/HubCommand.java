package github.universe.studio.nebula.velocity.commands.player;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Optional;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file HubCommand
 */
public class HubCommand implements SimpleCommand {

    private final VelocityPlugin plugin;
    private final ProxyServer server;

    public HubCommand(VelocityPlugin plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player)) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "only-players").getString("&cThis command is for players only"))
            ));
            return;
        }

        Player player = (Player) sender;
        ConfigurationNode config = ConfigManager.getConfig();
        List<String> hubs = null;
        try {
            hubs = config.node("hub").getList(String.class, List.of());
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }

        if (hubs.isEmpty()) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("hub", "no-hubs").getString("&cNo hubs configured"))
            ));
            return;
        }

        String hubName = null;
        for (String name : hubs) {
            if (server.getServer(name).isPresent()) {
                hubName = name;
                break;
            }
        }

        if (hubName == null) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("hub", "hub-not-found").getString("&cHub not found: %hub%").replace("%hub%", "N/A"))
            ));
            return;
        }

        String currentServer = player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("");
        if (currentServer.equalsIgnoreCase(hubName)) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("hub", "already-in-hub").getString("&cYou are already in the hub"))
            ));
            return;
        }

        Optional<RegisteredServer> hub = server.getServer(hubName);
        hub.ifPresent(server -> player.createConnectionRequest(server).fireAndForget());
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                CC.translate(ConfigManager.getMessages().node("hub", "sending-hub").getString("&aSending you to %hub%").replace("%hub%", hubName))
        ));
    }
}