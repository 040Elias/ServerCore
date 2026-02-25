package org.Elias040.servercore.invsee;

import org.bukkit.inventory.Inventory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InvSeeSessions {

    private InvSeeSessions() {}

    public record Session(UUID viewer, UUID target, Inventory gui) {}

    private static final ConcurrentHashMap<UUID, Session> sessions = new ConcurrentHashMap<>();

    public static void open(UUID viewer, UUID target, Inventory gui) {
        sessions.put(viewer, new Session(viewer, target, gui));
    }

    public static Session get(UUID viewer) {
        return sessions.get(viewer);
    }

    public static void close(UUID viewer) {
        sessions.remove(viewer);
    }
}