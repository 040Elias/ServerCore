package org.Elias040.servercore.utils;

import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigUtil {
    private ConfigUtil() {}

    public static int getInt(JavaPlugin plugin, String path, int def) {
        Object raw = plugin.getConfig().get(path);
        if (raw instanceof Number n) return n.intValue();
        if (raw instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
                return def;
            }
        }
        return plugin.getConfig().getInt(path, def);
    }
}