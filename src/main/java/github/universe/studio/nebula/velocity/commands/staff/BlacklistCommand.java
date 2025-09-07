package github.universe.studio.nebula.velocity.commands.staff;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file BlacklistCommand
 */
public class BlacklistCommand implements SimpleCommand {

    private final ProxyServer server;

    public BlacklistCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 2) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                    ConfigManager.getMessages().node("blacklist", "usage-add").getString("&cUsage: /blacklist add <player> <reason>")
            )));
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                    ConfigManager.getMessages().node("blacklist", "usage-remove").getString("&cUsage: /blacklist remove <player>")
            )));
            return;
        }

        String action = args[0].toLowerCase();
        String targetName = args[1].toLowerCase();
        ConfigurationNode config = ConfigManager.getConfig();

        if (action.equals("add")) {
            if (args.length < 3) {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("blacklist", "no-reason").getString("&cPlease provide a reason")
                )));
                return;
            }

            String reason = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);

            try {
                config.node("blacklist", targetName).set(String.class, reason);
                ConfigManager.saveConfig();
            } catch (Exception e) {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        CC.translate("&cError saving blacklist: " + e.getMessage())
                ));
                return;
            }

            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                    ConfigManager.getMessages().node("blacklist", "added").getString("&a%player% has been blacklisted: %reason%")
                            .replace("%player%", targetName)
                            .replace("%reason%", reason)
            )));

            server.getAllPlayers().forEach(p -> p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                    ConfigManager.getMessages().node("blacklist", "broadcast-add").getString("&c%player% has been blacklisted: %reason%")
                            .replace("%player%", targetName)
                            .replace("%reason%", reason)
            ))));

            server.getPlayer(targetName).ifPresent(target -> target.disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                    ConfigManager.getMessages().node("blacklist", "kick-message").getString("&cYou are blacklisted: %reason%")
                            .replace("%reason%", reason)
            ))));

        } else if (action.equals("remove")) {
            if (!config.node("blacklist", targetName).empty()) {
                try {
                    config.node("blacklist", targetName).set(null);
                    ConfigManager.saveConfig();
                } catch (Exception e) {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate("&cError removing blacklist: " + e.getMessage())
                    ));
                    return;
                }

                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("blacklist", "removed").getString("&a%player% has been removed from blacklist")
                                .replace("%player%", targetName)
                )));

                server.getAllPlayers().forEach(p -> p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("blacklist", "broadcast-remove").getString("&a%player% has been removed from blacklist")
                                .replace("%player%", targetName)
                ))));
            } else {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("blacklist", "already-removed").getString("&c%player% is not blacklisted")
                                .replace("%player%", targetName)
                )));
            }
        } else {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate("&cInvalid action. Use add/remove.")
            ));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!sender.hasPermission("nebula.blacklist")) {
            return List.of();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Arrays.asList("add", "remove").stream()
                    .filter(action -> action.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String partial = args[1].toLowerCase();
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}