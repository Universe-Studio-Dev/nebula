package github.universe.studio.nebula.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.velocity.commands.*;
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
import github.universe.studio.nebula.velocity.listeners.Announcer;
import github.universe.studio.nebula.velocity.listeners.FriendListener;
import github.universe.studio.nebula.velocity.listeners.GeneralListeners;
import github.universe.studio.nebula.velocity.listeners.MotdListener;
import github.universe.studio.nebula.velocity.listeners.StaffChatListener;
import github.universe.studio.nebula.velocity.listeners.ChatCaptchaListener;
import github.universe.studio.nebula.velocity.others.CaptchaManager;
import github.universe.studio.nebula.velocity.others.FriendManager;
import github.universe.studio.nebula.velocity.utils.CC;
import github.universe.studio.nebula.velocity.utils.ConfigManager;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file VelocityPlugin
 */
@Plugin(id = "nebula", name = "Nebula", version = "1.3", authors = {"Universe Studio"})
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

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Logger getLogger() {
        return logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Nebula.initVelocity(this);
        CC cc = new CC(server);
        configManager = new ConfigManager();
        ConfigManager.init(this);
        ConfigManager.load();

        logger.info(CC.translate("<gradient:blue:aqua>NEBULA</gradient> ⇨ <white>ProxyCore"));
        logger.info(CC.translate("<green><bold>ENABLED"));
        logger.info(CC.translate(" ⇨ <white>Version: <aqua>1.4"));
        logger.info(CC.translate(" ⇨ <white>Author: <aqua>Universe Studio"));
        logger.info(CC.translate(" ⇨ <white>Discord: <aqua>https://discord.gg/jGKm94fMAk"));
        logger.info(CC.translate(""));
        logger.info(CC.translate(" <aqua>This plugin will be free until a limited version,"));
        logger.info(CC.translate(" <aqua>so take advantage."));
        logger.info(CC.translate(""));

        announcer = new Announcer(this, server, cc);
        staffChatListener = new StaffChatListener(this, server, cc);
        friendManager = new FriendManager(configManager, server);
        captchaManager = new CaptchaManager(true, "captcha", "lobby", server);
        GeneralListeners generalListeners = new GeneralListeners(this, server, cc, pluginContainer);
        MotdListener motdListener = new MotdListener(this, server, cc);
        FriendListener friendListener = new FriendListener(friendManager);
        ChatCaptchaListener captchaListener = new ChatCaptchaListener(captchaManager, server);

        server.getEventManager().register(this, generalListeners);
        server.getEventManager().register(this, motdListener);
        server.getEventManager().register(this, staffChatListener);
        server.getEventManager().register(this, friendListener);
        server.getEventManager().register(this, captchaListener);

        friendManager.loadFriends();
        announcer.start();

        CommandManager commandManager = server.getCommandManager();
        commandManager.register(commandManager.metaBuilder("msg").aliases("tell", "w", "mensaje").build(), new MsgCommand(server));
        commandManager.register(commandManager.metaBuilder("reply").aliases("r", "responder").build(), new ReplyCommand(server));
        commandManager.register(commandManager.metaBuilder("helpop").aliases("ayuda").build(), new HelpopCommand(this, server));
        commandManager.register(commandManager.metaBuilder("hub").aliases("lobby").build(), new HubCommand(this, server));
        commandManager.register(commandManager.metaBuilder("ping").build(), new PingCommand(server));
        commandManager.register(commandManager.metaBuilder("report").aliases("reporte").build(), new ReportCommand(this, server));
        commandManager.register(commandManager.metaBuilder("blacklist").build(), new BlacklistCommand(server));
        commandManager.register(commandManager.metaBuilder("info").aliases("whois").build(), new InfoCommand(server));
        commandManager.register(commandManager.metaBuilder("maintenance").aliases("mantenimiento").build(), new MaintenanceCommand(this, server));
        commandManager.register(commandManager.metaBuilder("sc").aliases("staffchat").build(), new StaffChatCommand(this, staffChatListener));
        commandManager.register(commandManager.metaBuilder("nebula").aliases("n").build(), new NebulaCommand(this, server, announcer, pluginContainer));
        commandManager.register(commandManager.metaBuilder("stream").build(), new StreamCommand(this, server));
        commandManager.register(commandManager.metaBuilder("friend").aliases("friends", "amigos", "amigo", "frd").build(), new FriendCommand(friendManager, server));
    }
}