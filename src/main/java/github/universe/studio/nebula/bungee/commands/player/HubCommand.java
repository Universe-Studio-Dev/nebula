package github.universe.studio.nebula.bungee.commands.player;

import github.universe.studio.nebula.bungee.BungeePlugin;
import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

/**
 * @author DanielH131COL
 * @created 17/08/2025
 * @project nebula
 * @file HubCommand
 */
public class HubCommand extends Command {

    private final BungeePlugin plugin;

    public HubCommand(BungeePlugin plugin) {
        super("hub", "", "lobby");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!isEnabled()) {
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("messages.only-players")
            )));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        List<String> hubs = plugin.getConfigManager().getConfig().getStringList("hub");

        if (hubs == null || hubs.isEmpty()) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("hub.no-hubs")
            )));
            return;
        }

        String hubName = null;
        for (String name : hubs) {
            if (plugin.getProxy().getServerInfo(name) != null) {
                hubName = name;
                break;
            }
        }

        if (hubName == null) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("hub.hub-not-found").replace("%hub%", "N/A")
            )));
            return;
        }

        if (player.getServer() != null && player.getServer().getInfo().getName().equalsIgnoreCase(hubName)) {
            player.sendMessage(new TextComponent(CC.translate(
                    ConfigManager.getMessages().getString("hub.already-in-hub")
            )));
            return;
        }

        ServerInfo hub = plugin.getProxy().getServerInfo(hubName);
        player.connect(hub);
        player.sendMessage(new TextComponent(CC.translate(
                ConfigManager.getMessages().getString("hub.sending-hub").replace("%hub%", hubName)
        )));
    }

    private boolean isEnabled() {
        return ConfigManager.getConfig().getBoolean("commands.hub", true);
    }
}