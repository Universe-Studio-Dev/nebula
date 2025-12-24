package github.universe.studio.nebula.common;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Map;

public class ActionsEngine {

    private final VelocityPlugin plugin;
    private Map<String, List<String>> actions;

    public ActionsEngine(VelocityPlugin plugin) {
        this.plugin = plugin;
        loadActions();
    }

    private void loadActions() {
        ConfigurationNode root = ConfigManager.getConfig();
        ConfigurationNode actionsNode = root.node("actions");
        try {
            actions = actionsNode.get(new TypeToken<Map<String, List<String>>>() {});
        } catch (SerializationException e) {
            plugin.getLogger().error("Error loading actions", e);
            actions = Map.of();
        }
    }

    public void execute(String event, Player player, Object... args) {
        List<String> cmds = actions.get(event);
        if (cmds == null) return;

        String serverName = args.length > 0 ? (String) args[0] : "";
        for (String action : cmds) {
            action = action.replace("%player%", player.getUsername()).replace("%server%", serverName);
            if (action.startsWith("title:")) {
                player.showTitle(Title.title(Component.text(action.substring(6).trim()), Component.empty()));
            } else if (action.startsWith("sound:")) {
                String soundKey = action.substring(6).trim().toLowerCase().replace('_', '.');
                Sound sound = Sound.sound(Key.key("minecraft:" + soundKey), Sound.Source.MASTER, 1f, 1f);
                player.playSound(sound);
            } else if (action.startsWith("message:")) {
                player.sendMessage(Component.text(action.substring(8).trim()));
            } else if (action.startsWith("bossbar:")) {
                BossBar bossBar = BossBar.bossBar(Component.text(action.substring(8).trim()), 1f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
                player.showBossBar(bossBar);
            } else if (action.startsWith("actionbar:")) {
                player.sendActionBar(Component.text(action.substring(10).trim()));
            }
        }
    }

    @Subscribe
    public void onJoin(PostLoginEvent event) {
        execute("on-join", event.getPlayer());
    }
}