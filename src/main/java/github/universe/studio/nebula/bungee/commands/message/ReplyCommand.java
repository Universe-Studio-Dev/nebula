package github.universe.studio.nebula.bungee.commands.message;

import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ReplyCommand extends Command {

    public ReplyCommand() {
        super("reply", "", "r", "responder");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length < 1) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("reply.usage"))));
            return;
        }

        ProxiedPlayer target = MsgCommand.lastMessaged.get(player);

        if (target == null || !target.isConnected()) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("reply.no-reply"))));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        String msg = message.toString().trim();

        String toSender = ConfigManager.getMessages().getString("reply.to-sender")
                .replace("%sender%", player.getName())
                .replace("%target%", target.getName())
                .replace("%message%", msg);

        String toTarget = ConfigManager.getMessages().getString("reply.to-target")
                .replace("%sender%", player.getName())
                .replace("%target%", target.getName())
                .replace("%message%", msg);

        player.sendMessage(new TextComponent(CC.translate(toSender)));
        target.sendMessage(new TextComponent(CC.translate(toTarget)));

        MsgCommand.lastMessaged.put(player, target);
        MsgCommand.lastMessaged.put(target, player);
    }
}