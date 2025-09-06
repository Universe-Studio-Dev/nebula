package github.universe.studio.nebula.bungee.others;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import github.universe.studio.nebula.bungee.utils.CC;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
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
    private final Map<ProxiedPlayer, Integer> attempts = new HashMap<>();
    private final Random random = new Random();
    private final int maxAttempts = 3;

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
        String code = String.format("%04d", random.nextInt(10000));
        pending.put(p, code);
        attempts.put(p, 0);
        return code;
    }

    public boolean verify(ProxiedPlayer p, String input) {
        String expected = pending.get(p);
        int currentAttempts = attempts.getOrDefault(p, 0) + 1;
        attempts.put(p, currentAttempts);

        if (expected != null && expected.equalsIgnoreCase(input)) {
            pending.remove(p);
            attempts.remove(p);
            sendVerificationMessage(p);
            return true;
        }

        if (currentAttempts >= maxAttempts) {
            pending.remove(p);
            attempts.remove(p);
            p.disconnect(new TextComponent(CC.translate("&cYou have exceeded the maximum number of attempts.")));
        }
        return false;
    }

    public void sendToCaptchaServer(ProxiedPlayer p) {
        ServerInfo captcha = ProxyServer.getInstance().getServerInfo(captchaServer);
        if (captcha != null) {
            p.connect(captcha);
            sendRestrictionMessage(p);
        }
    }

    public void sendToLobby(ProxiedPlayer p) {
        ServerInfo lobby = ProxyServer.getInstance().getServerInfo(lobbyServer);
        if (lobby != null) {
            p.connect(lobby);
            sendVerificationMessage(p);
        }
    }

    public void clearPlayer(ProxiedPlayer p) {
        pending.remove(p);
        attempts.remove(p);
    }

    private void sendRestrictionMessage(ProxiedPlayer p) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("RestrictMovement");
        out.writeUTF(p.getName());
        p.getServer().sendData("BungeeCord", out.toByteArray());
    }

    private void sendVerificationMessage(ProxiedPlayer p) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("CaptchaVerified");
        out.writeUTF(p.getName());
        p.getServer().sendData("BungeeCord", out.toByteArray());
    }
}