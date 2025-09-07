package github.universe.studio.nebula.velocity.commands.staff;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 16/08/2025
 * @project nebula
 * @file MaintenanceCommand
 */
public class MaintenanceCommand implements SimpleCommand {

    private final VelocityPlugin plugin;
    private final ProxyServer server;

    public MaintenanceCommand(VelocityPlugin plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!sender.hasPermission("nebula.admin")) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "no-permission").getString("&cYou do not have permission"))
            ));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("maintenance", "maintenance-usage").getString("&cUsage: /maintenance <on/off>"))
            ));
            return;
        }

        String mode = args[0].toLowerCase();
        ConfigurationNode config = ConfigManager.getConfig();
        try {
            if (mode.equals("on")) {
                config.node("maintenance").set(true);
                ConfigManager.saveConfig();
                server.getAllPlayers().forEach(player -> player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("maintenance", "maintenance-on").getString("&cMaintenance mode enabled")))));
            } else if (mode.equals("off")) {
                config.node("maintenance").set(false);
                ConfigManager.saveConfig();
                server.getAllPlayers().forEach(player -> player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("maintenance", "maintenance-off").getString("&aMaintenance mode disabled")))));
            } else {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        CC.translate(ConfigManager.getMessages().node("maintenance", "maintenance-usage").getString("&cUsage: /maintenance <on/off>"))
                ));
            }
        } catch (SerializationException e) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate("&cError toggling maintenance mode: " + e.getMessage())));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!sender.hasPermission("nebula.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Arrays.asList("on", "off").stream()
                    .filter(mode -> mode.startsWith(partial))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}