package github.universe.studio.nebula.others;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author DanielH131COL
 * @created 28/08/2025
 * @project nebula
 * @file ConnectionLimiter
 */

public class ConnectionLimiter {
    private final Map<String, Long> lastConnection = new ConcurrentHashMap<>();
    private final int delay;

    public ConnectionLimiter(int delay) {
        this.delay = delay;
    }

    public boolean allowConnection(String ip) {
        long now = System.currentTimeMillis();
        if (lastConnection.containsKey(ip)) {
            long last = lastConnection.get(ip);
            if (now - last < delay) {
                return false;
            }
        }
        lastConnection.put(ip, now);
        return true;
    }
}