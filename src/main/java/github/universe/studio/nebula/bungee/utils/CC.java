package github.universe.studio.nebula.bungee.utils;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author DanielH131COL
 * @created 14/08/2025
 * @project nebula
 * @file CC
 */
public class CC {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)&[0-9A-FK-ORX]|§[0-9A-FK-ORX]");

    public static String translate(String text) {
        return IridiumColorAPI.process(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static List<String> translate(List<String> texts) {
        return texts.stream().map(CC::translate).collect(Collectors.toList());
    }

    public static void console(String text) {
        ProxyServer.getInstance().getConsole().sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static String stripColors(String text) {
        if (text == null) return "";
        return STRIP_COLOR_PATTERN.matcher(text).replaceAll("");
    }
}