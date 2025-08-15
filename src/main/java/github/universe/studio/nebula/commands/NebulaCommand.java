package github.universe.studio.nebula.commands;

import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.utils.CC;
import github.universe.studio.nebula.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DanielH131COL
 * @created 14/08/2025
 * @project nebula
 * @file NebulaCommand
 */
public class NebulaCommand extends Command {
    public NebulaCommand() {
        super("nebula", "nebula.admin", "n");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nebula.admin")) {
            sender.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.no-permission")));
            return;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                Nebula.getConfigManager().load();
                Nebula.getInstance().getAnnouncer().stop();
                Nebula.getInstance().getAnnouncer().start();
                sender.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.reloaded")));
                break;
            case "info":
                sender.sendMessage(CC.translate("&b&lNEBULA"));
                sender.sendMessage(CC.translate(" &7⇨ &fVersion: &a" + Nebula.getInstance().getDescription().getVersion()));
                sender.sendMessage(CC.translate(" &7⇨ &fAuthor: &a" + Nebula.getInstance().getDescription().getAuthor()));
                sender.sendMessage(CC.translate(" &7⇨ &fDescription: &aProxyCore with announcer and stream system and more."));
                break;
            case "addannouncement":
                if (args.length < 3) {
                    sender.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.invalid-usage")));
                    break;
                }
                String name = args[1];
                int interval;
                try {
                    interval = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.invalid-interval")));
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
                Configuration announcements = ConfigManager.getAnnouncements();
                Configuration group = announcements.getSection("announcements." + name);
                group.set("interval", interval);
                group.set("messages", messages);
                Nebula.getConfigManager().saveAnnouncements();
                Nebula.getInstance().getAnnouncer().stop();
                Nebula.getInstance().getAnnouncer().start();
                sender.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.announcement-added").replace("%name%", name)));
                break;
            case "removeannouncement":
                if (args.length != 2) {
                    sender.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.invalid-usage")));
                    break;
                }
                String removeName = args[1];
                Configuration removeAnnouncements = ConfigManager.getAnnouncements();
                if (!removeAnnouncements.contains("announcements." + removeName)) {
                    sender.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.announcement-not-found").replace("%name%", removeName)));
                    break;
                }
                removeAnnouncements.set("announcements." + removeName, null);
                Nebula.getConfigManager().saveAnnouncements();
                Nebula.getInstance().getAnnouncer().stop();
                Nebula.getInstance().getAnnouncer().start();
                sender.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.announcement-removed").replace("%name%", removeName)));
                break;
            default:
                sendHelp(sender);
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(CC.translate("&b&lNEBULA &fCommands:"));
        sender.sendMessage(CC.translate(" &7⇨ &a/nebula reload &7- Reload all configuration files."));
        sender.sendMessage(CC.translate(" &7⇨ &a/nebula info &7- Display plugin information."));
        sender.sendMessage(CC.translate(" &7⇨ &a/nebula addannouncement <name> <interval> <message> | <message> &7- Add a new announcement"));
        sender.sendMessage(CC.translate(" &7⇨ &a/nebula removeannouncement <name> &7- Remove an announcement"));
    }
}