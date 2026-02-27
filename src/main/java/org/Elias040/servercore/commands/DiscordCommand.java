package org.Elias040.servercore.commands;

import net.kyori.adventure.text.Component;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class DiscordCommand implements CommandExecutor {

    private final Main plugin;

    public DiscordCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (!p.hasPermission("servercore.discord")) {
            p.sendMessage(plugin.messages().component("no-permission", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        List<?> raw = plugin.getConfig().getList("discord.lines");
        if (raw == null || raw.isEmpty()) {
            p.sendMessage(TextUtil.toComponent("&cNo discord message configured. Add 'discord.lines' to config.yml."));
            return true;
        }

        List<Component> lines = raw.stream()
                .map(obj -> {
                    String s = obj == null ? "" : obj.toString();
                    String trimmed = s.trim();
                    Component c = TextUtil.toComponent(s);
                    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                        c = TextUtil.withClickUrl(c, trimmed);
                    }
                    return c;
                })
                .toList();

        String sound = plugin.getConfig().getString("discord.sound", "");

        for (Component line : lines) {
            p.sendMessage(line);
        }
        SoundUtil.play(p, sound);

        return true;
    }
}