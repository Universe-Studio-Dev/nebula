package github.universe.studio.nebula.bungee.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author DanielH131COL
 * @created 26/08/2025
 * @project nebula
 * @file ConnectionTracker
 */
public class ConnectionTracker {
    private static final Map<UUID, Long> loginTimes = new HashMap<>();

    public static void setLoginTime(UUID uuid) {
        loginTimes.put(uuid, System.currentTimeMillis());
    }

    public static long getLoginTime(UUID uuid) {
        return loginTimes.getOrDefault(uuid, System.currentTimeMillis());
    }
}