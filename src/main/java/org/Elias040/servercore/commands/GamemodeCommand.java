package org.Elias040.servercore.commands;

import net.kyori.adventure.text.Component;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GamemodeCommand implements CommandExecutor, TabCompleter {

    private final GameMode fixedMode;
    private final Main plugin;

    public GamemodeCommand(Main plugin, GameMode fixedMode) {
        this.plugin = plugin;
        this.fixedMode = fixedMode;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        GameMode mode = resolveMode(sender, args);
        if (mode == null) return true;

        String[] playerArgs = (fixedMode != null) ? args
                : (args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);

        Player target = resolveTarget(sender, playerArgs);
        if (target == null) return true;

        String modeKey  = mode.name().toLowerCase(Locale.ROOT);
        String permNode = "servercore.gamemode." + modeKey;

        if (!sender.hasPermission("servercore.gamemode") && !sender.hasPermission(permNode)) {
            sendError(sender, "no-permission");
            return true;
        }

        applyGamemode(sender, target, mode);
        return true;
    }

    private GameMode resolveMode(CommandSender sender, String[] args) {
        if (fixedMode != null) return fixedMode;
        if (args.length == 0) {
            sender.sendMessage(TextUtil.toComponent(
                    "&cUsage: /gm <0|1|2|3|survival|creative|adventure|spectator> [player]"));
            return null;
        }
        GameMode mode = parseMode(args[0]);
        if (mode == null) sender.sendMessage(TextUtil.toComponent("&cUnknown gamemode: &f" + args[0]));
        return mode;
    }

    private Player resolveTarget(CommandSender sender, String[] playerArgs) {
        if (playerArgs.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(TextUtil.toComponent("&cConsole must specify a player."));
                return null;
            }
            return p;
        }
        if (!sender.hasPermission("servercore.gamemode.other")) {
            sendError(sender, "no-permission");
            return null;
        }

        Player target = Bukkit.getPlayerExact(playerArgs[0]);
        if (target == null) {
            sendError(sender, "player-not-found");
            return null;
        }
        return target;
    }

    private void applyGamemode(CommandSender sender, Player target, GameMode mode) {
        final boolean isSelf       = sender.equals(target);
        final String  senderName   = sender instanceof Player sp ? sp.getName() : "Console";
        final String  targetName   = target.getName();
        final String  gamemodeName = capitalize(mode.name());

        target.getScheduler().run(plugin, t -> {
            target.setGameMode(mode);
            target.sendMessage(plugin.messages().component("gamemode.self", Map.of(
                    "player",   targetName,
                    "gamemode", gamemodeName
            )));

            if (isSelf) return;

            Component msgOther = plugin.messages().component("gamemode.other", Map.of(
                    "player",   targetName,
                    "sender",   senderName,
                    "gamemode", gamemodeName
            ));

            if (sender instanceof Player sp) {
                sp.getScheduler().run(plugin, t2 -> sp.sendMessage(msgOther), null);
            } else {
                sender.sendMessage(msgOther);
            }
        }, null);
    }

    private void sendError(CommandSender sender, String key) {
        if (sender instanceof Player p) {
            p.sendMessage(plugin.messages().component(key, Map.of()));
            SoundUtil.playError(plugin, p);
        } else {
            sender.sendMessage(plugin.messages().raw(key));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (fixedMode != null) {
            if (args.length == 1 && sender.hasPermission("servercore.gamemode.other")) {
                return playerNames(args[0]);
            }
            return List.of();
        }
        if (args.length == 1) {
            List<String> modes = List.of("survival", "creative", "adventure", "spectator", "0", "1", "2", "3");
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return modes.stream().filter(m -> m.startsWith(prefix)).toList();
        }
        if (args.length == 2 && sender.hasPermission("servercore.gamemode.other")) {
            return playerNames(args[1]);
        }
        return List.of();
    }

    private List<String> playerNames(String prefix) {
        String lc = prefix.toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(lc))
                .toList();
    }

    private static GameMode parseMode(String input) {
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "0", "survival",  "s"  -> GameMode.SURVIVAL;
            case "1", "creative",  "c"  -> GameMode.CREATIVE;
            case "2", "adventure", "a"  -> GameMode.ADVENTURE;
            case "3", "spectator", "sp" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }
}