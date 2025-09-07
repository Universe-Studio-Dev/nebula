package github.universe.studio.nebula.bungee;

import github.universe.studio.nebula.Nebula;
import github.universe.studio.nebula.bungee.commands.*;
import github.universe.studio.nebula.bungee.commands.message.IgnoreCommand;
import github.universe.studio.nebula.bungee.commands.message.MsgCommand;
import github.universe.studio.nebula.bungee.commands.message.ReplyCommand;
import github.universe.studio.nebula.bungee.commands.player.HelpopCommand;
import github.universe.studio.nebula.bungee.commands.player.HubCommand;
import github.universe.studio.nebula.bungee.commands.player.PingCommand;
import github.universe.studio.nebula.bungee.commands.player.ReportCommand;
import github.universe.studio.nebula.bungee.commands.staff.BlacklistCommand;
import github.universe.studio.nebula.bungee.commands.staff.InfoCommand;
import github.universe.studio.nebula.bungee.commands.staff.MaintenanceCommand;
import github.universe.studio.nebula.bungee.commands.staff.StaffChatCommand;
import github.universe.studio.nebula.bungee.listeners.*;
import github.universe.studio.nebula.bungee.others.*;
import github.universe.studio.nebula.bungee.utils.CC;
import github.universe.studio.nebula.bungee.utils.ConfigManager;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file BungeePlugin
 */
public class BungeePlugin extends Plugin {

    private static BungeePlugin instance;
    private ConfigManager configManager;
    private Announcer announcer;
    private StaffChatListener staffChatListener;
    private ConnectionLimiter connectionLimiter;
    private GlobalFloodDetector floodDetector;
    private BlacklistManager blacklistManager;
    private NameValidator nameValidator;
    private CaptchaManager captchaManager;
    private FriendManager friendManager;

    @Override
    public void onEnable() {
        Nebula.initBungee(this);
        instance = this;

        CC.console("&b&lNEBULA &7⇨ &fProxyCore");
        CC.console("        &a&lENABLED");
        CC.console(" &7⇨ &fVersion: &b" + getDescription().getVersion());
        CC.console(" &7⇨ &fAuthor: &b" + getDescription().getAuthor());
        CC.console(" &7⇨ &fDiscord: &bhttps://discord.gg/jGKm94fMAk");
        CC.console("");
        CC.console(" &bThis plugin will be free until a limited version,");
        CC.console(" &bso take advantage.");
        CC.console("");

        configManager = new ConfigManager(this);
        configManager.load();
        announcer = new Announcer(this);
        announcer.start();

        connectionLimiter = new ConnectionLimiter(3000);
        floodDetector = new GlobalFloodDetector(50, 5000);
        blacklistManager = new BlacklistManager(5 * 60 * 1000);
        nameValidator = new NameValidator();
        captchaManager = new CaptchaManager(
                configManager.getConfig().getBoolean("captcha.enabled", true),
                configManager.getConfig().getString("captcha.server", "captcha"),
                configManager.getConfig().getString("captcha.lobby", "lobby"),
                this
        );
        friendManager = new FriendManager(configManager);
        friendManager.loadFriends();

        getProxy().getPluginManager().registerListener(this, new ConnectionListener(this, connectionLimiter, floodDetector, blacklistManager, nameValidator));
        getProxy().getPluginManager().registerListener(this, new ChatCaptchaListener(captchaManager, this));
        getProxy().getPluginManager().registerListener(this, staffChatListener = new StaffChatListener(this));
        getProxy().getPluginManager().registerListener(this, new GeneralListeners(this));
        getProxy().getPluginManager().registerListener(this, new MotdListener(this));
        getProxy().getPluginManager().registerListener(this, new FriendListener(friendManager));

        getProxy().getPluginManager().registerCommand(this, new MsgCommand());
        getProxy().getPluginManager().registerCommand(this, new ReplyCommand());
        getProxy().getPluginManager().registerCommand(this, new IgnoreCommand());
        getProxy().getPluginManager().registerCommand(this, new BlacklistCommand());
        getProxy().getPluginManager().registerCommand(this, new NebulaCommand());
        getProxy().getPluginManager().registerCommand(this, new PingCommand());
        getProxy().getPluginManager().registerCommand(this, new InfoCommand());
        getProxy().getPluginManager().registerCommand(this, new MaintenanceCommand(this));
        getProxy().getPluginManager().registerCommand(this, new HubCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ReportCommand(this));
        getProxy().getPluginManager().registerCommand(this, new HelpopCommand(this));
        getProxy().getPluginManager().registerCommand(this, new StaffChatCommand(this, staffChatListener));
        getProxy().getPluginManager().registerCommand(this, new StreamCommand(this));
        getProxy().getPluginManager().registerCommand(this, new FriendCommand(friendManager));
    }

    @Override
    public void onDisable() {
        CC.console("&b&lNEBULA &7⇨ &fProxyCore");
        CC.console("        &c&lDISABLED");
        CC.console(" &7⇨ &fVersion: &b" + getDescription().getVersion());
        CC.console(" &7⇨ &fAuthor: &b" + getDescription().getAuthor());
        CC.console("");

        configManager.saveConfig();
        configManager.saveMessages();
        configManager.saveAnnouncements();
        configManager.saveFriends();
        announcer.stop();
    }

    public static BungeePlugin getInstance() {
        return instance;
    }

    public static ConfigManager getConfigManager() {
        return instance.configManager;
    }

    public Announcer getAnnouncer() {
        return announcer;
    }

    public StaffChatListener getStaffChatListener() {
        return staffChatListener;
    }

    public GlobalFloodDetector getFloodDetector() {
        return floodDetector;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public CaptchaManager getCaptchaManager() {
        return captchaManager;
    }
}