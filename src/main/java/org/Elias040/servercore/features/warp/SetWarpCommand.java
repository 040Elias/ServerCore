package org.Elias040.servercore.features.warp;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class SetWarpCommand implements CommandExecutor {

    private final Main plugin;

    public SetWarpCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (!p.hasPermission("servercore.warp.setwarp")) {
            p.sendMessage(plugin.messages().component("no-permission", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(TextUtil.toComponent("&cUsage: /setwarp <n>"));
            SoundUtil.playError(plugin, p);
            return true;
        }

        String name = args[0];

        if (plugin.warps().exists(name)) {
            p.sendMessage(plugin.messages().component("warp-already-exists", Map.of("warp_name", name)));
            SoundUtil.playError(plugin, p);
            return true;
        }

        plugin.warps().setWarp(name, p.getLocation());

        p.sendMessage(plugin.messages().component("setwarp-success", Map.of("warp_name", name)));
        return true;
    }
}
