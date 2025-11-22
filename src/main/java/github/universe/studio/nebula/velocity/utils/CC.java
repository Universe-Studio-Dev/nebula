package github.universe.studio.nebula.velocity.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 4/09/2025
 * @project nebula
 * @file CC
 */
public class CC {

    private final ProxyServer server;

    private static final Pattern STRIP_PATTERN = Pattern.compile("(?i)<[^>]*>|&[0-9A-FK-ORX]");

    public CC(ProxyServer server) {
        this.server = server;
    }

    public static String translate(String text) {
        return MiniMessage.miniMessage().serialize(
                MiniMessage.miniMessage().deserialize(text)
        );
    }

    public static List<String> translate(List<String> texts) {
        return texts.stream().map(CC::translate).collect(Collectors.toList());
    }

    public static Component translateToComponent(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    public void console(String text) {
        Component component = translateToComponent(text);
        server.getConsoleCommandSource().sendMessage(component);
    }

    public static String stripColors(String text) {
        if (text == null || text.isEmpty()) return "";
        return STRIP_PATTERN.matcher(text).replaceAll("");
    }
}