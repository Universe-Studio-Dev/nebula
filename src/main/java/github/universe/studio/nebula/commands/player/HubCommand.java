package github.universe.studio.nebula.commands.player;

import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;
import java.util.Random;

/**
 * @author DanielH131COL
 * @created 17/08/2025
 * @project nebula
 * @file HubCommand
 */

public class HubCommand extends Command {

    private final Nebula plugin;
    private final Random random = new Random();

    public HubCommand(Nebula plugin) {
        super("hub", "", "lobby");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ConfigManager.getMessages().getString("messages.only-players")));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        List<String> hubs = plugin.getConfigManager().getConfig().getStringList("hub-servers");

        if (hubs == null || hubs.isEmpty()) {
            player.sendMessage(new TextComponent(ConfigManager.getMessages().getString("hub.no-hubs")));
            return;
        }

        String hubName = hubs.get(random.nextInt(hubs.size()));
        ServerInfo hub = plugin.getProxy().getServerInfo(hubName);

        if (hub == null) {
            player.sendMessage(new TextComponent(ConfigManager.getMessages().getString("hub.hub-not-found").replace("%hub%", hubName)
            ));
            return;
        }

        if (player.getServer() != null && player.getServer().getInfo().getName().equalsIgnoreCase(hubName)) {
            player.sendMessage(new TextComponent(ConfigManager.getMessages().getString("hub.already-in-hub")));
            return;
        }

        player.connect(hub);
        player.sendMessage(new TextComponent(ConfigManager.getMessages().getString("hub.sending-hub").replace("%hub%", hubName)
        ));
    }
}