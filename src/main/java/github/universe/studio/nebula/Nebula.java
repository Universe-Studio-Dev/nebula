package github.universe.studio.nebula;

import github.universe.studio.nebula.commands.player.HelpopCommand;
import github.universe.studio.nebula.commands.NebulaCommand;
import github.universe.studio.nebula.commands.player.ReportCommand;
import github.universe.studio.nebula.commands.StreamCommand;
import github.universe.studio.nebula.commands.message.MsgCommand;
import github.universe.studio.nebula.commands.message.ReplyCommand;
import github.universe.studio.nebula.commands.staff.BlacklistCommand;
import github.universe.studio.nebula.commands.staff.StaffChatCommand;
import github.universe.studio.nebula.listeners.Announcer;
import github.universe.studio.nebula.listeners.GeneralListeners;
import github.universe.studio.nebula.listeners.MotdListener;
import github.universe.studio.nebula.listeners.StaffChatListener;
import github.universe.studio.nebula.utils.CC;
import github.universe.studio.nebula.utils.ConfigManager;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author DanielH131COL
 * @created 14/08/2025
 * @project nebula
 * @file Nebula
 */
public final class Nebula extends Plugin {

    private static Nebula instance;
    private ConfigManager configManager;
    private Announcer announcer;
    private StaffChatListener staffChatListener;

    @Override
    public void onEnable() {
        instance = this;

        CC.console("&b&lNEBULA &7⇨ &fProxyCore");
        CC.console("        &a&lENABLED");
        CC.console(" &7⇨ &fVersion: &b" + getDescription().getVersion());
        CC.console(" &7⇨ &fAuthor: &b" + getDescription().getAuthor());
        CC.console("");
        CC.console(" &bThis plugin will be free until a limited version,");
        CC.console(" &bso take advantage.");
        CC.console("");

        configManager = new ConfigManager(this);
        configManager.load();
        announcer = new Announcer(this);
        announcer.start();

        staffChatListener = new StaffChatListener(this);
        getProxy().getPluginManager().registerListener(this, staffChatListener);
        getProxy().getPluginManager().registerListener(this, new GeneralListeners(this));
        getProxy().getPluginManager().registerListener(this, new MotdListener(this));

        getProxy().getPluginManager().registerCommand(this, new MsgCommand());
        getProxy().getPluginManager().registerCommand(this, new ReplyCommand());
        getProxy().getPluginManager().registerCommand(this, new BlacklistCommand());
        getProxy().getPluginManager().registerCommand(this, new ReportCommand(this));
        getProxy().getPluginManager().registerCommand(this, new HelpopCommand(this));
        getProxy().getPluginManager().registerCommand(this, new StaffChatCommand(this, staffChatListener));
        getProxy().getPluginManager().registerCommand(this, new NebulaCommand());
        getProxy().getPluginManager().registerCommand(this, new StreamCommand(this));
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
        announcer.stop();
    }

    public static Nebula getInstance() {
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
}