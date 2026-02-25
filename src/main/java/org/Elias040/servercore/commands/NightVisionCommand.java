package org.Elias040.servercore.commands;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.nightvision.NightVisionManager;
import org.Elias040.servercore.utils.SoundUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class NightVisionCommand implements CommandExecutor {

    private final Main plugin;

    public NightVisionCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (!p.hasPermission("servercore.nightvision")) {
            p.sendMessage(plugin.messages().component("no-permission", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        boolean enabled = NightVisionManager.toggle(p);
        String key = enabled ? "nightvision-enabled" : "nightvision-disabled";

        p.sendMessage(plugin.messages().component(key, Map.of()));
        p.sendActionBar(plugin.messages().component(key, Map.of()));

        return true;
    }
}