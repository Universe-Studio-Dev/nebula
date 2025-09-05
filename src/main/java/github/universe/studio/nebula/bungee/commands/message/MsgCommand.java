package github.universe.studio.nebula.bungee.commands.message;

import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.Map;

public class MsgCommand extends Command {

    public static final Map<ProxiedPlayer, ProxiedPlayer> lastMessaged = new HashMap<>();

    public MsgCommand() {
        super("msg", "", "tell", "w", "mensaje");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length < 2) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("message.usage"))));
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);

        if (target == null || target.equals(player)) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("message.not-found"))));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        String msg = message.toString().trim();

        String toSender = ConfigManager.getMessages().getString("message.to-sender")
                .replace("%sender%", player.getName())
                .replace("%target%", target.getName())
                .replace("%message%", msg);

        String toTarget = ConfigManager.getMessages().getString("message.to-target")
                .replace("%sender%", player.getName())
                .replace("%target%", target.getName())
                .replace("%message%", msg);

        player.sendMessage(new TextComponent(CC.translate(toSender)));
        target.sendMessage(new TextComponent(CC.translate(toTarget)));

        lastMessaged.put(player, target);
        lastMessaged.put(target, player);
    }
}