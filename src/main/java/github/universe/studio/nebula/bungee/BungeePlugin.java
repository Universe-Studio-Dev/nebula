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
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

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

        CC.console("&b&lNEBULA (ProxyCore)");
        CC.console("        &a&lENABLED");
        CC.console(" (Version: &b" + getDescription().getVersion() + ")");
        CC.console(" (Author: &b" + getDescription().getAuthor() + ")");
        CC.console(" (Discord: &bhttps://discord.gg/jGKm94fMAk)");
        CC.console("");
        CC.console(" (This plugin will be free until a limited version,)");
        CC.console(" (so take advantage.)");
        CC.console("");

        configManager = new ConfigManager(this);
        configManager.load();

        announcer = new Announcer(this);
        announcer.start();

        connectionLimiter = new ConnectionLimiter(3000);
        floodDetector = new GlobalFloodDetector(50, 5000);
        blacklistManager = new BlacklistManager(5 * 60 * 1000);
        nameValidator = new NameValidator();
        captchaManager = new CaptchaManager(configManager.getConfig().getBoolean("captcha.enabled", true), configManager.getConfig().getString("captcha.server", "captcha"), configManager.getConfig().getString("captcha.lobby", "lobby"), this);
        friendManager = new FriendManager(configManager);
        friendManager.loadFriends();

        getProxy().getPluginManager().registerListener(this, new ConnectionListener(this, connectionLimiter, floodDetector, blacklistManager, nameValidator));
        getProxy().getPluginManager().registerListener(this, new ChatCaptchaListener(captchaManager, this));
        getProxy().getPluginManager().registerListener(this, staffChatListener = new StaffChatListener(this));
        getProxy().getPluginManager().registerListener(this, new GeneralListeners(this));
        getProxy().getPluginManager().registerListener(this, new MotdListener(this));
        getProxy().getPluginManager().registerListener(this, new FriendListener(friendManager));

        registerIfEnabled("msg", new MsgCommand());
        registerIfEnabled("reply", new ReplyCommand());
        registerIfEnabled("ignore", new IgnoreCommand());
        registerIfEnabled("blacklist", new BlacklistCommand());
        registerIfEnabled("nebula", new NebulaCommand());
        registerIfEnabled("ping", new PingCommand());
        registerIfEnabled("info", new InfoCommand());
        registerIfEnabled("maintenance", new MaintenanceCommand(this));
        registerIfEnabled("hub", new HubCommand(this));
        registerIfEnabled("report", new ReportCommand(this));
        registerIfEnabled("helpop", new HelpopCommand(this));
        registerIfEnabled("staffchat", new StaffChatCommand(this, staffChatListener));
        registerIfEnabled("stream", new StreamCommand(this));
        registerIfEnabled("friend", new FriendCommand(friendManager));
    }

    private void registerIfEnabled(String name, Command command) {
        if (isCommandEnabled(name)) {
            getProxy().getPluginManager().registerCommand(this, command);
        }
    }

    private boolean isCommandEnabled(String name) {
        return ConfigManager.getConfig().getBoolean("commands." + name, true);
    }

    @Override
    public void onDisable() {
        CC.console("&b&lNEBULA (ProxyCore)");
        CC.console("        &c&lDISABLED");
        CC.console(" (Version: &b" + getDescription().getVersion() + ")");
        CC.console(" (Author: &b" + getDescription().getAuthor() + ")");
        CC.console("");

        configManager.saveConfig();
        configManager.saveMessages();
        configManager.saveAnnouncements();
        configManager.saveFriends();
        announcer.stop();
    }

    public static BungeePlugin getInstance() { return instance; }
    public static ConfigManager getConfigManager() { return instance.configManager; }
    public Announcer getAnnouncer() { return announcer; }
    public StaffChatListener getStaffChatListener() { return staffChatListener; }
    public GlobalFloodDetector getFloodDetector() { return floodDetector; }
    public FriendManager getFriendManager() { return friendManager; }
    public CaptchaManager getCaptchaManager() { return captchaManager; }
}