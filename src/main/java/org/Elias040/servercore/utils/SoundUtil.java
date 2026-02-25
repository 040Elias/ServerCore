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

    private static Sound parseSound(String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty()) return null;

        if (s.contains(":")) {
            NamespacedKey key = NamespacedKey.fromString(s.toLowerCase(Locale.ROOT));
            if (key == null) return null;
            return Registry.SOUNDS.get(key);
        }

        if (s.contains(".")) {
            NamespacedKey key = NamespacedKey.minecraft(s.toLowerCase(Locale.ROOT));
            return Registry.SOUNDS.get(key);
        }

        String keyPart = s.toLowerCase(Locale.ROOT).replace('_', '.');
        NamespacedKey key = NamespacedKey.minecraft(keyPart);
        return Registry.SOUNDS.get(key);
    }
}