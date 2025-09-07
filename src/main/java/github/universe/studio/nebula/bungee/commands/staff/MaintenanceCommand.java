package github.universe.studio.nebula.bungee.commands.staff;

import github.universe.studio.nebula.bungee.BungeePlugin;
import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 16/08/2025
 * @project nebula
 * @file MaintenanceCommand
 */
public class MaintenanceCommand extends Command implements TabExecutor {

    private final BungeePlugin plugin;

    public MaintenanceCommand(BungeePlugin plugin) {
        super("maintenance", "nebula.admin", "mantenimiento");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nebula.admin")) {
            sender.sendMessage(TextComponent.fromLegacyText(CC.translate(
                    ConfigManager.getMessages().getString("messages.no-permission"))));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(TextComponent.fromLegacyText(CC.translate(
                    ConfigManager.getMessages().getString("maintenance.maintenance-usage"))));
            return;
        }

        String mode = args[0].toLowerCase();
        if (mode.equals("on")) {
            ConfigManager.getConfig().set("maintenance", true);
            plugin.getConfigManager().saveConfig();
            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(CC.translate(
                    ConfigManager.getMessages().getString("maintenance.maintenance-on"))));
        } else if (mode.equals("off")) {
            ConfigManager.getConfig().set("maintenance", false);
            plugin.getConfigManager().saveConfig();
            ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(CC.translate(
                    ConfigManager.getMessages().getString("maintenance.maintenance-off"))));
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(CC.translate(
                    ConfigManager.getMessages().getString("maintenance.maintenance-usage"))));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nebula.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return Arrays.asList("on", "off").stream()
                    .filter(mode -> mode.startsWith(partial))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}