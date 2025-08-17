package github.universe.studio.nebula.commands.staff;

import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.listeners.StaffChatListener;
import github.universe.studio.nebula.utils.CC;
import github.universe.studio.nebula.utils.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

/**
 * @author DanielH131COL
 * @created 16/08/2025
 * @project nebula
 * @file StaffChatCommand
 */
public class StaffChatCommand extends Command {

    private final Nebula plugin;
    private final StaffChatListener listener;

    public StaffChatCommand(Nebula plugin, StaffChatListener listener) {
        super("sc", "nubula.staff", "staffchat");
        this.plugin = plugin;
        this.listener = listener;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.no-console"))));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!player.hasPermission("nubula.staff")) {
            player.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.no-permission"))));
            return;
        }

        UUID uuid = player.getUniqueId();
        if (listener.getToggledPlayers().contains(uuid)) {
            listener.getToggledPlayers().remove(uuid);
            player.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.staffchat-off"))));
        } else {
            listener.getToggledPlayers().add(uuid);
            player.sendMessage(new TextComponent(CC.translate(ConfigManager.getMessages().getString("messages.staffchat-on"))));
        }
    }
}