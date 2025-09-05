package github.universe.studio.nebula.velocity.listeners;

import github.universe.studio.nebula.velocity.utils.CC;

import java.util.regex.Pattern;

/**
 * @author DanielH131COL
 * @created 04/09/2025
 * @project nebula
 * @file PlatformDetector
 */
public class PlatformDetector {

    public String detectPlatform(String link) {
        if (Pattern.compile(".*twitch\\.tv.*", Pattern.CASE_INSENSITIVE).matcher(link).matches()) {
            return "Twitch";
        } else if (Pattern.compile(".*kick\\.com.*", Pattern.CASE_INSENSITIVE).matcher(link).matches()) {
            return "Kick";
        } else if (Pattern.compile(".*(?:youtube\\.com|youtu\\.be).*", Pattern.CASE_INSENSITIVE).matcher(link).matches()) {
            return "YouTube";
        }
        return null;
    }

    public String getPlatformColor(String platform) {
        switch (platform) {
            case "Twitch":
                return CC.translate("&5");
            case "Kick":
                return CC.translate("&a");
            case "YouTube":
                return CC.translate("&c");
            default:
                return CC.translate("&f");
        }
    }

    public String getPlatformIcon(String platform) {
        switch (platform) {
            case "Twitch":
                return CC.translate("&5✦");
            case "Kick":
                return CC.translate("&a⚡");
            case "YouTube":
                return CC.translate("&c▶");
            default:
                return CC.translate("&f•");
        }
    }
}