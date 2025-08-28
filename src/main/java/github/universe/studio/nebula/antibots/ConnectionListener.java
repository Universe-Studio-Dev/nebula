package github.universe.studio.nebula.antibots;

import github.universe.studio.nebula.utils.CC;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author DanielH131COL
 * @created 28/08/2025
 * @project nebula
 * @file ConnectionListener
 */

public class ConnectionListener implements Listener {

    private final ConnectionLimiter limiter;
    private final GlobalFloodDetector floodDetector;
    private final BlacklistManager blacklist;
    private final NameValidator nameValidator;

    public ConnectionListener(Object plugin, ConnectionLimiter limiter, GlobalFloodDetector floodDetector, BlacklistManager blacklist, NameValidator nameValidator) {
        this.limiter = limiter;
        this.floodDetector = floodDetector;
        this.blacklist = blacklist;
        this.nameValidator = nameValidator;
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent e) {
        String ip = e.getConnection().getAddress().getAddress().getHostAddress();

        if (blacklist.isBanned(ip)) {
            e.setCancelled(true);
            e.setCancelReason(CC.translate("&cYou have been temporarily blocked due to suspected botting."));
            return;
        }

        if (!limiter.allowConnection(ip)) {
            e.setCancelled(true);
            e.setCancelReason(CC.translate("&cConnections too fast from your IP."));
            blacklist.ban(ip);
            return;
        }

        if (!floodDetector.allowConnection(ip)) {
            e.setCancelled(true);
            e.setCancelReason(CC.translate("&cServer in AntiBot protection mode. Please try again later."));
            return;
        }
    }

    @EventHandler
    public void onLogin(LoginEvent e) {
        String name = e.getConnection().getName();

        if (!nameValidator.isValid(name)) {
            e.setCancelled(true);
            e.setCancelReason(CC.translate("&cInvalid username."));
        }
    }
}