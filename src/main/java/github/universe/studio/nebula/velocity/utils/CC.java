package github.universe.studio.nebula.velocity.utils;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
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
        return IridiumColorAPI.process(LegacyComponentSerializer.legacyAmpersand().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(text)));
    }

    public static List<String> translate(List<String> texts) {
        return texts.stream().map(CC::translate).collect(Collectors.toList());
    }

    public void console(String text) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(IridiumColorAPI.process(text));
        server.getConsoleCommandSource().sendMessage(component);
    }
}