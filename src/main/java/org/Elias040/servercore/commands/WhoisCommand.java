package org.Elias040.servercore.commands;

import net.kyori.adventure.text.Component;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.freeze.FreezeManager;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.Bukkit;
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
            if (sender instanceof Player p) {
                p.sendMessage(plugin.messages().component("no-permission", Map.of()));
                SoundUtil.playError(plugin, p);
            } else {
                sender.sendMessage(plugin.messages().raw("no-permission"));
            }
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(TextUtil.toComponent("&cUsage: /whois <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            if (sender instanceof Player p) {
                p.sendMessage(plugin.messages().component("player-not-found", Map.of()));
                SoundUtil.playError(plugin, p);
            } else {
                sender.sendMessage(plugin.messages().raw("player-not-found"));
            }
            return true;
        }

        boolean sensitive = sender.hasPermission("servercore.whois.sensitive");
        String targetName = target.getName();
        String targetUuid = target.getUniqueId().toString();

        target.getScheduler().run(plugin, t -> {
            List<Component> lines = new ArrayList<>();

            lines.add(TextUtil.toComponent("&#38c1fc&l--- Whois of " + targetName + " ---"));
            lines.add(Component.empty());
            lines.add(line("UUID",       targetUuid));
            lines.add(line("First Join", DATE_FORMAT.format(Instant.ofEpochMilli(target.getFirstPlayed()))));
            lines.add(line("Last Seen",  DATE_FORMAT.format(Instant.ofEpochMilli(target.getLastSeen()))));
            lines.add(line("Playtime",   formatPlaytime(target.getStatistic(Statistic.PLAY_ONE_MINUTE))));
            lines.add(line("Ping",       target.getPing() + "ms"));

            if (sensitive) {
                lines.add(Component.empty());

                String ip = target.getAddress() != null
                        ? target.getAddress().getAddress().getHostAddress()
                        : "unknown";
                String client = target.getClientBrandName() != null
                        ? target.getClientBrandName()
                        : "unknown";

                lines.add(line("IP",       ip));
                lines.add(line("Client",   client));
                lines.add(line("Frozen",   FreezeManager.isFrozen(target) ? "&ctrue" : "&afalse"));
                lines.add(line("Gamemode", capitalize(target.getGameMode().name())));
                lines.add(line("Position", formatLocation(target)));
            }

            lines.add(Component.empty());
            lines.add(TextUtil.toComponent("&#38c1fc&l---"));

            if (sender instanceof Player sp) {
                sp.getScheduler().run(plugin, t2 -> {
                    for (Component c : lines) sp.sendMessage(c);
                }, null);
            } else {
                for (Component c : lines) sender.sendMessage(c);
            }
        }, null);

        return true;
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

    private Component line(String key, String value) {
        return TextUtil.toComponent("&8» &7" + key + ": &#38c1fc" + value);
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
        return String.format("%s, %.1f, %.1f, %.1f",
                player.getWorld().getName(),
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ()
        );
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}