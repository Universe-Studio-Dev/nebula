package github.universe.studio.nebula.bungee.others;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author DanielH131COL
 * @created 28/08/2025
 * @project nebula
 * @file BlacklistManager
 */
public class BlacklistManager {
    private final Map<String, Long> bannedIPs = new ConcurrentHashMap<>();
    private final long banDuration;

    public BlacklistManager(long banDuration) {
        this.banDuration = banDuration;
    }

    public void ban(String ip) {
        bannedIPs.put(ip, System.currentTimeMillis() + banDuration);
    }

    public boolean isBanned(String ip) {
        if (!bannedIPs.containsKey(ip)) return false;
        if (System.currentTimeMillis() > bannedIPs.get(ip)) {
            bannedIPs.remove(ip);
            return false;
        }
        return true;
    }
}