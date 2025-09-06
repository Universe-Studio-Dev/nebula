package github.universe.studio.nebula.bungee.listeners;

import github.universe.studio.nebula.bungee.others.FriendManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author DanielH131COL
 * @created 05/09/2025
 * @project nebula
 * @file FriendListener
 */
public class FriendListener implements Listener {
    private final FriendManager friendManager;

    public FriendListener(FriendManager friendManager) {
        this.friendManager = friendManager;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        friendManager.notifyFriendConnect(player);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        friendManager.notifyFriendDisconnect(player);
    }
}