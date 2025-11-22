package github.universe.studio.nebula.bungee.commands.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.universe.studio.nebula.bungee.BungeePlugin;
import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.config.Configuration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ReportCommand extends Command implements TabExecutor {
    private final BungeePlugin plugin;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ReportCommand(BungeePlugin plugin) {
        super("report", null, "reporte", "reportar");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!ConfigManager.getConfig().getBoolean("commands.report", true)) return;
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.no-console"))));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.report-usage"))));
            return;
        }

        String reported = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String server = player.getServer() != null ? player.getServer().getInfo().getName() : "Unknown";

        player.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.report-sent")
                .replace("%reported_player%", reported).replace("%message%", reason))));

        List<String> staffLines = ConfigManager.getMessages().getStringList("messages.report-received");
        TextComponent staffComponent = new TextComponent();
        for (String line : staffLines) {
            String formatted = CC.translate(line
                    .replace("%player%", player.getName())
                    .replace("%reported_player%", reported)
                    .replace("%server%", server)
                    .replace("%message%", reason));
            TextComponent part = new TextComponent(formatted + "\n");
            if (!server.equals("Unknown")) {
                part.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server));
            }
            staffComponent.addExtra(part);
        }
        plugin.getProxy().getPlayers().stream()
                .filter(p -> p.hasPermission("nebula.staff"))
                .forEach(p -> p.sendMessage(staffComponent));

        String url = ConfigManager.getConfig().getString("webhooks.report", "");
        if (!url.isEmpty() && !url.contains("REPLACES")) {
            sendDiscordEmbed("report", player.getName(), player.getUniqueId().toString(), server, reason, reported);
        }
    }

    private void sendDiscordEmbed(String type, String playerName, String uuid, String server, String message, String reported) {
        String url = ConfigManager.getConfig().getString("webhooks." + type, "");
        if (url.isEmpty() || url.contains("REPLACES")) return;

        Configuration section = ConfigManager.getConfig().getSection("webhooks.embed." + type);
        if (section == null) return;

        JsonObject embed = new JsonObject();
        embed.addProperty("title", section.getString("title"));
        embed.addProperty("color", section.getInt("color"));
        embed.addProperty("timestamp", Instant.now().toString());

        String footer = section.getString("footer");
        if (footer != null && !footer.isEmpty()) {
            JsonObject f = new JsonObject();
            f.addProperty("text", footer);
            embed.add("footer", f);
        }

        String thumb = section.getString("thumbnail", "").replace("%uuid%", uuid);
        if (!thumb.isEmpty()) {
            JsonObject t = new JsonObject();
            t.addProperty("url", thumb);
            embed.add("thumbnail", t);
        }

        JsonArray fields = new JsonArray();
        List<?> fieldList = section.getList("fields", List.of());
        for (Object obj : fieldList) {
            if (!(obj instanceof java.util.Map<?, ?> map)) continue;
            JsonObject field = new JsonObject();
            field.addProperty("name", (String) map.get("name"));

            String value = ((String) map.get("value"))
                    .replace("%player%", playerName)
                    .replace("%uuid%", uuid)
                    .replace("%server%", server)
                    .replace("%message%", message);
            if (reported != null) value = value.replace("%reported_player%", reported);

            field.addProperty("value", CC.stripColors(value));
            field.addProperty("inline", map.containsKey("inline") && (Boolean) map.get("inline"));
            fields.add(field);
        }
        embed.add("fields", fields);

        JsonObject payload = new JsonObject();
        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        payload.add("embeds", embeds);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception ignored) {}
        });
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer) || args.length != 1) return List.of();
        String partial = args[0].toLowerCase();
        return plugin.getProxy().getPlayers().stream()
                .map(ProxiedPlayer::getName)
                .filter(name -> name.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
    }
}