package github.universe.studio.nebula.bungee.commands.message;

import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author DanielH131COL
 * @created 07/09/2025
 * @project nebula
 * @file ReplyCommand
 */
public class ReplyCommand extends Command implements TabExecutor {

    public ReplyCommand() {
        super("reply", "", "r", "responder");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("messages.no-console", "&cThis command is for players only"))));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length < 1) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("reply.usage", "&cUsage: /reply <message>"))));
            return;
        }

        ProxiedPlayer target = MsgCommand.lastMessaged.get(player);

        if (target == null || !target.isConnected()) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("reply.no-reply", "&cNo one to reply to"))));
            return;
        }

        // Check if the sender is ignored by the target
        List<String> targetIgnoreList = getIgnoreList(target);
        if (targetIgnoreList.contains(player.getUniqueId().toString())) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("message.ignored", "&c%player% is ignoring you")
                            .replace("%player%", target.getName()))));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        String msg = message.toString().trim();

        String toSender = ConfigManager.getMessages().getString("reply.to-sender", "&7[&bYou &7-> &b%target%&7] &f%message%")
                .replace("%sender%", player.getName())
                .replace("%target%", target.getName())
                .replace("%message%", msg);

        String toTarget = ConfigManager.getMessages().getString("reply.to-target", "&7[&b%sender% &7-> &bYou&7] &f%message%")
                .replace("%sender%", player.getName())
                .replace("%target%", target.getName())
                .replace("%message%", msg);

        player.sendMessage(new TextComponent(CC.translate(toSender)));
        target.sendMessage(new TextComponent(CC.translate(toTarget)));

        MsgCommand.lastMessaged.put(player, target);
        MsgCommand.lastMessaged.put(target, player);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private List<String> getIgnoreList(ProxiedPlayer player) {
        return ConfigManager.getConfig().getStringList("ignore." + player.getUniqueId().toString());
    }
}