package github.universe.studio.nebula.commands.player;

import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.utils.CC;
import github.universe.studio.nebula.utils.ConfigManager;
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
 * @file ReportCommand
 */
public class ReportCommand extends Command {

    private final Nebula plugin;

    public ReportCommand(Nebula plugin) {
        super("report", null, "reporte");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.no-console"))));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length < 2) {
            player.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.report-usage"))));
            return;
        }

        String reportedPlayer = args[0];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        String server = player.getServer() != null ? player.getServer().getInfo().getName() : "Unknown";

        String playerMessage = CC.translate(ConfigManager.getMessages().getString("messages.report-sent")
                .replace("%reported_player%", reportedPlayer)
                .replace("%message%", message));
        player.sendMessage(new TextComponent(playerMessage));

        List<String> staffMessages = ConfigManager.getMessages().getStringList("messages.report-received");
        TextComponent staffText = new TextComponent();
        for (String line : staffMessages) {
            String formattedLine = CC.translate(line.replace("%player%", player.getName())
                    .replace("%reported_player%", reportedPlayer)
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