package github.universe.studio.nebula.bungee.utils;

/**
 * @author DanielH131COL
 * @created 28/08/2025
 * @project nebula
 * @file TimeUtils
 */
public class TimeUtils {
    public static String formatMillis(long ms) {
        long s = ms / 1000;
        long m = s / 60;
        return m + "m " + (s % 60) + "s";
    }
}