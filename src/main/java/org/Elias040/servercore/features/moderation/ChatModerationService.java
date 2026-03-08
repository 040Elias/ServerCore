package org.Elias040.servercore.features.moderation;

import net.kyori.adventure.text.Component;
import org.Elias040.servercore.Main;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatModerationService {

    private final Main plugin;
    private final ConcurrentHashMap<UUID, PlayerModerationState> states = new ConcurrentHashMap<>();
    private volatile ChatModerationConfig config;

    public ChatModerationService(Main plugin) {
        this.plugin = plugin;
        this.config = new ChatModerationConfig(plugin);
    }

    public void reload() {
        this.config = new ChatModerationConfig(plugin);
    }

    public void evictPlayer(UUID uuid) {
        states.remove(uuid);
    }

    public ViolationResult checkChat(Player player, String message) {
        ChatModerationConfig cfg = this.config;
        PlayerModerationState state = states.computeIfAbsent(player.getUniqueId(), id -> new PlayerModerationState());
        long now = System.currentTimeMillis();

        if (cfg.chatCooldownEnabled && !player.hasPermission(cfg.chatCooldownBypassPerm)) {
            long elapsed = now - state.getLastChatTimestamp();
            if (elapsed < cfg.chatCooldownMillis) {
                long remaining = cfg.chatCooldownMillis - elapsed;
                return new ViolationResult(
                        ViolationResult.ViolationType.CHAT_COOLDOWN,
                        Map.of("remaining_time", String.valueOf((remaining + 999) / 1000))
                );
            }
        }

        if (cfg.similarityEnabled && !player.hasPermission(cfg.similarityBypassPerm)) {
            String last = state.getLastMessage();
            if (last != null) {
                long elapsed = now - state.getLastMessageTimestamp();
                if (elapsed <= cfg.similarityTimeWindowMillis) {
                    String a = cap(message, cfg.similarityMaxCompareLength);
                    String b = cap(last, cfg.similarityMaxCompareLength);
                    double similarity = levenshteinSimilarity(a, b, cfg.similarityThreshold);
                    if (similarity >= cfg.similarityThreshold) {
                        return new ViolationResult(
                                ViolationResult.ViolationType.SIMILARITY,
                                Map.of()
                        );
                    }
                }
            }
        }

        if (cfg.uppercaseEnabled && !player.hasPermission(cfg.uppercaseBypassPerm)) {
            int percent = uppercasePercent(message);
            if (letterCount(message) >= cfg.minLettersToCheck && percent > cfg.maxUppercasePercent) {
                return new ViolationResult(
                        ViolationResult.ViolationType.UPPERCASE,
                        Map.of()
                );
            }
        }

        if (cfg.blacklistEnabled && !player.hasPermission(cfg.blacklistBypassPerm)) {
            if (cfg.blacklistMatcher.matches(message)) {
                return new ViolationResult(
                        ViolationResult.ViolationType.BLACKLIST,
                        Map.of()
                );
            }
        }

        state.setLastChatTimestamp(now);
        state.setLastMessage(message, now);
        return null;
    }

    public ViolationResult checkCommand(Player player, String normalizedCommand) {
        ChatModerationConfig cfg = this.config;

        if (player.hasPermission(cfg.commandCooldownBypassPerm)) return null;

        Long specific = cfg.commandCooldownPerCommand.get(normalizedCommand);
        long cooldown;
        if (specific != null) {
            cooldown = specific;
        } else if (cfg.commandCooldownGlobalEnabled) {
            cooldown = cfg.commandCooldownGlobalMillis;
        } else {
            return null;
        }

        if (cooldown <= 0) return null;

        PlayerModerationState state = states.computeIfAbsent(player.getUniqueId(), id -> new PlayerModerationState());
        long now = System.currentTimeMillis();
        long elapsed = now - state.getLastCommandTimestamp(normalizedCommand);

        if (elapsed < cooldown) {
            long remainingSeconds = (cooldown - elapsed + 999) / 1000;
            return new ViolationResult(
                    ViolationResult.ViolationType.COMMAND_COOLDOWN,
                    Map.of("time", String.valueOf(remainingSeconds))
            );
        }

        state.setLastCommandTimestamp(normalizedCommand, now);
        return null;
    }

    public void sendFeedback(Player player, ViolationResult result) {
        String key = switch (result.type()) {
            case CHAT_COOLDOWN    -> "moderation-chat-cooldown";
            case SIMILARITY       -> "moderation-similarity";
            case UPPERCASE        -> "moderation-uppercase";
            case BLACKLIST        -> "moderation-blacklist";
            case COMMAND_COOLDOWN -> "moderation-command-cooldown";
        };
        Component message = plugin.messages().component(key, result.placeholders());
        player.getScheduler().run(plugin, t -> player.sendMessage(message), null);
    }

    private static String cap(String s, int maxLength) {
        return s.length() > maxLength ? s.substring(0, maxLength) : s;
    }

    private static double levenshteinSimilarity(String a, String b, double threshold) {
        if (a.equals(b)) return 1.0;
        int la = a.length();
        int lb = b.length();
        if (la == 0 || lb == 0) return 0.0;
        int maxLen = Math.max(la, lb);
        if (1.0 - (double) Math.abs(la - lb) / maxLen < threshold) return 0.0;
        int dist = levenshteinDistance(a, la, b, lb);
        return 1.0 - (double) dist / maxLen;
    }

    private static int levenshteinDistance(String a, int la, String b, int lb) {
        int[] prev = new int[lb + 1];
        int[] curr = new int[lb + 1];
        for (int j = 0; j <= lb; j++) prev[j] = j;
        for (int i = 1; i <= la; i++) {
            curr[0] = i;
            for (int j = 1; j <= lb; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[lb];
    }

    private static int letterCount(String message) {
        int count = 0;
        for (int i = 0; i < message.length(); i++) {
            if (Character.isLetter(message.charAt(i))) count++;
        }
        return count;
    }

    private static int uppercasePercent(String message) {
        int letters = 0;
        int upper = 0;
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) upper++;
            }
        }
        return letters == 0 ? 0 : (upper * 100 / letters);
    }
}