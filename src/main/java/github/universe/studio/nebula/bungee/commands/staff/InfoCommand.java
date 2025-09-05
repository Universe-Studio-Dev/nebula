package github.universe.studio.nebula.bungee.commands.staff;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import github.universe.studio.nebula.bungee.utils.ConnectionTracker;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InfoCommand extends Command {

    public InfoCommand() {
        super("info", "nebula.info", "whois");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.no-console")));
            return;
        }

        if (!sender.hasPermission("nebula.info")) {
            sender.sendMessage(CC.translate(ConfigManager.getMessages().getString("messages.no-permission")
            ));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(CC.translate(ConfigManager.getMessages().getString("info.usage")));
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(CC.translate(ConfigManager.getMessages().getString("info.no-online")));
            return;
        }

        UUID uuid = target.getUniqueId();
        String name = target.getName();
        String ip = target.getAddress().getAddress().getHostAddress();
        int ping = target.getPing();
        String server = (target.getServer() != null) ? target.getServer().getInfo().getName() : "unknown";

        String client = "unknown";
        try {
            client = String.valueOf(target.getPendingConnection().getVersion());
        } catch (Exception ignored) { }

        long conectadoDesdeMs = System.currentTimeMillis() - ConnectionTracker.getLoginTime(target.getUniqueId());
        String tiempoConectado = formatearTiempo(conectadoDesdeMs);

        final UUID uuidFinal = uuid;
        final String nameFinal = name;
        final String ipFinal = ip;
        final int pingFinal = ping;
        final String serverFinal = server;
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

            List<String> lines = ConfigManager.getMessages().getStringList("info.format");
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
                player.sendMessage(CC.translate(IridiumColorAPI.process(replaced)));
            }
        });
    }

    private String formatearTiempo(long ms) {
        long segundos = ms / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        minutos %= 60;
        segundos %= 60;
        return String.format("%02dh %02dm %02ds", horas, minutos, segundos);
    }
}