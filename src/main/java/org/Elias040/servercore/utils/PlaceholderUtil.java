package org.Elias040.servercore.utils;

import java.util.Map;

public final class PlaceholderUtil {
    private PlaceholderUtil() {}

    public static String apply(String input, Map<String, String> placeholders) {
        if (input == null) return "";
        String out = input;
        for (var e : placeholders.entrySet()) {
            out = out.replace("%" + e.getKey() + "%", e.getValue());
        }
        return out;
    }
}