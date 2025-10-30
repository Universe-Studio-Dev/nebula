package github.universe.studio.nebula.velocity.commands.player;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file PingCommand
 */
public class PingCommand implements SimpleCommand {

    private final ProxyServer server;

    public PingCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!isEnabled()) {
            invocation.source().sendMessage(
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(null)
            );
            return;
        }
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player)) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "no-console").getString("&cThis command is for players only"))
            ));
            return;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            int ping = Math.toIntExact(player.getPing());
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "ping").getString("&aYour ping: %ping%ms").replace("%ping%", String.valueOf(ping)))
            ));
        } else {
            Player target = server.getPlayer(args[0]).orElse(null);
            if (target == null) {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        CC.translate("&cThat player is not online.")
                ));
                return;
            }

            int ping = Math.toIntExact(target.getPing());
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "ping").getString("&a%player%'s ping: %ping%ms").replace("%ping%", String.valueOf(ping)).replace("%player%", target.getUsername()))
            ));
        }
    }

    private boolean isEnabled() {
        return ConfigManager.getConfig().node("commands", "ping").getBoolean(true);
    }
}