package github.universe.studio.nebula.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.velocity.commands.*;
import github.universe.studio.nebula.velocity.commands.message.IgnoreCommand;
import github.universe.studio.nebula.velocity.commands.message.MsgCommand;
import github.universe.studio.nebula.velocity.commands.message.ReplyCommand;
import github.universe.studio.nebula.velocity.commands.player.HelpopCommand;
import github.universe.studio.nebula.velocity.commands.player.HubCommand;
import github.universe.studio.nebula.velocity.commands.player.PingCommand;
import github.universe.studio.nebula.velocity.commands.player.ReportCommand;
import github.universe.studio.nebula.velocity.commands.staff.BlacklistCommand;
import github.universe.studio.nebula.velocity.commands.staff.InfoCommand;
import github.universe.studio.nebula.velocity.commands.staff.MaintenanceCommand;
import github.universe.studio.nebula.velocity.commands.staff.StaffChatCommand;
import github.universe.studio.nebula.velocity.listeners.*;
import github.universe.studio.nebula.velocity.others.CaptchaManager;
import github.universe.studio.nebula.velocity.others.FriendManager;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "nebula", name = "Nebula", version = "1.6", authors = {"Universe Studio"})
public class VelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final PluginContainer pluginContainer;
    private final Path dataDirectory;
    private Announcer announcer;
    private StaffChatListener staffChatListener;
    private FriendManager friendManager;
    private CaptchaManager captchaManager;
    private ConfigManager configManager;

    @Inject
    public VelocityPlugin(ProxyServer server, Logger logger, PluginContainer pluginContainer, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Nebula.initVelocity(this);
        CC cc = new CC(server);
        configManager = new ConfigManager();
        ConfigManager.init(this);
        ConfigManager.load();

        cc.console("&b&lNEBULA (ProxyCore)");
        cc.console("       &a&lENABLED");
        cc.console(" (Version: &b1.6)");
        cc.console(" (Author: &bUniverse Studio)");
        cc.console(" (Discord: &bhttps://discord.gg/jGKm94fMAk)");
        cc.console("");
        cc.console(" (This plugin will be free until a limited version,)");
        cc.console(" (so take advantage.)");
        cc.console("");

        announcer = new Announcer(this, server, cc);
        staffChatListener = new StaffChatListener(this, server, cc);
        var config = ConfigManager.getConfig();
        boolean captchaEnabled = config.node("captcha", "enabled").getBoolean(true);
        String captchaServer = config.node("captcha", "server").getString("captcha");
        String lobbyServer = config.node("captcha", "lobby").getString("lobby");
        captchaManager = new CaptchaManager(captchaEnabled, captchaServer, lobbyServer, server, this);
        friendManager = new FriendManager(configManager, server);

        server.getEventManager().register(this, new GeneralListeners(this, server, cc, pluginContainer));
        server.getEventManager().register(this, new MotdListener(this, server, cc));
        server.getEventManager().register(this, staffChatListener);
        server.getEventManager().register(this, new FriendListener(friendManager));
        server.getEventManager().register(this, new ChatCaptchaListener(captchaManager, server, this));

        friendManager.loadFriends();
        announcer.start();

        CommandManager commandManager = server.getCommandManager();

        registerCommand(commandManager, "ignore", "ignorar");
        registerCommand(commandManager, "msg", "tell", "w", "mensaje");
        registerCommand(commandManager, "reply", "r", "responder");
        registerCommand(commandManager, "helpop", "ayuda");
        registerCommand(commandManager, "hub", "lobby");
        registerCommand(commandManager, "ping");
        registerCommand(commandManager, "report", "reporte");
        registerCommand(commandManager, "blacklist");
        registerCommand(commandManager, "info", "whois");
        registerCommand(commandManager, "maintenance", "mantenimiento");
        registerCommand(commandManager, "sc", "staffchat");
        registerCommand(commandManager, "nebula", "n");
        registerCommand(commandManager, "stream");
        registerCommand(commandManager, "friend", "friends", "amigos", "amigo", "frd");
    }

    private void registerCommand(CommandManager manager, String name, String... aliases) {
        if (!isCommandEnabled(name)) return;

        CommandMeta meta = manager.metaBuilder(name)
                .aliases(aliases)
                .build();

        manager.register(meta, getCommandInstance(name));
    }

    private com.velocitypowered.api.command.Command getCommandInstance(String name) {
        return switch (name.toLowerCase()) {
            case "ignore" -> new IgnoreCommand(server);
            case "msg" -> new MsgCommand(server);
            case "reply" -> new ReplyCommand(server);
            case "helpop" -> new HelpopCommand(this, server);
            case "hub" -> new HubCommand(this, server);
            case "ping" -> new PingCommand(server);
            case "report" -> new ReportCommand(this, server);
            case "blacklist" -> new BlacklistCommand(server);
            case "info" -> new InfoCommand(server);
            case "maintenance" -> new MaintenanceCommand(this, server);
            case "sc" -> new StaffChatCommand(this, staffChatListener);
            case "nebula" -> new NebulaCommand(this, server, announcer, pluginContainer);
            case "stream" -> new StreamCommand(this, server);
            case "friend" -> new FriendCommand(friendManager, server);
            default -> throw new IllegalArgumentException("Unknown command: " + name);
        };
    }

    private boolean isCommandEnabled(String name) {
        return ConfigManager.getConfig().node("commands", name).getBoolean(true);
    }

    public Path getDataDirectory() { return dataDirectory; }
    public Logger getLogger() { return logger; }
    public CaptchaManager getCaptchaManager() { return captchaManager; }
}