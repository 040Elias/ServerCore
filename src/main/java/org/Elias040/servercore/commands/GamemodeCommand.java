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

        GameMode mode;
        String[] playerArgs;

        if (fixedMode != null) {
            mode = fixedMode;
            playerArgs = args;
        } else {
            if (args.length == 0) {
                sender.sendMessage(TextUtil.toComponent("&cUsage: /gm <0|1|2|3|survival|creative|adventure|spectator> [player]"));
                return true;
            }
            mode = parseMode(args[0]);
            if (mode == null) {
                sender.sendMessage(TextUtil.toComponent("&cUnknown gamemode: &f" + args[0]));
                return true;
            }
            playerArgs = args.length > 1
                    ? java.util.Arrays.copyOfRange(args, 1, args.length)
                    : new String[0];
        }

        Player target;
        if (playerArgs.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(TextUtil.toComponent("&cConsole must specify a player."));
                return true;
            }
            target = p;
        } else {
            if (!sender.hasPermission("servercore.gamemode.other")) {
                if (sender instanceof Player p) {
                    p.sendMessage(plugin.messages().component("no-permission", Map.of()));
                    SoundUtil.playError(plugin, p);
                } else {
                    sender.sendMessage(plugin.messages().raw("no-permission"));
                }
                return true;
            }
            target = Bukkit.getPlayerExact(playerArgs[0]);
            if (target == null || !target.isOnline()) {
                if (sender instanceof Player p) {
                    p.sendMessage(plugin.messages().component("player-not-found", Map.of()));
                    SoundUtil.playError(plugin, p);
                } else {
                    sender.sendMessage(plugin.messages().raw("player-not-found"));
                }
                return true;
            }
        }

        String permNode = "servercore.gamemode." + mode.name().toLowerCase(Locale.ROOT);
        if (!sender.hasPermission("servercore.gamemode") && !sender.hasPermission(permNode)) {
            if (sender instanceof Player p) {
                p.sendMessage(plugin.messages().component("no-permission", Map.of()));
                SoundUtil.playError(plugin, p);
            } else {
                sender.sendMessage(plugin.messages().raw("no-permission"));
            }
            return true;
        }

        final GameMode finalMode = mode;
        final boolean isSelf = sender.equals(target);
        final String senderName = sender instanceof Player sp ? sp.getName() : "Console";
        final String targetName = target.getName();
        final String gamemodeName = capitalize(mode.name());

        target.getScheduler().run(plugin, t -> {
            target.setGameMode(finalMode);

            Component msgSelf = plugin.messages().component("gamemode.self", Map.of(
                    "player", targetName,
                    "gamemode", gamemodeName
            ));
            target.sendMessage(msgSelf);

            if (!isSelf && sender instanceof Player sp) {
                sp.getScheduler().run(plugin, t2 -> {
                    Component msgOther = plugin.messages().component("gamemode.other", Map.of(
                            "player", targetName,
                            "sender", senderName,
                            "gamemode", gamemodeName
                    ));
                    sp.sendMessage(msgOther);
                }, null);
            } else if (!isSelf) {
                Component msgOther = plugin.messages().component("gamemode.other", Map.of(
                        "player", targetName,
                        "sender", senderName,
                        "gamemode", gamemodeName
                ));
                sender.sendMessage(msgOther);
            }
        }, null);

        return true;
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
        return s.charAt(0) + s.substring(1).toLowerCase(Locale.ROOT);
    }
}
