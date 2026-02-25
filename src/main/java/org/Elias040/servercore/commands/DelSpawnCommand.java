package org.Elias040.servercore.commands;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SoundUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class DelSpawnCommand implements CommandExecutor {

    private final Main plugin;

    public DelSpawnCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (!p.hasPermission("servercore.spawn.setspawn")) {
            p.sendMessage(plugin.messages().component("no-permission", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(plugin.messages().plainComponent("&cUsage: /delspawn <name>"));
            SoundUtil.playError(plugin, p);
            return true;
        }

        String name = args[0];

        if (!plugin.spawns().deleteSpawn(name)) {
            p.sendMessage(plugin.messages().component("spawn-not-found", Map.of(
                    "spawn_name", name
            )));
            SoundUtil.playError(plugin, p);
            return true;
        }

        p.sendMessage(plugin.messages().component("delspawn-success", Map.of(
                "spawn_name", name
        )));
        return true;
    }
}