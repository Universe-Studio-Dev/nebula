package github.universe.studio.nebula.velocity.commands.staff;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.listeners.StaffChatListener;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file StaffChatCommand
 */
public class StaffChatCommand implements SimpleCommand {

    private final VelocityPlugin plugin;
    private final StaffChatListener listener;

    public StaffChatCommand(VelocityPlugin plugin, StaffChatListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();

        if (!(sender instanceof Player)) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "no-console").getString("&cThis command is for players only"))
            ));
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("nubula.staff")) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "no-permission").getString("&cYou do not have permission"))
            ));
            return;
        }

        UUID uuid = player.getUniqueId();
        if (listener.getToggledPlayers().contains(uuid)) {
            listener.getToggledPlayers().remove(uuid);
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "staffchat-off").getString("&cStaff chat disabled"))
            ));
        } else {
            listener.getToggledPlayers().add(uuid);
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    CC.translate(ConfigManager.getMessages().node("messages", "staffchat-on").getString("&aStaff chat enabled"))
            ));
        }
    }
}