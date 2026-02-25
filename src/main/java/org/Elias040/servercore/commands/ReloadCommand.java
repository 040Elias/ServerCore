package org.Elias040.servercore.commands;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final Main plugin;

    public ReloadCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("servercore.reload")) {
            sender.sendMessage(TextUtil.toComponent("&cYou don't have permission."));
            return true;
        }

        plugin.reloadConfig();
        plugin.messages().loadMessages();

        sender.sendMessage(TextUtil.toComponent("&7Successfully &#38c1fcreloaded&7 the configs."));
        return true;
    }
}