package github.universe.studio.nebula.bungee.utils;

import github.universe.studio.nebula.bungee.BungeePlugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author DanielH131COL
 * @created 14/08/2025
 * @project nebula
 * @file ConfigManager
 */
public class ConfigManager {
    private static Configuration config, messages, announcements;
    private final BungeePlugin plugin;

    public ConfigManager(BungeePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        try {
            config = loadFile("config.yml");
            messages = loadFile("messages.yml");
            announcements = loadFile("announcements.yml");
        } catch (IOException e) {
            plugin.getLogger().severe("Error loading configs: " + e.getMessage());
        }
    }

    private Configuration loadFile(String name) throws IOException {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResourceAsStream(name)) {
                Files.copy(in, file.toPath());
            }
        }
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
    }

    public void saveConfig() {
        saveFile(config, "config.yml");
    }

    public void saveMessages() {
        saveFile(messages, "messages.yml");
    }

    public void saveAnnouncements() {
        saveFile(announcements, "announcements.yml");
    }

    private void saveFile(Configuration cfg, String name) {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(cfg, new File(plugin.getDataFolder(), name));
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving " + name + ": " + e.getMessage());
        }
    }

    public static Configuration getConfig() {
        return config;
    }

    public static Configuration getMessages() {
        return messages;
    }

    public static Configuration getAnnouncements() {
        return announcements;
    }
}