package org.Elias040.servercore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class TextUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private TextUtil() {}

    public static Component toComponent(String legacyWithHex) {
        return MM.deserialize(legacyToMiniMessage(legacyWithHex));
    }

    // Supports:
    //  - &a, &7, &l, &r ...
    //  - &#RRGGBB (your format)
    private static String legacyToMiniMessage(String input) {
        if (input == null) return "";

        String s = input;

        // &#RRGGBB -> <#RRGGBB>
        s = s.replaceAll("(?i)&\\#([0-9a-f]{6})", "<#$1>");

        // color codes
        s = s
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&o", "<italic>")
                .replace("&n", "<underlined>")
                .replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>")
                .replace("&r", "<reset>");

        // uppercase variants (optional safety)
        s = s
                .replace("&A", "<green>")
                .replace("&B", "<aqua>")
                .replace("&C", "<red>")
                .replace("&D", "<light_purple>")
                .replace("&E", "<yellow>")
                .replace("&F", "<white>")
                .replace("&L", "<bold>")
                .replace("&O", "<italic>")
                .replace("&N", "<underlined>")
                .replace("&M", "<strikethrough>")
                .replace("&K", "<obfuscated>")
                .replace("&R", "<reset>");

        return s;
    }
}