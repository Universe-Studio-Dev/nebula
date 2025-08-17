package github.universe.studio.nebula.commands;

import github.universe.studio.nebula.listeners.PlatformDetector;
import github.universe.studio.nebula.utils.CC;
import github.universe.studio.nebula.utils.ConfigManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.HashMap;
import java.util.List;

/**
 * @author DanielH131COL
 * @created 14/08/2025
 * @project nebula
 * @file StreamCommand
 */
public class StreamCommand extends Command {

    private final Plugin plugin;
    private final PlatformDetector platformDetector;
    private final HashMap<String, Long> cooldowns;

    public StreamCommand(Plugin plugin) {
        super("stream", "nebula.stream");
        this.plugin = plugin;
        this.platformDetector = new PlatformDetector();
        this.cooldowns = new HashMap<>();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.no-console"))));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        Configuration config = ConfigManager.getConfig();
        long cooldownTime = config.getLong("stream.cooldown", 5 * 60 * 1000);

        long currentTime = System.currentTimeMillis();
        Long lastUsed = cooldowns.get(player.getUniqueId().toString());

        if (lastUsed != null && (currentTime - lastUsed) < cooldownTime) {
            long timeLeftMillis = cooldownTime - (currentTime - lastUsed);
            long minutes = timeLeftMillis / (60 * 1000);
            long seconds = (timeLeftMillis % (60 * 1000)) / 1000;
            String timeLeft = (minutes > 0 ? minutes + "m " : "") + (seconds > 0 ? seconds + "s" : "");
            player.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.stream-cooldown").replace("%time%", timeLeft))));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.stream-usage"))));
            return;
        }

        String link = args[0];
        String platform = platformDetector.detectPlatform(link);
        if (platform == null) {
            player.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.stream-invalid-platform"))));
            return;
        }

        String platformColor = platformDetector.getPlatformColor(platform);
        String platformIcon = platformDetector.getPlatformIcon(platform);
        List<String> format = CC.translate(ConfigManager.getConfig().getStringList("stream.format"));

        TextComponent message = new TextComponent();
        for (String line : format) {
            String formattedLine = line.replace("%platform_color%", platformColor)
                    .replace("%platform_icon%", platformIcon)
                    .replace("%platform%", platform.toUpperCase())
                    .replace("%player%", player.getName())
                    .replace("%link%", link);
            if (line.contains("%link%")) {
                TextComponent linkComponent = new TextComponent(CC.translate(formattedLine.replace("%link%", link)));
                linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
                linkComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(CC.translate(config.getString("stream.link-hover", "&eClick to open the link")))));
                message.addExtra(linkComponent);
            } else {
                message.addExtra(new TextComponent(CC.translate(formattedLine)));
            }
            message.addExtra(new TextComponent("\n"));
        }

        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            p.sendMessage(message);
        }

        cooldowns.put(player.getUniqueId().toString(), currentTime);
    }
}