package org.Elias040.servercore.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public class MessageManager {

    private final JavaPlugin plugin;
    private FileConfiguration messages;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(file);
    }

    public String raw(String key) {
        if (messages == null) return "&cMissing messages.yml";
        return messages.getString(key, "&cMissing message: " + key);
    }

    public Component component(String key, Map<String, String> placeholders) {
        String s = raw(key);
        s = PlaceholderUtil.apply(s, placeholders);
        return TextUtil.toComponent(s);
    }

    public Component plainComponent(String legacyWithHex) {
        return TextUtil.toComponent(legacyWithHex);
    }
}