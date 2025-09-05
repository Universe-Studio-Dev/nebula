package github.universe.studio.nebula.velocity.others;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file CaptchaManager
 */

public class CaptchaManager {

    private final boolean enabled;
    private final String captchaServer;
    private final String lobbyServer;
    private final Map<Player, String> pending = new HashMap<>();
    private final Random random = new Random();
    private final ProxyServer proxy;

    public CaptchaManager(ProxyServer proxy, boolean enabled, String captchaServer, String lobbyServer) {
        this.proxy = proxy;
        this.enabled = enabled;
        this.captchaServer = captchaServer;
        this.lobbyServer = lobbyServer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean needsCaptcha(Player p) {
        return pending.containsKey(p);
    }

    public String generateCaptcha(Player p) {
        String code = String.valueOf(1000 + random.nextInt(9000)); // 4 dígitos
        pending.put(p, code);
        return code;
    }

    public boolean verify(Player p, String input) {
        String expected = pending.get(p);
        if (expected != null && expected.equalsIgnoreCase(input)) {
            pending.remove(p);
            return true;
        }
        return false;
    }

    public void sendToCaptchaServer(Player p) {
        Optional<RegisteredServer> captcha = proxy.getServer(captchaServer);
        captcha.ifPresent(server -> p.createConnectionRequest(server).fireAndForget());
    }

    public void sendToLobby(Player p) {
        Optional<RegisteredServer> lobby = proxy.getServer(lobbyServer);
        lobby.ifPresent(server -> p.createConnectionRequest(server).fireAndForget());
    }
}