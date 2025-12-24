package github.universe.studio.nebula.common;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class InteractiveChat {

    public static Component buildInteractiveMessage(Player player, String message) {
        Component base = Component.text("[" + player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Unknown") + "] ")
                .append(Component.text(player.getUsername(), NamedTextColor.GREEN))
                .append(Component.text(": " + message));

        Component hover = Component.text("Ping: " + player.getPing() + "ms\n")
                .append(Component.text("Server: " + player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Unknown") + "\n"))
                .append(Component.text("Rank: " + getRank(player)));

        base = base.hoverEvent(HoverEvent.showText(hover));

        base = base.clickEvent(ClickEvent.suggestCommand("/msg " + player.getUsername() + " "));

        if (isStaff(player)) {
            base = Component.text("[Staff] ").append(base)
                    .clickEvent(ClickEvent.runCommand("/reply " + player.getUsername()));
        }

        return base;
    }

    private static String getRank(Player player) {
        return "User";
    }

    private static boolean isStaff(Player player) {
        return false;
    }
}