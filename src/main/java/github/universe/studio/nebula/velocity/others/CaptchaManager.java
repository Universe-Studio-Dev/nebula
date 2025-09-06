package github.universe.studio.nebula.velocity.others;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import github.universe.studio.nebula.velocity.utils.CC;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
    private final Map<Player, String> pending = new HashMap<>();
    private final Map<Player, Integer> attempts = new HashMap<>();
    private final Random random = new Random();
    private final int maxAttempts = 3;
    private final ProxyServer server;

    public CaptchaManager(boolean enabled, String captchaServer, String lobbyServer, ProxyServer server) {
        this.enabled = enabled;
        this.captchaServer = captchaServer;
        this.lobbyServer = lobbyServer;
        this.server = server;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean needsCaptcha(Player player) {
        return pending.containsKey(player);
    }

    public String generateCaptcha(Player player) {
        String code = String.format("%04d", random.nextInt(10000));
        pending.put(player, code);
        attempts.put(player, 0);
        return code;
    }

    public boolean verify(Player player, String input) {
        String expected = pending.get(player);
        int currentAttempts = attempts.getOrDefault(player, 0) + 1;
        attempts.put(player, currentAttempts);

        if (expected != null && expected.equalsIgnoreCase(input)) {
            pending.remove(player);
            attempts.remove(player);
            sendVerificationMessage(player);
            return true;
        }

        if (currentAttempts >= maxAttempts) {
            pending.remove(player);
            attempts.remove(player);
            player.disconnect(CC.translateToComponent("<red>You have exceeded the maximum number of attempts."));
        }
        return false;
    }

    public void sendToCaptchaServer(Player player) {
        Optional<RegisteredServer> captcha = server.getServer(captchaServer);
        captcha.ifPresent(server -> {
            player.createConnectionRequest(server).fireAndForget();
            sendRestrictionMessage(player);
        });
    }

    public void sendToLobby(Player player) {
        Optional<RegisteredServer> lobby = server.getServer(lobbyServer);
        lobby.ifPresent(server -> {
            player.createConnectionRequest(server).fireAndForget();
            sendVerificationMessage(player);
        });
    }

    public void clearPlayer(Player player) {
        pending.remove(player);
        attempts.remove(player);
    }

    private void sendRestrictionMessage(Player player) {
        Optional<ServerConnection> connection = player.getCurrentServer();
        connection.ifPresent(conn -> {
            conn.sendPluginMessage(
                    com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier.from("bungeecord:main"),
                    new String[]{"RestrictMovement", player.getUsername()}.toString().getBytes()
            );
        });
    }

    private void sendVerificationMessage(Player player) {
        Optional<ServerConnection> connection = player.getCurrentServer();
        connection.ifPresent(conn -> {
            conn.sendPluginMessage(
                    com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier.from("bungeecord:main"),
                    new String[]{"CaptchaVerified", player.getUsername()}.toString().getBytes()
            );
        });
    }
}