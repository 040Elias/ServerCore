package org.Elias040.servercore.commands;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.freeze.FreezeListener;
import org.Elias040.servercore.freeze.FreezeManager;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class FreezeCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final FreezeListener listener;

    public FreezeCommand(Main plugin, FreezeListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("servercore.freeze")) {
            if (sender instanceof Player p) {
                p.sendMessage(plugin.messages().component("no-permission", Map.of()));
                SoundUtil.playError(plugin, p);
            } else {
                sender.sendMessage(plugin.messages().raw("no-permission"));
            }
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(TextUtil.toComponent("&cUsage: /freeze <player>"));
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

        if (FreezeManager.isFrozen(target)) {
            if (sender instanceof Player p) {
                p.sendMessage(plugin.messages().component("freeze-already-frozen", Map.of("player", target.getName())));
                SoundUtil.playError(plugin, p);
            } else {
                sender.sendMessage(plugin.messages().raw("freeze-already-frozen").replace("%player%", target.getName()));
            }
            return true;
        }

        FreezeManager.freeze(target);
        target.showTitle(listener.buildFreezeTitle());

        if (sender instanceof Player p) {
            p.sendMessage(plugin.messages().component("freeze-frozen", Map.of("player", target.getName())));
        } else {
            sender.sendMessage(plugin.messages().raw("freeze-frozen").replace("%player%", target.getName()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !FreezeManager.isFrozen(p))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}