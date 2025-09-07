package github.universe.studio.nebula.bungee.commands.message;

import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 07/09/2025
 * @project nebula
 * @file MsgCommand
 */
public class MsgCommand extends Command implements TabExecutor {

    public static final Map<ProxiedPlayer, ProxiedPlayer> lastMessaged = new HashMap<>();

    public MsgCommand() {
        super("msg", "", "tell", "w", "mensaje");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("messages.no-console", "&cThis command is for players only"))));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length < 2) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("message.usage", "&cUsage: /msg <player> <message>"))));
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);

        if (target == null || target.equals(player)) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("message.not-found", "&cPlayer not found or you cannot message yourself"))));
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
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        String msg = message.toString().trim();

        String toSender = ConfigManager.getMessages().getString("message.to-sender", "&7[&bYou &7-> &b%target%&7] &f%message%")
                .replace("%sender%", player.getName())
                .replace("%target%", target.getName())
                .replace("%message%", msg);

        String toTarget = ConfigManager.getMessages().getString("message.to-target", "&7[&b%sender% &7-> &bYou&7] &f%message%")
                .replace("%sender%", player.getName())
                .replace("%target%", target.getName())
                .replace("%message%", msg);

        player.sendMessage(new TextComponent(CC.translate(toSender)));
        target.sendMessage(new TextComponent(CC.translate(toTarget)));

        lastMessaged.put(player, target);
        lastMessaged.put(target, player);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return ProxyServer.getInstance().getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private List<String> getIgnoreList(ProxiedPlayer player) {
        return ConfigManager.getConfig().getStringList("ignore." + player.getUniqueId().toString());
    }
}