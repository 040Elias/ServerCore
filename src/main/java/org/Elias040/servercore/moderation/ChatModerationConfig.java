package org.Elias040.servercore.moderation;

import org.Elias040.servercore.Main;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChatModerationConfig {

    public final boolean chatCooldownEnabled;
    public final long chatCooldownMillis;
    public final String chatCooldownBypassPerm;

    public final boolean similarityEnabled;
    public final double similarityThreshold;
    public final int similarityMaxCompareLength;
    public final long similarityTimeWindowMillis;
    public final String similarityBypassPerm;

    public final boolean uppercaseEnabled;
    public final int maxUppercasePercent;
    public final int minLettersToCheck;
    public final String uppercaseBypassPerm;

    public final boolean blacklistEnabled;
    public final BlacklistMatcher blacklistMatcher;
    public final String blacklistBypassPerm;

    public final boolean commandCooldownGlobalEnabled;
    public final long commandCooldownGlobalMillis;
    public final Map<String, Long> commandCooldownPerCommand;
    public final String commandCooldownBypassPerm;

    public ChatModerationConfig(Main plugin) {
        var cfg = plugin.getConfig();

        chatCooldownEnabled = cfg.getBoolean("moderation.chat-cooldown.enabled", true);
        chatCooldownMillis = cfg.getLong("moderation.chat-cooldown.cooldown-millis", 1000L);
        chatCooldownBypassPerm = cfg.getString("moderation.chat-cooldown.bypass-permission",
                "servercore.moderation.bypass.cooldown");

        similarityEnabled = cfg.getBoolean("moderation.similarity.enabled", true);
        similarityThreshold = cfg.getDouble("moderation.similarity.threshold", 0.85);
        similarityMaxCompareLength = cfg.getInt("moderation.similarity.max-compare-length", 100);
        similarityTimeWindowMillis = cfg.getLong("moderation.similarity.time-window-millis", 30000L);
        similarityBypassPerm = cfg.getString("moderation.similarity.bypass-permission",
                "servercore.moderation.bypass.similarity");

        uppercaseEnabled = cfg.getBoolean("moderation.uppercase.enabled", true);
        maxUppercasePercent = cfg.getInt("moderation.uppercase.max-uppercase-percent", 70);
        minLettersToCheck = cfg.getInt("moderation.uppercase.min-letters-to-check", 8);
        uppercaseBypassPerm = cfg.getString("moderation.uppercase.bypass-permission",
                "servercore.moderation.bypass.uppercase");

        blacklistEnabled = cfg.getBoolean("moderation.blacklist.enabled", true);
        List<String> words = cfg.getStringList("moderation.blacklist.words");
        String rawMode = cfg.getString("moderation.blacklist.match-mode", "CONTAINS");
        BlacklistMatcher.MatchMode matchMode = "WHOLE_WORD".equalsIgnoreCase(rawMode)
                ? BlacklistMatcher.MatchMode.WHOLE_WORD
                : BlacklistMatcher.MatchMode.CONTAINS;
        boolean ignoreCase = cfg.getBoolean("moderation.blacklist.ignore-case", true);
        blacklistMatcher = new BlacklistMatcher(words, matchMode, ignoreCase);
        blacklistBypassPerm = cfg.getString("moderation.blacklist.bypass-permission",
                "servercore.moderation.bypass.blacklist");

        commandCooldownGlobalEnabled = cfg.getBoolean("moderation.command-cooldown.global.enabled", true);
        commandCooldownGlobalMillis = cfg.getLong("moderation.command-cooldown.global.cooldown-millis", 500L);
        commandCooldownBypassPerm = cfg.getString("moderation.command-cooldown.bypass-permission",
                "servercore.moderation.bypass.command");

        ConfigurationSection perCmd = cfg.getConfigurationSection("moderation.command-cooldown.per-command");
        Map<String, Long> perMap = new HashMap<>();
        if (perCmd != null) {
            for (String key : perCmd.getKeys(false)) {
                perMap.put(key.toLowerCase(), perCmd.getLong(key));
            }
        }
        commandCooldownPerCommand = Collections.unmodifiableMap(perMap);
    }
}