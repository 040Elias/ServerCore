package org.Elias040.servercore.moderation;

import java.util.concurrent.ConcurrentHashMap;

public final class PlayerModerationState {

    private volatile long lastChatTimestamp = 0;
    private volatile long lastMessageTimestamp = 0;
    private volatile String lastMessage = null;
    private final ConcurrentHashMap<String, Long> commandTimestamps = new ConcurrentHashMap<>();

    public long getLastChatTimestamp() {
        return lastChatTimestamp;
    }

    public void setLastChatTimestamp(long timestamp) {
        this.lastChatTimestamp = timestamp;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String message, long timestamp) {
        this.lastMessage = message;
        this.lastMessageTimestamp = timestamp;
    }

    public long getLastCommandTimestamp(String command) {
        return commandTimestamps.getOrDefault(command, 0L);
    }

    public void setLastCommandTimestamp(String command, long timestamp) {
        commandTimestamps.put(command, timestamp);
    }
}