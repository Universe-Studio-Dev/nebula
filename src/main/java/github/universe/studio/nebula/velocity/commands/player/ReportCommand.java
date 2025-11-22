package github.universe.studio.nebula.velocity.commands.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ReportCommand implements SimpleCommand {

    private final VelocityPlugin plugin;
    private final ProxyServer server;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ReportCommand(VelocityPlugin plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!ConfigManager.getConfig().node("commands", "report").getBoolean(true)) return;

        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate(ConfigManager.getMessages().node("messages", "no-console").getString("&cOnly players can use this command."))
                    )
            );
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            player.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                            CC.translate(ConfigManager.getMessages().node("messages", "report-usage").getString("&cUsage: /report <player> <reason>"))
                    )
            );
            return;
        }

        String reported = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String serverName = player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Unknown");

        String playerMsg = ConfigManager.getMessages().node("messages", "report-sent")
                .getString("&aReport sent.")
                .replace("%reported_player%", reported)
                .replace("%message%", reason);
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(playerMsg)));

        List<String> staffLines;
        try {
            staffLines = ConfigManager.getMessages().node("messages", "report-received")
                    .getList(String.class, List.of());
        } catch (Exception e) {
            staffLines = List.of("&cError loading report message.");
        }

        final String finalServerName = serverName;
        Component staffComponent = Component.empty();
        for (String line : staffLines) {
            String formatted = CC.translate(line
                    .replace("%player%", player.getUsername())
                    .replace("%reported_player%", reported)
                    .replace("%server%", serverName)
                    .replace("%message%", reason));
            Component part = LegacyComponentSerializer.legacyAmpersand().deserialize(formatted);
            if (!serverName.equals("Unknown")) {
                part = part.clickEvent(ClickEvent.runCommand("/server " + finalServerName));
            }
            staffComponent = staffComponent.append(part).append(Component.newline());
        }

        Component finalStaffComponent = staffComponent;
        server.getAllPlayers().stream()
                .filter(p -> p.hasPermission("nebula.staff"))
                .forEach(p -> p.sendMessage(finalStaffComponent));

        String url = ConfigManager.getConfig().node("webhooks", "report").getString("");
        if (!url.isEmpty() && !url.contains("REPLACES")) {
            sendDiscordEmbed("report", player.getUsername(), player.getUniqueId().toString(), serverName, reason, reported);
        }
    }

    private void sendDiscordEmbed(String type, String playerName, String uuid, String server, String message, String reported) {
        String url = ConfigManager.getConfig().node("webhooks", type).getString("");
        if (url.isEmpty() || url.contains("REPLACES")) return;

        ConfigurationNode section = ConfigManager.getConfig().node("webhooks", "embed", type);
        if (section.virtual()) return;

        final String finalServer = server;

        JsonObject embed = new JsonObject();
        embed.addProperty("title", section.node("title").getString("New Report"));
        embed.addProperty("color", section.node("color").getInt(15158332));
        embed.addProperty("timestamp", Instant.now().toString());

        String footer = section.node("footer").getString("");
        if (!footer.isEmpty()) {
            JsonObject f = new JsonObject();
            f.addProperty("text", footer);
            embed.add("footer", f);
        }

        String thumb = section.node("thumbnail").getString("").replace("%uuid%", uuid);
        if (!thumb.isEmpty()) {
            JsonObject t = new JsonObject();
            t.addProperty("url", thumb);
            embed.add("thumbnail", t);
        }

        JsonArray fields = new JsonArray();
        try {
            List<? extends ConfigurationNode> fieldNodes = section.node("fields").childrenList();
            for (ConfigurationNode node : fieldNodes) {
                JsonObject field = new JsonObject();
                field.addProperty("name", node.node("name").getString(""));

                String value = node.node("value").getString("")
                        .replace("%player%", playerName)
                        .replace("%uuid%", uuid)
                        .replace("%server%", finalServer)
                        .replace("%message%", message);
                if (reported != null) {
                    value = value.replace("%reported_player%", reported);
                }

                field.addProperty("value", CC.stripColors(value));
                field.addProperty("inline", node.node("inline").getBoolean(false));
                fields.add(field);
            }
        } catch (Exception ignored) {}

        embed.add("fields", fields);

        JsonObject payload = new JsonObject();
        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        payload.add("embeds", embeds);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception ignored) {}
        });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length != 1) return List.of();
        String partial = invocation.arguments()[0].toLowerCase();
        return server.getAllPlayers().stream()
                .map(Player::getUsername)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }
}