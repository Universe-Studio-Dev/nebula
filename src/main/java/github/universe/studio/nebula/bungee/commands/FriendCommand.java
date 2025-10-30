package github.universe.studio.nebula.bungee.commands;

import github.universe.studio.nebula.bungee.others.FriendManager;
import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 05/09/2025
 * @project nebula
 * @file FriendCommand
 */
public class FriendCommand extends Command implements TabExecutor {
    private final FriendManager friendManager;
    private static final List<String> SUBCOMMANDS = Arrays.asList("add", "remove", "list", "join", "accept", "deny");

    public FriendCommand(FriendManager friendManager) {
        super("friend", null, "friends", "amigos", "amigo", "frd");
        this.friendManager = friendManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!isEnabled()) {
            sender.sendMessage(new TextComponent(CC.translate("&cThis command is disabled.")));
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(CC.translate("&cThis command is for players only!"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (args.length == 0) {
            player.sendMessage(CC.translate("&b&lNEBULA &7| &3&lFRIENDS"));
            player.sendMessage(CC.translate(""));
            player.sendMessage(CC.translate(" &f⇨ &7/friend list"));
            player.sendMessage(CC.translate(" &f⇨ &7/friend add <player>"));
            player.sendMessage(CC.translate(" &f⇨ &7/friend remove <player>"));
            player.sendMessage(CC.translate(" &f⇨ &7/friend accept <player>"));
            player.sendMessage(CC.translate(" &f⇨ &7/friend deny <player>"));
            player.sendMessage(CC.translate(""));
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "add":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&cUsage: /friend add <player>"));
                    return;
                }
                ProxiedPlayer targetAdd = ProxyServer.getInstance().getPlayer(args[1]);
                if (targetAdd == null) {
                    player.sendMessage(CC.translate("&cPlayer not found!"));
                    return;
                }
                friendManager.sendFriendRequest(player, targetAdd);
                break;
            case "accept":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&cUsage: /friend accept <player>"));
                    return;
                }
                ProxiedPlayer targetAccept = ProxyServer.getInstance().getPlayer(args[1]);
                if (targetAccept == null) {
                    player.sendMessage(CC.translate("&cPlayer not found!"));
                    return;
                }
                friendManager.acceptFriendRequest(player, targetAccept);
                break;
            case "deny":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&cUsage: /friend deny <player>"));
                    return;
                }
                ProxiedPlayer targetDeny = ProxyServer.getInstance().getPlayer(args[1]);
                if (targetDeny == null) {
                    player.sendMessage(CC.translate("&cPlayer not found!"));
                    return;
                }
                friendManager.denyFriendRequest(player, targetDeny);
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&cUsage: /friend remove <player>"));
                    return;
                }
                ProxiedPlayer targetRemove = ProxyServer.getInstance().getPlayer(args[1]);
                if (targetRemove == null) {
                    player.sendMessage(CC.translate("&cPlayer not found!"));
                    return;
                }
                friendManager.removeFriend(player, targetRemove);
                break;
            case "list":
                friendManager.listFriends(player);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(CC.translate("&cUsage: /friend join <player>"));
                    return;
                }
                ProxiedPlayer targetJoin = ProxyServer.getInstance().getPlayer(args[1]);
                if (targetJoin == null) {
                    player.sendMessage(CC.translate("&cPlayer not found!"));
                    return;
                }
                friendManager.joinFriend(player, targetJoin);
                break;
            default:
                player.sendMessage(CC.translate("&b&lNEBULA &7| &3&lFRIENDS"));
                player.sendMessage(CC.translate(""));
                player.sendMessage(CC.translate(" &f⇨ &7/friend list"));
                player.sendMessage(CC.translate(" &f⇨ &7/friend add <player>"));
                player.sendMessage(CC.translate(" &f⇨ &7/friend remove <player>"));
                player.sendMessage(CC.translate(" &f⇨ &7/friend accept <player>"));
                player.sendMessage(CC.translate(" &f⇨ &7/friend deny <player>"));
                player.sendMessage(CC.translate(""));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return Arrays.asList();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                    .filter(cmd -> cmd.startsWith(partial))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && !args[0].equalsIgnoreCase("list")) {
            String partial = args[1].toLowerCase();
            return ProxyServer.getInstance().getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return Arrays.asList();
    }

    private boolean isEnabled() {
        return ConfigManager.getConfig().getBoolean("commands.friend", true);
    }
}