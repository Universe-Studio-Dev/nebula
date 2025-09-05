package github.universe.studio.nebula.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.listeners.PlatformDetector;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.List;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file StreamCommand
 */
public class StreamCommand implements SimpleCommand {

    private final VelocityPlugin plugin;
    private final ProxyServer server;
    private final PlatformDetector platformDetector;
    private final HashMap<String, Long> cooldowns;

    public StreamCommand(VelocityPlugin plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
        this.platformDetector = new PlatformDetector();
        this.cooldowns = new HashMap<>();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!(sender instanceof Player)) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "no-console").getString("&cThis command is for players only"))
            ));
            return;
        }

        Player player = (Player) sender;
        ConfigurationNode config = ConfigManager.getConfig();
        long cooldownTime = config.node("stream", "cooldown").getLong(5 * 60 * 1000);

        long currentTime = System.currentTimeMillis();
        Long lastUsed = cooldowns.get(player.getUniqueId().toString());

        if (lastUsed != null && (currentTime - lastUsed) < cooldownTime) {
            long timeLeftMillis = cooldownTime - (currentTime - lastUsed);
            long minutes = timeLeftMillis / (60 * 1000);
            long seconds = (timeLeftMillis % (60 * 1000)) / 1000;
            String timeLeft = (minutes > 0 ? minutes + "m " : "") + (seconds > 0 ? seconds + "s" : "");
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "stream-cooldown").getString("&cPlease wait %time% before using this command again").replace("%time%", timeLeft))
            ));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "stream-usage").getString("&cUsage: /stream <link>"))
            ));
            return;
        }

        String link = args[0];
        String platform = platformDetector.detectPlatform(link);
        if (platform == null) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "stream-invalid-platform").getString("&cInvalid streaming platform"))
            ));
            return;
        }

        String platformColor = platformDetector.getPlatformColor(platform);
        String platformIcon = platformDetector.getPlatformIcon(platform);
        List<String> format = null;
        try {
            format = config.node("stream", "format").getList(String.class, List.of());
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }

        Component message = Component.empty();
        for (String line : format) {
            String formattedLine = line.replace("%platform_color%", platformColor)
                    .replace("%platform_icon%", platformIcon)
                    .replace("%platform%", platform.toUpperCase())
                    .replace("%player%", player.getUsername())
                    .replace("%link%", link);
            Component lineComponent;
            if (line.contains("%link%")) {
                lineComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(formattedLine.replace("%link%", link)))
                        .clickEvent(ClickEvent.openUrl(link))
                        .hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(
                                CC.translate(config.node("stream", "link-hover").getString("&eClick to open the link"))
                        )));
            } else {
                lineComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate(formattedLine));
            }
            message = message.append(lineComponent).append(Component.newline());
        }

        for (Player p : server.getAllPlayers()) {
            p.sendMessage(message);
        }

        cooldowns.put(player.getUniqueId().toString(), currentTime);
    }
}