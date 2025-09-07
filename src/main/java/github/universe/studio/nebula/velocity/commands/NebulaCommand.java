package github.universe.studio.nebula.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.listeners.Announcer;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.velocitypowered.api.plugin.PluginContainer;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file NebulaCommand
 */
public class NebulaCommand implements SimpleCommand {

    private final VelocityPlugin plugin;
    private final ProxyServer server;
    private final Announcer announcer;
    private final PluginContainer pluginContainer;

    public NebulaCommand(VelocityPlugin plugin, ProxyServer server, Announcer announcer, PluginContainer pluginContainer) {
        this.plugin = plugin;
        this.server = server;
        this.announcer = announcer;
        this.pluginContainer = pluginContainer;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!sender.hasPermission("nebula.admin")) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "no-permission").getString("&cYou do not have permission"))
            ));
            return;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                ConfigManager.load();
                announcer.stop();
                announcer.start();
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        CC.translate("&aFiles reloaded successfully.")
                ));
                break;
            case "info":
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("&b&lNEBULA")));
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &7⇨ &fVersion: &a" + pluginContainer.getDescription().getVersion().orElse("Unknown"))));
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &7⇨ &fAuthor: &a" + pluginContainer.getDescription().getAuthors())));
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &7⇨ &fDescription: &aProxyCore with announcer and stream system and more.")));
                break;
            case "addannouncement":
                if (args.length < 3) {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate(ConfigManager.getMessages().node("messages", "invalid-usage").getString("&cUsage: /nebula addannouncement <name> <interval> <message> | <message>"))
                    ));
                    break;
                }
                String name = args[1];
                int interval;
                try {
                    interval = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate(ConfigManager.getMessages().node("messages", "invalid-interval").getString("&cInvalid interval"))
                    ));
                    break;
                }
                List<String> messages = new ArrayList<>();
                StringBuilder message = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    if (args[i].equals("|")) {
                        messages.add(message.toString().trim());
                        message = new StringBuilder();
                    } else {
                        message.append(args[i]).append(" ");
                    }
                }
                messages.add(message.toString().trim());
                ConfigurationNode announcements = ConfigManager.getAnnouncements();
                try {
                    announcements.node("announcements", name, "interval").set(interval);
                    announcements.node("announcements", name, "messages").set(messages);
                    ConfigManager.saveAnnouncements();
                    announcer.stop();
                    announcer.start();
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate(ConfigManager.getMessages().node("messages", "announcement-added").getString("&aAnnouncement %name% added").replace("%name%", name))
                    ));
                } catch (SerializationException e) {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate("&cError adding announcement: " + e.getMessage())
                    ));
                }
                break;
            case "removeannouncement":
                if (args.length != 2) {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate(ConfigManager.getMessages().node("messages", "invalid-usage").getString("&cUsage: /nebula removeannouncement <name>"))
                    ));
                    break;
                }
                String removeName = args[1];
                ConfigurationNode removeAnnouncements = ConfigManager.getAnnouncements();
                if (removeAnnouncements.node("announcements", removeName).empty()) {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate(ConfigManager.getMessages().node("messages", "announcement-not-found").getString("&cAnnouncement %name% not found").replace("%name%", removeName))
                    ));
                    break;
                }
                try {
                    removeAnnouncements.node("announcements", removeName).set(null);
                    ConfigManager.saveAnnouncements();
                    announcer.stop();
                    announcer.start();
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate(ConfigManager.getMessages().node("messages", "announcement-removed").getString("&aAnnouncement %name% removed").replace("%name%", removeName))
                    ));
                } catch (SerializationException e) {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate("&cError removing announcement: " + e.getMessage())
                    ));
                }
                break;
            case "captcha":
                ConfigurationNode config = ConfigManager.getConfig();
                boolean currentCaptchaState = config.node("captcha", "enabled").getBoolean(true);
                try {
                    config.node("captcha", "enabled").set(!currentCaptchaState);
                    ConfigManager.saveConfig();

                    if (!currentCaptchaState) {
                        for (Player player : server.getAllPlayers()) {
                            plugin.getCaptchaManager().clearPlayer(player);
                            plugin.getCaptchaManager().sendToLobby(player);
                        }
                    }
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate("&aCaptcha has been " + (!currentCaptchaState ? "enabled" : "disabled") + ".")
                    ));
                } catch (SerializationException e) {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate("&cError toggling captcha: " + e.getMessage())
                    ));
                }
                break;
            default:
                sendHelp(sender);
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> completions = new ArrayList<>();

        if (!invocation.source().hasPermission("nebula.admin")) {
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "info", "addannouncement", "removeannouncement", "captcha"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("removeannouncement")) {
            ConfigurationNode announcements = ConfigManager.getAnnouncements();
            if (!announcements.node("announcements").empty()) {
                completions.addAll(announcements.node("announcements").childrenMap().keySet().stream()
                        .map(Object::toString)
                        .toList());
            }
        }

        return completions;
    }

    private void sendHelp(CommandSource sender) {
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("&b&lNEBULA &fCommands:")));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &7⇨ &a/nebula reload &7- Reload all configuration files.")));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &7⇨ &a/nebula info &7- Display plugin information.")));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &7⇨ &a/nebula addannouncement <name> <interval> <message> | <message> &7- Add a new announcement")));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &7⇨ &a/nebula removeannouncement <name> &7- Remove an announcement")));
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(" &7⇨ &a/nebula captcha &7- Toggle captcha system on/off")));
    }
}