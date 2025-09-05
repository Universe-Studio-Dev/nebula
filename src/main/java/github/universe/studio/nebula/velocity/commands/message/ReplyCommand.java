package github.universe.studio.nebula.velocity.commands.message;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file ReplyCommand
 */
public class ReplyCommand implements SimpleCommand {

    private final ProxyServer server;

    public ReplyCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("reply", "usage").getString("&cUsage: /reply <message>"))
            ));
            return;
        }

        Player target = MsgCommand.lastMessaged.get(player);

        if (target == null || !server.getPlayer(target.getUniqueId()).isPresent()) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("reply", "no-reply").getString("&cNo one to reply to"))
            ));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        String msg = message.toString().trim();

        String toSender = ConfigManager.getMessages().node("reply", "to-sender").getString("&7To %target%: &f%message%")
                .replace("%sender%", player.getUsername())
                .replace("%target%", target.getUsername())
                .replace("%message%", msg);

        String toTarget = ConfigManager.getMessages().node("reply", "to-target").getString("&7From %sender%: &f%message%")
                .replace("%sender%", player.getUsername())
                .replace("%target%", target.getUsername())
                .replace("%message%", msg);

        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(toSender)));
        target.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(toTarget)));

        MsgCommand.lastMessaged.put(player, target);
        MsgCommand.lastMessaged.put(target, player);
    }
}