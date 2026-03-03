package org.Elias040.servercore.features.warp;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class DelWarpCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public DelWarpCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (!p.hasPermission("servercore.warp.delwarp")) {
            p.sendMessage(plugin.messages().component("no-permission", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(TextUtil.toComponent("&cUsage: /delwarp <n>"));
            SoundUtil.playError(plugin, p);
            return true;
        }

        String name = args[0];
        String displayName = plugin.warps().getDisplayName(name);

        if (!plugin.warps().deleteWarp(name)) {
            p.sendMessage(plugin.messages().component("warp-not-found", Map.of("warp_name", displayName)));
            SoundUtil.playError(plugin, p);
            return true;
        }

        p.sendMessage(plugin.messages().component("delwarp-success", Map.of("warp_name", displayName)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return plugin.warps().getDisplayNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}