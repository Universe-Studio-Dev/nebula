package github.universe.studio.nebula.bungee.listeners;

import github.universe.studio.nebula.bungee.BungeePlugin;
import github.universe.studio.nebula.bungee.others.CaptchaManager;
import github.universe.studio.nebula.bungee.utils.CC;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author DanielH131COL
 * @created 28/08/2025
 * @project nebula
 * @file ChatCaptchaListener
 */
public class ChatCaptchaListener implements Listener {

    private final CaptchaManager captchaManager;
    private final BungeePlugin plugin;

    public ChatCaptchaListener(CaptchaManager captchaManager, BungeePlugin plugin) {
        this.captchaManager = captchaManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent e) {
        ProxiedPlayer p = e.getPlayer();

        if (!captchaManager.isEnabled()) return;

        String code = captchaManager.generateCaptcha(p);
        captchaManager.sendToCaptchaServer(p);
        captchaManager.scheduleTimeout(p);

        Title title = ProxyServer.getInstance().createTitle();
        title.title(new TextComponent(CC.translate("&c&l⚠ AntiBot Activated")));
        title.subTitle(new TextComponent(CC.translate("&fEnter this code in the chat: &e" + code)));
        title.fadeIn(20);
        title.stay(300);
        title.fadeOut(20);
        title.send(p);
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) return;
        ProxiedPlayer p = (ProxiedPlayer) e.getSender();

        if (!captchaManager.isEnabled()) return;
        if (!captchaManager.needsCaptcha(p)) return;

        e.setCancelled(true);

        if (captchaManager.verify(p, e.getMessage())) {
            Title title = ProxyServer.getInstance().createTitle();
            title.title(new TextComponent(CC.translate("&a&l✅ Verification Completed!")));
            title.subTitle(new TextComponent(CC.translate("&fWelcome to the server")));
            title.fadeIn(20);
            title.stay(100);
            title.fadeOut(20);
            title.send(p);
            captchaManager.cancelTimeout(p);
            captchaManager.sendToLobby(p);
        } else {
            Title title = ProxyServer.getInstance().createTitle();
            title.title(new TextComponent(CC.translate("&c&l❌ Incorrect Code")));
            title.subTitle(new TextComponent(CC.translate("&fPlease try again")));
            title.fadeIn(20);
            title.stay(100);
            title.fadeOut(20);
            title.send(p);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e) {
        ProxiedPlayer p = e.getPlayer();
        captchaManager.clearPlayer(p);
    }
}