package org.Elias040.servercore.commands;

import net.kyori.adventure.text.Component;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.PlaceholderUtil;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LiveCommand implements CommandExecutor {

    private final Main plugin;

    private final ConcurrentHashMap<UUID, Long> lastUseMillis = new ConcurrentHashMap<>();

    public LiveCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (!p.hasPermission("servercore.live")) {
            p.sendMessage(plugin.messages().component("no-permission", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(TextUtil.toComponent("&cUsage: /live <url>"));
            SoundUtil.playError(plugin, p);
            return true;
        }

        String url = args[0];
        if (!isValidUrl(url)) {
            p.sendMessage(plugin.messages().component("live-invalid-url", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        int cooldownMinutes = plugin.getConfig().getInt("live.cooldown-minutes", 15);
        long cooldownMillis = cooldownMinutes * 60_000L;
        long now = System.currentTimeMillis();
        long last = lastUseMillis.getOrDefault(p.getUniqueId(), 0L);
        long elapsed = now - last;

        if (elapsed < cooldownMillis) {
            long remainingSec = (cooldownMillis - elapsed) / 1000L;
            long remainingMin = remainingSec / 60;
            long remainingSecPart = remainingSec % 60;
            String remaining = remainingMin > 0
                    ? remainingMin + "m " + remainingSecPart + "s"
                    : remainingSecPart + "s";
            p.sendMessage(plugin.messages().component("live-cooldown", Map.of("remaining", remaining)));
            SoundUtil.playError(plugin, p);
            return true;
        }

        lastUseMillis.put(p.getUniqueId(), now);

        List<?> rawLines = plugin.getConfig().getList("live.lines");
        if (rawLines == null || rawLines.isEmpty()) {
            p.sendMessage(TextUtil.toComponent("&cNo live message configured. Add 'live.lines' to config.yml."));
            return true;
        }

        String playerName = p.getName();
        String sound = plugin.getConfig().getString("live.sound", "");

        List<Component> lines = rawLines.stream()
                .map(obj -> {
                    String s = obj == null ? "" : obj.toString();
                    s = PlaceholderUtil.apply(s, Map.of("player", playerName));
                    return TextUtil.buildWithLink(s, url);
                })
                .toList();

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.getScheduler().run(plugin, t -> {
                for (Component line : lines) {
                    online.sendMessage(line);
                }
                SoundUtil.play(online, sound);
            }, null);
        }

        return true;
    }

    public void cleanup(UUID uuid) {
        lastUseMillis.remove(uuid);
    }

    private static boolean isValidUrl(String url) {
        if (url == null) return false;
        try {
            java.net.URI uri = new java.net.URI(url);
            String scheme = uri.getScheme();
            return ("http".equals(scheme) || "https".equals(scheme))
                    && uri.getHost() != null
                    && !uri.getHost().isEmpty();
        } catch (java.net.URISyntaxException e) {
            return false;
        }
    }
}