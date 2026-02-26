package org.Elias040.servercore.commands;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.msg.MsgSession;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ReplyCommand implements CommandExecutor {

    private final Main plugin;

    public ReplyCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (!p.hasPermission("servercore.msg")) {
            p.sendMessage(plugin.messages().component("no-permission", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(TextUtil.toComponent("&cUsage: /r <message>"));
            SoundUtil.playError(plugin, p);
            return true;
        }

        UUID partnerUuid = MsgSession.getPartner(p.getUniqueId());
        if (partnerUuid == null) {
            p.sendMessage(plugin.messages().component("msg-no-reply", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        Player target = Bukkit.getPlayer(partnerUuid);
        if (target == null || !target.isOnline()) {
            p.sendMessage(plugin.messages().component("msg-reply-offline", Map.of()));
            SoundUtil.playError(plugin, p);
            MsgSession.remove(p.getUniqueId());
            return true;
        }

        String message = String.join(" ", args);

        p.sendMessage(plugin.messages().component("msg-sender", Map.of(
                "receiver", target.getName(),
                "message", message
        )));

        target.sendMessage(plugin.messages().component("msg-receiver", Map.of(
                "sender", p.getName(),
                "message", message
        )));

        MsgSession.set(p.getUniqueId(), target.getUniqueId());

        return true;
    }
}