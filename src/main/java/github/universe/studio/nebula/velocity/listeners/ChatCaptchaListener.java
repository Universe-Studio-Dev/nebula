package github.universe.studio.nebula.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import github.universe.studio.nebula.velocity.others.CaptchaManager;
import github.universe.studio.nebula.velocity.utils.CC;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file ChatCaptchaListener
 */

public class ChatCaptchaListener {

    private final CaptchaManager captchaManager;
    private final CC cc;

    public ChatCaptchaListener(CaptchaManager captchaManager, CC cc) {
        this.captchaManager = captchaManager;
        this.cc = cc;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent e) {
        Player p = e.getPlayer();

        if (!captchaManager.isEnabled()) return;

        String code = captchaManager.generateCaptcha(p);
        captchaManager.sendToCaptchaServer(p);

        p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("&c⚠ AntiBot activated")));
        p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("Enter the code in the chat: " + code)));
    }

    @Subscribe
    public void onChat(PlayerChatEvent e) {
        Player p = e.getPlayer();

        if (!captchaManager.isEnabled()) return;
        if (!captchaManager.needsCaptcha(p)) return;

        e.setResult(PlayerChatEvent.ChatResult.denied());

        if (captchaManager.verify(p, e.getMessage())) {
            p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("&a✅ Verification completed. Welcome!")));
            captchaManager.sendToLobby(p);
        } else {
            p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(CC.translate("&c❌ Incorrect code. Please try again.")));
        }
    }
}