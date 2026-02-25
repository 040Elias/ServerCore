package org.Elias040.servercore.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class SoundUtil {
    private SoundUtil() {}

    public static void play(Player player, String soundName) {
        Sound sound = parseSound(soundName);
        if (sound == null) return;

        player.playSound(player.getLocation(), sound, 1f, 1f);
    }

    public static void playError(JavaPlugin plugin, Player player) {
        play(player, plugin.getConfig().getString("global.error-sound", ""));
    }

    public static void playTeleporting(JavaPlugin plugin, Player player) {
        play(player, plugin.getConfig().getString("spawn.teleporting-sound", ""));
    }

    /**
     * Unterstützt 3 Eingabe-Formate in der config:
     * 1) Enum-Style: ENTITY_VILLAGER_NO
     * 2) MC-Key ohne Namespace: entity.villager.no
     * 3) Voller NamespacedKey: minecraft:entity.villager.no
     */
    private static Sound parseSound(String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty()) return null;

        // 3) already namespaced, e.g. "minecraft:entity.villager.no"
        if (s.contains(":")) {
            NamespacedKey key = NamespacedKey.fromString(s.toLowerCase(Locale.ROOT));
            if (key == null) return null;
            return Registry.SOUNDS.get(key);
        }

        // 2) dot key without namespace, e.g. "entity.villager.no"
        if (s.contains(".")) {
            NamespacedKey key = NamespacedKey.minecraft(s.toLowerCase(Locale.ROOT));
            return Registry.SOUNDS.get(key);
        }

        // 1) enum-style, e.g. "ENTITY_VILLAGER_NO" -> "entity.villager.no"
        String keyPart = s.toLowerCase(Locale.ROOT).replace('_', '.');
        NamespacedKey key = NamespacedKey.minecraft(keyPart);
        return Registry.SOUNDS.get(key);
    }
}