package github.universe.studio.nebula.bungee.commands.staff;

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
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 16/08/2025
 * @project nebula
 * @file BlacklistCommand
 */
public class BlacklistCommand extends Command implements TabExecutor {

    public BlacklistCommand() {
        super("blacklist", "nebula.blacklist");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextComponent.fromLegacyText(CC.translate(
                    ConfigManager.getMessages().getString("blacklist.usage-add"))));
            sender.sendMessage(TextComponent.fromLegacyText(CC.translate(
                    ConfigManager.getMessages().getString("blacklist.usage-remove"))));
            return;
        }

        String action = args[0].toLowerCase();
        String targetName = args[1].toLowerCase();

        if (action.equals("add")) {
            if (args.length < 3) {
                sender.sendMessage(TextComponent.fromLegacyText(CC.translate(
                        ConfigManager.getMessages().getString("blacklist.no-reason"))));
                return;
            }

            String reason = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);

            ConfigManager.getConfig().set("blacklist." + targetName, reason);
            BungeePlugin.getConfigManager().saveConfig();

            sender.sendMessage(TextComponent.fromLegacyText(CC.translate(
                    ConfigManager.getMessages().getString("blacklist.added")
                            .replace("%player%", targetName)
                            .replace("%reason%", reason))));

            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(CC.translate(
                    ConfigManager.getMessages().getString("blacklist.broadcast-add")
                            .replace("%player%", targetName)
                            .replace("%reason%", reason))));

            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetName);
            if (target != null) {
                target.disconnect(TextComponent.fromLegacyText(CC.translate(
                        ConfigManager.getMessages().getString("blacklist.kick-message")
                                .replace("%reason%", reason))));
            }

        } else if (action.equals("remove")) {
            if (ConfigManager.getConfig().contains("blacklist." + targetName)) {
                ConfigManager.getConfig().set("blacklist." + targetName, null);
                BungeePlugin.getConfigManager().saveConfig();

                sender.sendMessage(TextComponent.fromLegacyText(CC.translate(
                        ConfigManager.getMessages().getString("blacklist.removed")
                                .replace("%player%", targetName))));

                ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(CC.translate(
                        ConfigManager.getMessages().getString("blacklist.broadcast-remove")
                                .replace("%player%", targetName))));
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(CC.translate(
                        ConfigManager.getMessages().getString("blacklist.already-removed"))));
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(CC.translate(
                    "&cInvalid action. Use add/remove.")));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nebula.blacklist")) {
            return List.of();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Arrays.asList("add", "remove").stream()
                    .filter(action -> action.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String partial = args[1].toLowerCase();
            return ProxyServer.getInstance().getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}