package org.Elias040.servercore.commands;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class PingCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public PingCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(plugin.messages().component("ping-self", Map.of(
                    "ping", String.valueOf(p.getPing())
            )));
            return true;
        }

        if (!p.hasPermission("servercore.ping.other")) {
            p.sendMessage(plugin.messages().component("no-permission", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            p.sendMessage(plugin.messages().component("player-not-found", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        p.sendMessage(plugin.messages().component("ping-other", Map.of(
                "player", target.getName(),
                "ping", String.valueOf(target.getPing())
        )));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("servercore.ping.other")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}