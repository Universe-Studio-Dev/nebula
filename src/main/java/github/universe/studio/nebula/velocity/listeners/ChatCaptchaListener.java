package github.universe.studio.nebula.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.others.CaptchaManager;
import github.universe.studio.nebula.velocity.utils.CC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import java.time.Duration;

/**
 * @author DanielH131COL
 * @created 28/08/2025
 * @project nebula
 * @file ChatCaptchaListener
 */
public class ChatCaptchaListener {

    private final CaptchaManager captchaManager;
    private final ProxyServer server;

    public ChatCaptchaListener(CaptchaManager captchaManager, ProxyServer server) {
        this.captchaManager = captchaManager;
        this.server = server;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();

        if (!captchaManager.isEnabled()) return;

        String code = captchaManager.generateCaptcha(player);
        captchaManager.sendToCaptchaServer(player);

        player.sendMessage(CC.translateToComponent("<red><bold>⚠ AntiBot Activated</bold>"));
        player.sendMessage(CC.translateToComponent("<white>Please enter this code in the chat: <yellow>" + code));

        Component title = CC.translateToComponent("<red><bold>⚠ AntiBot Activated</bold>");
        Component subtitle = CC.translateToComponent("<white>Enter this code: <yellow>" + code);
        Title.Times times = Title.Times.times(Duration.ofMillis(1000), Duration.ofMillis(5000), Duration.ofMillis(1000));
        Title titleObject = Title.title(title, subtitle, times);
        player.showTitle(titleObject);
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!captchaManager.isEnabled()) return;
        if (!captchaManager.needsCaptcha(player)) return;

        String message = event.getMessage();
        if (captchaManager.verify(player, message)) {
            Component title = CC.translateToComponent("<green><bold>✅ Verification Completed!</bold>");
            Component subtitle = CC.translateToComponent("<white>Welcome to the server");
            Title.Times times = Title.Times.times(Duration.ofMillis(1000), Duration.ofMillis(3000), Duration.ofMillis(1000));
            Title titleObject = Title.title(title, subtitle, times);
            player.showTitle(titleObject);
            player.sendMessage(CC.translateToComponent("<green><bold>✅ Verification Completed!</bold>"));
            player.sendMessage(CC.translateToComponent("<white>Welcome to the server"));
            captchaManager.sendToLobby(player);
        } else {
            Component title = CC.translateToComponent("<red><bold>❌ Incorrect Code</bold>");
            Component subtitle = CC.translateToComponent("<white>Please try again");
            Title.Times times = Title.Times.times(Duration.ofMillis(1000), Duration.ofMillis(3000), Duration.ofMillis(1000));
            Title titleObject = Title.title(title, subtitle, times);
            player.showTitle(titleObject);
            player.sendMessage(CC.translateToComponent("<red><bold>❌ Incorrect Code</bold>"));
            player.sendMessage(CC.translateToComponent("<white>Please try again"));
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        captchaManager.clearPlayer(player);
    }
}