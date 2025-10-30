package github.universe.studio.nebula.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.others.FriendManager;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 05/09/2025
 * @project nebula
 * @file FriendCommand
 */
public class FriendCommand implements SimpleCommand {
    private final FriendManager friendManager;
    private final ProxyServer server;
    private static final List<String> SUBCOMMANDS = Arrays.asList("add", "remove", "list", "join", "accept", "deny");

    public FriendCommand(FriendManager friendManager, ProxyServer server) {
        this.friendManager = friendManager;
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
        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(CC.translateToComponent("<red>This command is for players only!"));
            return;
        }
        Player player = (Player) invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            player.sendMessage(CC.translateToComponent("<gradient:blue:aqua>NEBULA</gradient> <gray>| <aqua>FRIENDS"));
            player.sendMessage(CC.translateToComponent(""));
            player.sendMessage(CC.translateToComponent(" <white>⇨ <gray>/friend list"));
            player.sendMessage(CC.translateToComponent(" <white>⇨ <gray>/friend add <player>"));
            player.sendMessage(CC.translateToComponent(" <white>⇨ <gray>/friend remove <player>"));
            player.sendMessage(CC.translateToComponent(" <white>⇨ <gray>/friend accept <player>"));
            player.sendMessage(CC.translateToComponent(" <white>⇨ <gray>/friend deny <player>"));
            player.sendMessage(CC.translateToComponent(""));
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "add":
                if (args.length < 2) {
                    player.sendMessage(CC.translateToComponent("<red>Usage: /friend add <player>"));
                    return;
                }
                server.getPlayer(args[1]).ifPresentOrElse(
                        target -> {
                            try {
                                friendManager.sendFriendRequest(player, target);
                            } catch (Exception e) {
                                player.sendMessage(CC.translateToComponent("<red>An error occurred: " + e.getMessage()));
                            }
                        },
                        () -> player.sendMessage(CC.translateToComponent("<red>Player not found!"))
                );
                break;
            case "accept":
                if (args.length < 2) {
                    player.sendMessage(CC.translateToComponent("<red>Usage: /friend accept <player>"));
                    return;
                }
                server.getPlayer(args[1]).ifPresentOrElse(
                        target -> {
                            try {
                                friendManager.acceptFriendRequest(player, target);
                            } catch (Exception e) {
                                player.sendMessage(CC.translateToComponent("<red>An error occurred: " + e.getMessage()));
                            }
                        },
                        () -> player.sendMessage(CC.translateToComponent("<red>Player not found!"))
                );
                break;
            case "deny":
                if (args.length < 2) {
                    player.sendMessage(CC.translateToComponent("<red>Usage: /friend deny <player>"));
                    return;
                }
                server.getPlayer(args[1]).ifPresentOrElse(
                        target -> {
                            try {
                                friendManager.denyFriendRequest(player, target);
                            } catch (Exception e) {
                                player.sendMessage(CC.translateToComponent("<red>An error occurred: " + e.getMessage()));
                            }
                        },
                        () -> player.sendMessage(CC.translateToComponent("<red>Player not found!"))
                );
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(CC.translateToComponent("<red>Usage: /friend remove <player>"));
                    return;
                }
                server.getPlayer(args[1]).ifPresentOrElse(
                        target -> {
                            try {
                                friendManager.removeFriend(player, target);
                            } catch (Exception e) {
                                player.sendMessage(CC.translateToComponent("<red>An error occurred: " + e.getMessage()));
                            }
                        },
                        () -> player.sendMessage(CC.translateToComponent("<red>Player not found!"))
                );
                break;
            case "list":
                friendManager.listFriends(player);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(CC.translateToComponent("<red>Usage: /friend join <player>"));
                    return;
                }
                server.getPlayer(args[1]).ifPresentOrElse(
                        target -> friendManager.joinFriend(player, target),
                        () -> player.sendMessage(CC.translateToComponent("<red>Player not found!"))
                );
                break;
            default:
                player.sendMessage(CC.translateToComponent("<gradient:blue:aqua>NEBULA</gradient> <gray>| <aqua>FRIENDS"));
                player.sendMessage(CC.translateToComponent(""));
                player.sendMessage(CC.translateToComponent(" <white>⇨ <gray>/friend list"));
                player.sendMessage(CC.translateToComponent(" <white>⇨ <gray>/friend add <player>"));
                player.sendMessage(CC.translateToComponent(" <white>⇨ <gray>/friend remove <player>"));
                player.sendMessage(CC.translateToComponent(" <white>⇨ <gray>/friend accept <player>"));
                player.sendMessage(CC.translateToComponent(" <white>⇨ <gray>/friend deny <player>"));
                player.sendMessage(CC.translateToComponent(""));
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            return CompletableFuture.completedFuture(Arrays.asList());
        }

        String[] args = invocation.arguments();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return CompletableFuture.completedFuture(
                    SUBCOMMANDS.stream()
                            .filter(cmd -> cmd.startsWith(partial))
                            .collect(Collectors.toList())
            );
        }

        if (args.length == 2 && !args[0].equalsIgnoreCase("list")) {
            String partial = args[1].toLowerCase();
            return CompletableFuture.completedFuture(
                    server.getAllPlayers().stream()
                            .map(Player::getUsername)
                            .filter(name -> name.toLowerCase().startsWith(partial))
                            .collect(Collectors.toList())
            );
        }

        return CompletableFuture.completedFuture(Arrays.asList());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    private boolean isEnabled() {
        return ConfigManager.getConfig().node("commands", "friend").getBoolean(true);
    }
}