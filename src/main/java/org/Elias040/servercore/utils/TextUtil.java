package org.Elias040.servercore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class TextUtil {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    private TextUtil() {}

    public static Component toComponent(String input) {
        if (input == null) return Component.empty();
        return LEGACY.deserialize(input);
    }
}