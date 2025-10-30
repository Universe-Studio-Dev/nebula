package github.universe.studio.nebula.bungee.commands.message;

import github.universe.studio.nebula.bungee.BungeePlugin;
import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

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
public class IgnoreCommand extends Command implements TabExecutor {

    private static final List<String> SUBCOMMANDS = Arrays.asList("add", "remove", "list");

    public IgnoreCommand() {
        super("ignore", null, "ignorar");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!isEnabled()) {
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("messages.no-console", "&cThis command is for players only"))));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {
            sendHelp(player);
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "add":
                if (args.length < 2) {
                    player.sendMessage(new TextComponent(CC.translate(
                            ConfigManager.getMessages().getString("ignore.usage-add", "&cUsage: /ignore add <player>"))));
                    return;
                }
                ProxiedPlayer targetAdd = ProxyServer.getInstance().getPlayer(args[1]);
                if (targetAdd == null || targetAdd.equals(player)) {
                    player.sendMessage(new TextComponent(CC.translate(
                            ConfigManager.getMessages().getString("ignore.not-found", "&cPlayer not found or you cannot ignore yourself"))));
                    return;
                }
                List<String> ignoreListAdd = getIgnoreList(player);
                if (ignoreListAdd.contains(targetAdd.getUniqueId().toString())) {
                    player.sendMessage(new TextComponent(CC.translate(
                            ConfigManager.getMessages().getString("ignore.already-ignored", "&cYou are already ignoring %player%")
                                    .replace("%player%", targetAdd.getName()))));
                    return;
                }
                ignoreListAdd.add(targetAdd.getUniqueId().toString());
                saveIgnoreList(player, ignoreListAdd);
                player.sendMessage(new TextComponent(CC.translate(
                        ConfigManager.getMessages().getString("ignore.added", "&aYou are now ignoring %player%")
                                .replace("%player%", targetAdd.getName()))));
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(new TextComponent(CC.translate(
                            ConfigManager.getMessages().getString("ignore.usage-remove", "&cUsage: /ignore remove <player>"))));
                    return;
                }
                ProxiedPlayer targetRemove = ProxyServer.getInstance().getPlayer(args[1]);
                if (targetRemove == null) {
                    player.sendMessage(new TextComponent(CC.translate(
                            ConfigManager.getMessages().getString("ignore.not-found", "&cPlayer not found"))));
                    return;
                }
                List<String> ignoreListRemove = getIgnoreList(player);
                if (!ignoreListRemove.contains(targetRemove.getUniqueId().toString())) {
                    player.sendMessage(new TextComponent(CC.translate(
                            ConfigManager.getMessages().getString("ignore.not-ignored", "&cYou are not ignoring %player%")
                                    .replace("%player%", targetRemove.getName()))));
                    return;
                }
                ignoreListRemove.remove(targetRemove.getUniqueId().toString());
                saveIgnoreList(player, ignoreListRemove);
                player.sendMessage(new TextComponent(CC.translate(
                        ConfigManager.getMessages().getString("ignore.removed", "&aYou are no longer ignoring %player%")
                                .replace("%player%", targetRemove.getName()))));
                break;
            case "list":
                List<String> ignoreList = getIgnoreList(player);
                if (ignoreList.isEmpty()) {
                    player.sendMessage(new TextComponent(CC.translate(
                            ConfigManager.getMessages().getString("ignore.list-empty", "&7You are not ignoring anyone"))));
                    return;
                }
                player.sendMessage(new TextComponent(CC.translate(
                        ConfigManager.getMessages().getString("ignore.list-header", "&b&lIgnored Players:"))));
                for (String uuid : ignoreList) {
                    ProxiedPlayer ignoredPlayer = ProxyServer.getInstance().getPlayer(UUID.fromString(uuid));
                    String name = ignoredPlayer != null ? ignoredPlayer.getName() : uuid;
                    player.sendMessage(new TextComponent(CC.translate(
                            ConfigManager.getMessages().getString("ignore.list-entry", "&7- %player%").replace("%player%", name))));
                }
                break;
            default:
                sendHelp(player);
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                    .filter(cmd -> cmd.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            String partial = args[1].toLowerCase();
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

    private void saveIgnoreList(ProxiedPlayer player, List<String> ignoreList) {
        try {
            ConfigManager.getConfig().set("ignore." + player.getUniqueId().toString(), ignoreList);
            BungeePlugin.getConfigManager().load();
        } catch (Exception e) {
            player.sendMessage(new TextComponent(CC.translate("&cError saving ignore list: " + e.getMessage())));
        }
    }

    private void sendHelp(ProxiedPlayer player) {
        player.sendMessage(new TextComponent(CC.translate("&b&lNEBULA &7| &3&lIGNORE")));
        player.sendMessage(new TextComponent(CC.translate(" &f⇨ &7/ignore add <player> &f- Ignore messages from a player")));
        player.sendMessage(new TextComponent(CC.translate(" &f⇨ &7/ignore remove <player> &f- Stop ignoring a player")));
        player.sendMessage(new TextComponent(CC.translate(" &f⇨ &7/ignore list &f- List all ignored players")));
    }

    private boolean isEnabled() {
        return ConfigManager.getConfig().getBoolean("commands.ignore", true);
    }
}