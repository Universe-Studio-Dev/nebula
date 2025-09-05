package github.universe.studio.nebula.velocity.others;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import github.universe.studio.nebula.velocity.utils.CC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.net.InetSocketAddress;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file ConnectionListener
 */
public class ConnectionListener {

    private final ConnectionLimiter limiter;
    private final GlobalFloodDetector floodDetector;
    private final BlacklistManager blacklist;
    private final NameValidator nameValidator;
    private final CC cc;

    public ConnectionListener(Object plugin, ConnectionLimiter limiter, GlobalFloodDetector floodDetector, BlacklistManager blacklist, NameValidator nameValidator, CC cc) {
        this.limiter = limiter;
        this.floodDetector = floodDetector;
        this.blacklist = blacklist;
        this.nameValidator = nameValidator;
        this.cc = cc;
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent e) {
        String ip = ((InetSocketAddress) e.getConnection().getRemoteAddress()).getAddress().getHostAddress();

        if (blacklist.isBanned(ip)) {
            e.setResult(PreLoginComponentResult.denied(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("&cYou have been temporarily blocked due to suspected botting."))));
            return;
        }

        if (!limiter.allowConnection(ip)) {
            e.setResult(PreLoginComponentResult.denied(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("&cConnections too fast from your IP."))));
            blacklist.ban(ip);
            return;
        }

        if (!floodDetector.allowConnection(ip)) {
            e.setResult(PreLoginComponentResult.denied(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("&cServer in AntiBot protection mode. Please try again later."))));
            return;
        }

        String name = e.getUsername();

        if (!nameValidator.isValid(name)) {
            e.setResult(PreLoginComponentResult.denied(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("&cInvalid username."))));
        }
    }
}