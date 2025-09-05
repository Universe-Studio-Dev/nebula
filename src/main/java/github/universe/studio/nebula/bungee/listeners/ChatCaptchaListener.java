package github.universe.studio.nebula.bungee.listeners;

import github.universe.studio.nebula.bungee.others.CaptchaManager;
import github.universe.studio.nebula.bungee.utils.CC;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
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

    public ChatCaptchaListener(CaptchaManager captchaManager) {
        this.captchaManager = captchaManager;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent e) {
        ProxiedPlayer p = e.getPlayer();

        if (!captchaManager.isEnabled()) return;

        String code = captchaManager.generateCaptcha(p);
        captchaManager.sendToCaptchaServer(p);

        p.sendMessage(CC.translate("&c⚠ AntiBot activated"));
        p.sendMessage(CC.translate("Enter the code in the chat: " + code));
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) return;
        ProxiedPlayer p = (ProxiedPlayer) e.getSender();

        if (!captchaManager.isEnabled()) return;
        if (!captchaManager.needsCaptcha(p)) return;

        e.setCancelled(true);

        if (captchaManager.verify(p, e.getMessage())) {
            p.sendMessage(CC.translate("&a✅ Verification completed. Welcome!"));
            captchaManager.sendToLobby(p);
        } else {
            p.sendMessage(CC.translate("&c❌ Incorrect code. Please try again."));
        }
    }
}