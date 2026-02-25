package org.Elias040.servercore.freeze;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FreezeManager {

    private FreezeManager() {}

    private static final NamespacedKey KEY = new NamespacedKey("servercore", "frozen");

    private static final Set<UUID> frozenOnline = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void freeze(Player player) {
        player.getPersistentDataContainer().set(KEY, PersistentDataType.BOOLEAN, true);
        frozenOnline.add(player.getUniqueId());
    }

    public static void unfreeze(Player player) {
        player.getPersistentDataContainer().remove(KEY);
        frozenOnline.remove(player.getUniqueId());
    }

    public static boolean isFrozen(Player player) {
        return frozenOnline.contains(player.getUniqueId());
    }

    public static void syncFromPdc(Player player) {
        boolean frozen = player.getPersistentDataContainer()
                .getOrDefault(KEY, PersistentDataType.BOOLEAN, false);
        if (frozen) {
            frozenOnline.add(player.getUniqueId());
        } else {
            frozenOnline.remove(player.getUniqueId());
        }
    }

    public static void evict(UUID uuid) {
        frozenOnline.remove(uuid);
    }
}