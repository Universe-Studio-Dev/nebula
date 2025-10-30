package github.universe.studio.nebula.velocity.commands.staff;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import github.universe.studio.nebula.velocity.utils.ConnectionTracker;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file InfoCommand
 */
public class InfoCommand implements SimpleCommand {

    private final ProxyServer server;

    public InfoCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!isEnabled()) {
            invocation.source().sendMessage(
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(null)
            );
            return;
        }
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "no-console").getString("&cThis command is for players only"))
            ));
            return;
        }

        if (!sender.hasPermission("nebula.info")) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "no-permission").getString("&cYou do not have permission"))
            ));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("info", "usage").getString("&cUsage: /info <player>"))
            ));
            return;
        }

        Player target = server.getPlayer(args[0]).orElse(null);
        if (target == null) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("info", "no-online").getString("&cPlayer not online"))
            ));
            return;
        }

        UUID uuid = target.getUniqueId();
        String name = target.getUsername();
        String ip = target.getRemoteAddress().getAddress().getHostAddress();
        int ping = Math.toIntExact(target.getPing());
        String serverName = target.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("unknown");

        String client = "unknown";
        try {
            client = String.valueOf(target.getProtocolVersion().getProtocol());
        } catch (Exception ignored) { }

        long conectadoDesdeMs = System.currentTimeMillis() - ConnectionTracker.getLoginTime(target.getUniqueId());
        String tiempoConectado = formatearTiempo(conectadoDesdeMs);

        final UUID uuidFinal = uuid;
        final String nameFinal = name;
        final String ipFinal = ip;
        final int pingFinal = ping;
        final String serverFinal = serverName;
        final String clientFinal = client;
        final String tiempoConectadoFinal = tiempoConectado;

        CompletableFuture.runAsync(() -> {
            String country = "unknown";
            try {
                URL url = new URL("http://ip-api.com/json/" + ipFinal + "?fields=country");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setRequestMethod("GET");

                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(new InputStreamReader(connection.getInputStream()));
                country = obj.get("country").toString();
            } catch (Exception ignored) { }

            List<String> lines;
            try {
                lines = ConfigManager.getMessages().node("info", "format").getList(String.class, List.of());
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
            for (String line : lines) {
                String replaced = line
                        .replace("{player}", nameFinal)
                        .replace("{uuid}", uuidFinal.toString())
                        .replace("{ip}", ipFinal)
                        .replace("{country}", country)
                        .replace("{server}", serverFinal)
                        .replace("{ping}", String.valueOf(pingFinal))
                        .replace("{client}", clientFinal)
                        .replace("{time}", tiempoConectadoFinal);
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(replaced)));
            }
        });
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!sender.hasPermission("nebula.info")) {
            return List.of();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    private String formatearTiempo(long ms) {
        long segundos = ms / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        minutos %= 60;
        segundos %= 60;
        return String.format("%02dh %02dm %02ds", horas, minutos, segundos);
    }

    private boolean isEnabled() {
        return ConfigManager.getConfig().node("commands", "info").getBoolean(true);
    }
}