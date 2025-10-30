package github.universe.studio.nebula.bungee.commands.player;

import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author DanielH131COL
 * @created 26/08/2025
 * @project Nebula
 * @file PingCommand
 */
public class PingCommand extends Command {

    public PingCommand() {
        super("ping");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!isEnabled()) {
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            CC.console(ConfigManager.getMessages().getString("no-console"));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {
            int ping = player.getPing();
            player.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.ping").replace("%ping%", String.valueOf(ping))));
        } else {
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
            if (target == null || !target.isConnected()) {
                sender.sendMessage(CC.translate("&cThat player is not online."));
                return;
            }

            int ping = target.getPing();
            player.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.ping").replace("%ping%", String.valueOf(ping))));
        }
    }

    private boolean isEnabled() {
        return ConfigManager.getConfig().getBoolean("commands.ping", true);
    }
}