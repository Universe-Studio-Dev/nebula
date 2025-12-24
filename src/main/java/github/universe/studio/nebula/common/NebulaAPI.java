package github.universe.studio.nebula.common;

import com.velocitypowered.api.proxy.Player;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

public class NebulaAPI {

    private static VelocityPlugin plugin;

    public static void init(VelocityPlugin plugin) {
        NebulaAPI.plugin = plugin;
    }

    public static List<UUID> getFriends(UUID player) {
        SocialSystem social = plugin.getSocialSystem();
        if (social != null) {
            return social.getFriends(player);
        }
        return List.of();
    }

    public static void sendProxyMessage(Player player, Component component) {
        player.sendMessage(component);
    }

    public static String getCurrentServer(Player player) {
        return player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Unknown");
    }
}