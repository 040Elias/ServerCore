package org.Elias040.servercore.moderation;

import java.util.Map;

public record ViolationResult(ViolationType type, Map<String, String> placeholders) {

    public enum ViolationType {
        CHAT_COOLDOWN,
        SIMILARITY,
        UPPERCASE,
        BLACKLIST,
        COMMAND_COOLDOWN
    }
}