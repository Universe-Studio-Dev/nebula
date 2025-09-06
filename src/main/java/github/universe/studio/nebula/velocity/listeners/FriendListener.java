package github.universe.studio.nebula.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import github.universe.studio.nebula.velocity.others.FriendManager;

/**
 * @author DanielH131COL
 * @created 05/09/2025
 * @project nebula
 * @file FriendListener
 */
public class FriendListener {
    private final FriendManager friendManager;

    public FriendListener(FriendManager friendManager) {
        this.friendManager = friendManager;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        friendManager.notifyFriendConnect(player);
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        friendManager.notifyFriendDisconnect(player);
    }
}