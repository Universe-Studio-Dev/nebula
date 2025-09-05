package github.universe.studio.nebula.velocity.commands.message;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file MsgCommand
 */
public class MsgCommand implements SimpleCommand {

    private final ProxyServer server;
    public static final Map<Player, Player> lastMessaged = new HashMap<>();

    public MsgCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("message", "usage").getString("&cUsage: /msg <player> <message>"))
            ));
            return;
        }

        Player target = server.getPlayer(args[0]).orElse(null);

        if (target == null || target.equals(player)) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("message", "not-found").getString("&cPlayer not found or cannot message yourself"))
            ));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        String msg = message.toString().trim();

        String toSender = ConfigManager.getMessages().node("message", "to-sender").getString("&7To %target%: &f%message%")
                .replace("%sender%", player.getUsername())
                .replace("%target%", target.getUsername())
                .replace("%message%", msg);

        String toTarget = ConfigManager.getMessages().node("message", "to-target").getString("&7From %sender%: &f%message%")
                .replace("%sender%", player.getUsername())
                .replace("%target%", target.getUsername())
                .replace("%message%", msg);

        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(toSender)));
        target.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(toTarget)));

        lastMessaged.put(player, target);
        lastMessaged.put(target, player);
    }
}
