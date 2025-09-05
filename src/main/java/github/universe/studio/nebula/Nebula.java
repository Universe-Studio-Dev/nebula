package github.universe.studio.nebula;

import github.universe.studio.nebula.velocity.VelocityPlugin;
import github.universe.studio.nebula.velocity.utils.ConfigManager;

/**
 * @author DanielH131COL
 * @created 4/09/2025
 * @project nebula
 * @file Nebula
 */
public class Nebula {

    public static void initBungee(Object plugin) {
    }

    public static void initVelocity(VelocityPlugin plugin) {
        ConfigManager.init(plugin);
        ConfigManager.load();
    }
}