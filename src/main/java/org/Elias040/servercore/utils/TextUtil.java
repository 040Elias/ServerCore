package org.Elias040.servercore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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

    public static Component buildWithLink(String raw, String url) {
        if (raw == null) return Component.empty();
        if (!raw.contains("%link%")) return toComponent(raw);

        String[] parts = raw.split("%link%", -1);

        // The URL component: clickable + hover tooltip
        Component urlComponent = Component.text(url)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(toComponent("&7Click to open")));

        Component result = Component.empty();
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result = result.append(toComponent(parts[i]));
            }
            if (i < parts.length - 1) {
                result = result.append(urlComponent);
            }
        }
        return result;
    }

    public static Component withClickUrl(Component component, String url) {
        return component
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(toComponent("&7Click to open")));
    }
}