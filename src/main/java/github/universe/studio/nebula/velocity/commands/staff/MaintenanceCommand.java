package github.universe.studio.nebula.velocity.commands.staff;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * @author DanielH131COL
 * @created 04/09/2025
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
        if (mode.equals("on")) {
            try {
                config.node("maintenance").set(true);
                ConfigManager.saveConfig();
                server.getAllPlayers().forEach(p -> p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("maintenance", "maintenance-on").getString("&cMaintenance mode enabled")
                ))));
            } catch (Exception e) {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        CC.translate("&cError enabling maintenance: " + e.getMessage())
                ));
            }
        } else if (mode.equals("off")) {
            try {
                config.node("maintenance").set(false);
                ConfigManager.saveConfig();
                server.getAllPlayers().forEach(p -> p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("maintenance", "maintenance-off").getString("&aMaintenance mode disabled")
                ))));
            } catch (Exception e) {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        CC.translate("&cError disabling maintenance: " + e.getMessage())
                ));
            }
        } else {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("maintenance", "maintenance-usage").getString("&cUsage: /maintenance <on/off>"))
            ));
        }
    }
}