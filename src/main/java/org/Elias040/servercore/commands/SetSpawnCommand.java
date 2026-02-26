package org.Elias040.servercore.commands;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class SetSpawnCommand implements CommandExecutor {

    private final Main plugin;

    public SetSpawnCommand(Main plugin) {
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
            p.sendMessage(TextUtil.toComponent("&cUsage: /setspawn <name>"));
            SoundUtil.playError(plugin, p);
            return true;
        }

        String name = args[0];

        if (plugin.spawns().exists(name)) {
            p.sendMessage(plugin.messages().component("spawn-already-exists", Map.of(
                    "spawn_name", name
            )));
            SoundUtil.playError(plugin, p);
            return true;
        }

        plugin.spawns().setSpawn(name, p.getLocation());

        p.sendMessage(plugin.messages().component("setspawn-success", Map.of(
                "spawn_name", name
        )));
        return true;
    }
}