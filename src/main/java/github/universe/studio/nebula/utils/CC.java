package github.universe.studio.nebula.utils;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 14/08/2025
 * @project nebula
 * @file Nebula
 */

public class CC {

    public static String translate(String text) {
        return IridiumColorAPI.process(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static List<String> translate(List<String> texts) {
        return texts.stream().map(CC::translate).collect(Collectors.toList());
    }

    public static void console(String text) {
        ProxyServer.getInstance().getConsole().sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }
}
