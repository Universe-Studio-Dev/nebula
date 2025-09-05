package github.universe.studio.nebula.bungee.commands.player;

import github.universe.studio.nebula.bungee.BungeePlugin;
import github.universe.studio.nebula.bungee.BungeePlugin;
import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

/**
 * @author DanielH131COL
 * @created 16/08/2025
 * @project nebula
 * @file HelpopCommand
 */
public class HelpopCommand extends Command {

    private final BungeePlugin plugin;

    public HelpopCommand(BungeePlugin plugin) {
        super("helpop", null, "ayuda");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.no-console"))));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length < 1) {
            player.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.helpop-usage"))));
            return;
        }

        String message = String.join(" ", args);
        String server = player.getServer() != null ? player.getServer().getInfo().getName() : "Unknown";

        String playerMessage = CC.translate(ConfigManager.getMessages().getString("messages.helpop-sent")
                .replace("%message%", message));
        player.sendMessage(new TextComponent(playerMessage));

        List<String> staffMessages = ConfigManager.getMessages().getStringList("messages.helpop-received");
        TextComponent staffText = new TextComponent();
        for (String line : staffMessages) {
            String formattedLine = CC.translate(line.replace("%player%", player.getName())
                    .replace("%server%", server)
                    .replace("%message%", message));
            TextComponent lineComponent = new TextComponent(formattedLine + "\n");
            if (!server.equals("Unknown")) {
                lineComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server));
            }
            staffText.addExtra(lineComponent);
        }

        for (ProxiedPlayer onlinePlayer : plugin.getProxy().getPlayers()) {
            if (onlinePlayer.hasPermission("nubula.staff")) {
                onlinePlayer.sendMessage(staffText);
            }
        }
    }
}