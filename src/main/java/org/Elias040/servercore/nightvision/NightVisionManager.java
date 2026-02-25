package org.Elias040.servercore.nightvision;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class NightVisionManager {

    private NightVisionManager() {}

    private static final NamespacedKey KEY = new NamespacedKey("servercore", "nightvision");
    private static final PotionEffect EFFECT = new PotionEffect(
            PotionEffectType.NIGHT_VISION,
            Integer.MAX_VALUE,
            1,
            false,
            false,
            false
    );

    public static boolean toggle(Player player) {
        if (isEnabled(player)) {
            disable(player);
            return false;
        } else {
            enable(player);
            return true;
        }
    }

    public static void enable(Player player) {
        player.addPotionEffect(EFFECT);
        player.getPersistentDataContainer().set(KEY, PersistentDataType.BOOLEAN, true);
    }

    public static void disable(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.getPersistentDataContainer().remove(KEY);
    }

    public static boolean isEnabled(Player player) {
        return player.getPersistentDataContainer().getOrDefault(KEY, PersistentDataType.BOOLEAN, false);
    }
}