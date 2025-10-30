package github.universe.studio.nebula.bungee.commands.player;

import github.universe.studio.nebula.bungee.BungeePlugin;
import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 16/08/2025
 * @project nebula
 * @file ReportCommand
 */
public class ReportCommand extends Command implements TabExecutor {
    private final BungeePlugin plugin;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ReportCommand(BungeePlugin plugin) {
        super("report", null, "reporte", "reportar");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!isEnabled()) {
            sender.sendMessage(new TextComponent(CC.translate("&cThis command is disabled.")));
            return;
        }
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
            if (onlinePlayer.hasPermission("nebula.staff")) {
                onlinePlayer.sendMessage(staffText);
            }
        }

        String webhookUrl = ConfigManager.getConfig().getString("webhooks.report", "");
        if (!webhookUrl.isEmpty() && !webhookUrl.contains("REPLACES")) {
            String discordMessage = String.join("\n", staffMessages)
                    .replace("%player%", player.getName())
                    .replace("%reported_player%", reportedPlayer)
                    .replace("%server%", server)
                    .replace("%message%", message);
            sendWebhook(webhookUrl, discordMessage);
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return List.of();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return plugin.getProxy().getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    private void sendWebhook(String url, String content) {
        executor.submit(() -> {
            try {
                String json = "{\"content\": \"" + content.replace("\"", "\\\"") + "\"}";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                plugin.getLogger().warning("Error sending report webhook: " + e.getMessage());
            }
        });
    }

    private boolean isEnabled() {
        return ConfigManager.getConfig().getBoolean("commands.report", true);
    }
}