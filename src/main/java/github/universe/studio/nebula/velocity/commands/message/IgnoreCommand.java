package github.universe.studio.nebula.velocity.commands.message;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 07/09/2025
 * @project nebula
 * @file IgnoreCommand
 */
public class IgnoreCommand implements SimpleCommand {

    private final ProxyServer server;
    private static final List<String> SUBCOMMANDS = Arrays.asList("add", "remove", "list");

    public IgnoreCommand(ProxyServer server) {
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
                    CC.translate(ConfigManager.getMessages().node("messages", "no-console").getString("&cThis command is for players only"))));
            return;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "add":
                if (args.length < 2) {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                            ConfigManager.getMessages().node("ignore", "usage-add").getString("&cUsage: /ignore add <player>"))));
                    return;
                }
                Player targetAdd = server.getPlayer(args[1]).orElse(null);
                if (targetAdd == null || targetAdd.equals(player)) {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                            ConfigManager.getMessages().node("ignore", "not-found").getString("&cPlayer not found or you cannot ignore yourself"))));
                    return;
                }
                List<String> ignoreListAdd = getIgnoreList(player);
                if (ignoreListAdd.contains(targetAdd.getUniqueId().toString())) {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                            ConfigManager.getMessages().node("ignore", "already-ignored").getString("&cYou are already ignoring %player%")
                                    .replace("%player%", targetAdd.getUsername()))));
                    return;
                }
                ignoreListAdd.add(targetAdd.getUniqueId().toString());
                saveIgnoreList(player, ignoreListAdd);
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("ignore", "added").getString("&aYou are now ignoring %player%")
                                .replace("%player%", targetAdd.getUsername()))));
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                            ConfigManager.getMessages().node("ignore", "usage-remove").getString("&cUsage: /ignore remove <player>"))));
                    return;
                }
                Player targetRemove = server.getPlayer(args[1]).orElse(null);
                if (targetRemove == null) {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                            ConfigManager.getMessages().node("ignore", "not-found").getString("&cPlayer not found"))));
                    return;
                }
                List<String> ignoreListRemove = getIgnoreList(player);
                if (!ignoreListRemove.contains(targetRemove.getUniqueId().toString())) {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                            ConfigManager.getMessages().node("ignore", "not-ignored").getString("&cYou are not ignoring %player%")
                                    .replace("%player%", targetRemove.getUsername()))));
                    return;
                }
                ignoreListRemove.remove(targetRemove.getUniqueId().toString());
                saveIgnoreList(player, ignoreListRemove);
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("ignore", "removed").getString("&aYou are no longer ignoring %player%")
                                .replace("%player%", targetRemove.getUsername()))));
                break;
            case "list":
                List<String> ignoreList = getIgnoreList(player);
                if (ignoreList.isEmpty()) {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                            ConfigManager.getMessages().node("ignore", "list-empty").getString("&7You are not ignoring anyone"))));
                    return;
                }
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                        ConfigManager.getMessages().node("ignore", "list-header").getString("&b&lIgnored Players:"))));
                for (String uuid : ignoreList) {
                    Player ignoredPlayer = server.getPlayer(UUID.fromString(uuid)).orElse(null);
                    String name = ignoredPlayer != null ? ignoredPlayer.getUsername() : uuid;
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(
                            ConfigManager.getMessages().node("ignore", "list-entry").getString("&7- %player%").replace("%player%", name))));
                }
                break;
            default:
                sendHelp(player);
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                    .filter(cmd -> cmd.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            String partial = args[1].toLowerCase();
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private List<String> getIgnoreList(Player player) {
        try {
            return ConfigManager.getConfig().node("ignore", player.getUniqueId().toString()).getList(String.class, Collections.emptyList());
        } catch (SerializationException e) {
            return Collections.emptyList();
        }
    }

    private void saveIgnoreList(Player player, List<String> ignoreList) {
        try {
            ConfigManager.getConfig().node("ignore", player.getUniqueId().toString()).setList(String.class, ignoreList);
            ConfigManager.saveConfig();
        } catch (SerializationException e) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate("&cError saving ignore list: " + e.getMessage())));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("&b&lNEBULA &7| &3&lIGNORE")));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &f⇨ &7/ignore add <player> &f- Ignore messages from a player")));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &f⇨ &7/ignore remove <player> &f- Stop ignoring a player")));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &f⇨ &7/ignore list &f- List all ignored players")));
    }

    private boolean isEnabled() {
        return ConfigManager.getConfig().node("commands", "ignore").getBoolean(true);
    }
}