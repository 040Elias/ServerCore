package org.Elias040.servercore.commands;

import net.kyori.adventure.text.Component;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.features.freeze.FreezeManager;
import org.Elias040.servercore.utils.SchedulerCompat;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WhoisCommand implements CommandExecutor, TabCompleter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private final Main plugin;

    public WhoisCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("servercore.whois.use")) {
            sendError(sender, "no-permission");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(TextUtil.toComponent("&cUsage: /whois <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sendError(sender, "player-not-found");
            return true;
        }

        boolean sensitive  = sender.hasPermission("servercore.whois.sensitive");
        String  targetName = target.getName();
        String  targetUuid = target.getUniqueId().toString();

        SchedulerCompat.runForEntity(plugin, target, () -> {
            List<Component> lines = buildWhoisLines(target, targetName, targetUuid, sensitive);
            deliverLines(sender, lines);
        });

        return true;
    }

    private List<Component> buildWhoisLines(Player target, String name, String uuid, boolean sensitive) {
        List<Component> lines = new ArrayList<>();

        lines.add(TextUtil.toComponent("&#38c1fc&l--- Whois of " + name + " ---"));
        lines.add(Component.empty());
        lines.add(line("UUID",       uuid));
        lines.add(line("First Join", DATE_FORMAT.format(Instant.ofEpochMilli(target.getFirstPlayed()))));
        lines.add(line("Last Seen",  DATE_FORMAT.format(Instant.ofEpochMilli(target.getLastSeen()))));
        lines.add(line("Playtime",   formatPlaytime(target.getStatistic(Statistic.PLAY_ONE_MINUTE))));
        lines.add(line("Ping",       target.getPing() + "ms"));

        if (sensitive) {
            lines.add(Component.empty());
            lines.add(line("IP",       resolveIp(target)));
            lines.add(line("Client",   resolveClient(target)));
            lines.add(line("Frozen",   FreezeManager.isFrozen(target) ? "&ctrue" : "&afalse"));
            lines.add(line("Gamemode", capitalize(target.getGameMode().name())));
            lines.add(line("Position", formatLocation(target)));
        }

        lines.add(Component.empty());
        lines.add(TextUtil.toComponent("&#38c1fc&l---"));
        return lines;
    }

    private void deliverLines(CommandSender sender, List<Component> lines) {
        if (sender instanceof Player sp) {
            SchedulerCompat.runForEntity(plugin, sp, () -> lines.forEach(sp::sendMessage));
        } else {
            lines.forEach(sender::sendMessage);
        }
    }

    private void sendError(CommandSender sender, String key) {
        if (sender instanceof Player p) {
            p.sendMessage(plugin.messages().component(key, Map.of()));
            SoundUtil.playError(plugin, p);
        } else {
            sender.sendMessage(plugin.messages().raw(key));
        }
    }

    private String resolveIp(Player target) {
        return target.getAddress() != null
                ? target.getAddress().getAddress().getHostAddress()
                : "unknown";
    }

    private String resolveClient(Player target) {
        return target.getClientBrandName() != null ? target.getClientBrandName() : "unknown";
    }

    private String formatPlaytime(int ticks) {
        long seconds = ticks / 20L;
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        long days    = hours / 24;

        hours   = hours % 24;
        minutes = minutes % 60;

        if (days > 0)  return days + "d " + hours + "h " + minutes + "m";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    private String formatLocation(Player player) {
        Location loc = player.getLocation();
        return String.format("%s, %.1f, %.1f, %.1f",
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ()
        );
    }

    private Component line(String key, String value) {
        return TextUtil.toComponent("&8» &7" + key + ": &#38c1fc" + value);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}