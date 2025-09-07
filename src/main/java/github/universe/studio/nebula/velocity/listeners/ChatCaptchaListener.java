package github.universe.studio.nebula.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.others.CaptchaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author DanielH131COL
 * @created 28/08/2025
 * @project nebula
 * @file ChatCaptchaListener
 */
public class ChatCaptchaListener {

    private final CaptchaManager captchaManager;
    private final ProxyServer server;
    private final VelocityPlugin plugin;

    public ChatCaptchaListener(CaptchaManager captchaManager, ProxyServer server, VelocityPlugin plugin) {
        this.captchaManager = captchaManager;
        this.server = server;
        this.plugin = plugin;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();

        if (!captchaManager.isEnabled()) return;

        String code = captchaManager.generateCaptcha(player);
        captchaManager.sendToCaptchaServer(player);

        String titleText = "&c&l⚠ AntiBot Activated";
        String subtitleText = "&fPlease enter this code: &e" + code;
        Component title = LegacyComponentSerializer.legacyAmpersand().deserialize(titleText);
        Component subtitle = LegacyComponentSerializer.legacyAmpersand().deserialize(subtitleText);
        Title.Times times = Title.Times.times(Duration.ofMillis(1000), Duration.ofMillis(15000), Duration.ofMillis(1000));
        Title titleObject = Title.title(title, subtitle, times);

        server.getScheduler()
                .buildTask(plugin, () -> player.showTitle(titleObject))
                .delay(500, TimeUnit.MILLISECONDS)
                .schedule();
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!captchaManager.isEnabled()) return;
        if (!captchaManager.needsCaptcha(player)) return;

        String message = event.getMessage();
        if (captchaManager.verify(player, message)) {
            String titleText = "&a&l✅ Verification Completed!";
            String subtitleText = "&fWelcome to the server";
            Component title = LegacyComponentSerializer.legacyAmpersand().deserialize(titleText);
            Component subtitle = LegacyComponentSerializer.legacyAmpersand().deserialize(subtitleText);
            Title.Times times = Title.Times.times(Duration.ofMillis(1000), Duration.ofMillis(5000), Duration.ofMillis(1000));
            Title titleObject = Title.title(title, subtitle, times);
            player.showTitle(titleObject);
            captchaManager.sendToLobby(player);
        } else {
            String titleText = "&c&l❌ Incorrect Code";
            String subtitleText = "&fPlease try again";
            Component title = LegacyComponentSerializer.legacyAmpersand().deserialize(titleText);
            Component subtitle = LegacyComponentSerializer.legacyAmpersand().deserialize(subtitleText);
            Title.Times times = Title.Times.times(Duration.ofMillis(1000), Duration.ofMillis(5000), Duration.ofMillis(1000));
            Title titleObject = Title.title(title, subtitle, times);
            player.showTitle(titleObject);
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        captchaManager.clearPlayer(player);
    }
}