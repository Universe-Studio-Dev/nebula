package github.universe.studio.nebula.bungee.others;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author DanielH131COL
 * @created 28/08/2025
 * @project nebula
 * @file CaptchaManager
 */

public class CaptchaManager {

    private final boolean enabled;
    private final String captchaServer;
    private final String lobbyServer;
    private final Map<ProxiedPlayer, String> pending = new HashMap<>();
    private final Random random = new Random();

    public CaptchaManager(boolean enabled, String captchaServer, String lobbyServer) {
        this.enabled = enabled;
        this.captchaServer = captchaServer;
        this.lobbyServer = lobbyServer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean needsCaptcha(ProxiedPlayer p) {
        return pending.containsKey(p);
    }

    public String generateCaptcha(ProxiedPlayer p) {
        String code = String.valueOf(1000 + random.nextInt(9000)); // 4 dígitos
        pending.put(p, code);
        return code;
    }

    public boolean verify(ProxiedPlayer p, String input) {
        String expected = pending.get(p);
        if (expected != null && expected.equalsIgnoreCase(input)) {
            pending.remove(p);
            return true;
        }
        return false;
    }

    public void sendToCaptchaServer(ProxiedPlayer p) {
        ServerInfo captcha = ProxyServer.getInstance().getServerInfo(captchaServer);
        if (captcha != null) {
            p.connect(captcha);
        }
    }

    public void sendToLobby(ProxiedPlayer p) {
        ServerInfo lobby = ProxyServer.getInstance().getServerInfo(lobbyServer);
        if (lobby != null) {
            p.connect(lobby);
        }
    }
}