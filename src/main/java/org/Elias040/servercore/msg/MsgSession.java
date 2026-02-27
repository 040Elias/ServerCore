package org.Elias040.servercore.msg;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MsgSession {

    private MsgSession() {}

    private static final ConcurrentHashMap<UUID, UUID> lastPartner = new ConcurrentHashMap<>();

    public static void set(UUID a, UUID b) {
        lastPartner.put(a, b);
        lastPartner.put(b, a);
    }

    public static UUID getPartner(UUID uuid) {
        return lastPartner.get(uuid);
    }

    public static void remove(UUID uuid) {
        UUID partner = lastPartner.remove(uuid);
        if (partner != null) {
            lastPartner.remove(partner, uuid);
        }
    }
}