package github.universe.studio.nebula.velocity.utils;

import github.universe.studio.nebula.velocity.VelocityPlugin;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file ConfigManager
 */
public class ConfigManager {

    private static ConfigurationNode config;
    private static ConfigurationNode messages;
    private static ConfigurationNode announcements;
    private static ConfigurationNode friends;
    private static VelocityPlugin plugin;
    private static Path dataFolder;
    private static Logger logger;

    public static void init(VelocityPlugin p) {
        plugin = p;
        dataFolder = p.getDataDirectory();
        logger = p.getLogger();
    }

    public static void load() {
        try {
            config = loadFile("config.yml");
            messages = loadFile("messages.yml");
            announcements = loadFile("announcements.yml");
            friends = loadFile("friends.yml");
        } catch (IOException e) {
            logger.error("Failed to load configuration files: {}", e.getMessage(), e);
        }
    }

    private static ConfigurationNode loadFile(String name) throws IOException {
        Path file = dataFolder.resolve(name);

        if (!Files.exists(file)) {
            Files.createDirectories(dataFolder);
            try (InputStream in = plugin.getClass().getResourceAsStream("/" + name)) {
                if (in != null) {
                    Files.copy(in, file);
                } else {
                    Files.createFile(file);
                }
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(file)
                .build();

        return loader.load();
    }

    public static void saveConfig() {
        saveFile(config, "config.yml");
    }

    public static void saveMessages() {
        saveFile(messages, "messages.yml");
    }

    public static void saveAnnouncements() {
        saveFile(announcements, "announcements.yml");
    }

    public static void saveFriends() {
        saveFile(friends, "friends.yml");
    }

    private static void saveFile(ConfigurationNode node, String name) {
        if (node == null) {
            logger.warn("Cannot save {}: ConfigurationNode is null", name);
            return;
        }
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(dataFolder.resolve(name))
                    .build();
            loader.save(node);
        } catch (IOException e) {
            logger.error("Failed to save {}: {}", name, e.getMessage(), e);
        }
    }

    public static ConfigurationNode getConfig() {
        return config;
    }

    public static ConfigurationNode getMessages() {
        return messages;
    }

    public static ConfigurationNode getAnnouncements() {
        return announcements;
    }

    public static ConfigurationNode getFriends() {
        return friends;
    }
}