package github.universe.studio.nebula.velocity.commands.player;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file HelpopCommand
 */
public class HelpopCommand implements SimpleCommand {

    private final VelocityPlugin plugin;
    private final ProxyServer server;

    public HelpopCommand(VelocityPlugin plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player)) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "no-console").getString("&cThis command is for players only"))
            ));
            return;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "helpop-usage").getString("&cUsage: /helpop <message>"))
            ));
            return;
        }

        String message = String.join(" ", args);
        String serverName = player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Unknown");

        String playerMessage = CC.translate(ConfigManager.getMessages().node("messages", "helpop-sent").getString("&aHelp request sent: %message%")
                .replace("%message%", message));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(playerMessage));

        List<String> staffMessages = null;
        try {
            staffMessages = ConfigManager.getMessages().node("messages", "helpop-received").getList(String.class, new ArrayList<>());
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        Component staffText = Component.empty();
        for (String line : staffMessages) {
            String formattedLine = CC.translate(line.replace("%player%", player.getUsername())
                    .replace("%server%", serverName)
                    .replace("%message%", message));
            Component lineComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(formattedLine);
            if (!serverName.equals("Unknown")) {
                lineComponent = lineComponent.clickEvent(ClickEvent.runCommand("/server " + serverName));
            }
            staffText = staffText.append(lineComponent).append(Component.newline());
        }

        for (Player onlinePlayer : server.getAllPlayers()) {
            if (onlinePlayer.hasPermission("nebula.staff")) {
                onlinePlayer.sendMessage(staffText);
            }
        }
    }
}