package github.universe.studio.nebula.velocity.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 4/09/2025
 * @project nebula
 * @file CC
 */
public class CC {

    private final ProxyServer server;

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
}