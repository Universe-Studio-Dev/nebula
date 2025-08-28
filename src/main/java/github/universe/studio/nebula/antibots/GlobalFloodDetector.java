package github.universe.studio.nebula.antibots;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author DanielH131COL
 * @created 28/08/2025
 * @project nebula
 * @file GlobalFloodDetector
 */

public class GlobalFloodDetector {
    private final Queue<Long> connections = new LinkedList<>();
    private final int limit;
    private final int window;
    private boolean emergency = false;

    public GlobalFloodDetector(int limit, int window) {
        this.limit = limit;
        this.window = window;
    }

    public boolean allowConnection(String ip) {
        long now = System.currentTimeMillis();

        connections.add(now);
        while (!connections.isEmpty() && now - connections.peek() > window) {
            connections.poll();
        }

        if (connections.size() > limit) {
            emergency = true;
        }

        if (emergency) {
            return false;
        }

        return true;
    }

    public boolean isEmergency() {
        return emergency;
    }
}