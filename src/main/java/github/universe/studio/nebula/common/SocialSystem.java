package github.universe.studio.nebula.common;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.*;

public class SocialSystem {

    private final VelocityPlugin plugin;
    private final Map<UUID, List<UUID>> friends = new HashMap<>();
    private final Map<UUID, String> statuses = new HashMap<>();

    public SocialSystem(VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    public void addFriend(UUID player, UUID friend) {
        friends.computeIfAbsent(player, k -> new ArrayList<>()).add(friend);
    }

    private void notifyFriends(Player player, String message, ClickEvent click) {
        friends.getOrDefault(player.getUniqueId(), Collections.emptyList()).stream()
                .map(plugin.getServer()::getPlayer)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(p -> p.sendMessage(Component.text(message).clickEvent(click)));
    }

    @Subscribe
    public void onConnect(ServerConnectedEvent event) {
        statuses.put(event.getPlayer().getUniqueId(), "Online (" + event.getServer().getServerInfo().getName() + ")");
        notifyFriends(event.getPlayer(), event.getPlayer().getUsername() + " se conectó\nServidor: " + event.getServer().getServerInfo().getName(),
                ClickEvent.runCommand("/server " + event.getServer().getServerInfo().getName()));
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        statuses.put(event.getPlayer().getUniqueId(), "Offline");
        notifyFriends(event.getPlayer(), event.getPlayer().getUsername() + " se desconectó", null);
    }

    public String getStatus(UUID uuid) {
        return statuses.getOrDefault(uuid, "Offline");
    }

    public List<UUID> getFriends(UUID player) {
        return friends.getOrDefault(player, Collections.emptyList());
    }
}