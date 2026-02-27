package org.Elias040.servercore.commands;

import net.kyori.adventure.text.Component;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.msg.MsgSession;
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

public class MsgCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public MsgCommand(Main plugin) {
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

        if (args.length < 2) {
            p.sendMessage(TextUtil.toComponent("&cUsage: /msg <player> <message>"));
            SoundUtil.playError(plugin, p);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            p.sendMessage(plugin.messages().component("player-not-found", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        if (target.equals(p)) {
            p.sendMessage(plugin.messages().component("msg-self", Map.of()));
            SoundUtil.playError(plugin, p);
            return true;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        // Pre-build both components — Component is immutable, safe across threads
        Component senderMsg = plugin.messages().component("msg-sender", Map.of(
                "receiver", target.getName(),
                "message", message
        ));
        Component receiverMsg = plugin.messages().component("msg-receiver", Map.of(
                "sender", p.getName(),
                "message", message
        ));

        // Sender is already on their own entity thread
        p.sendMessage(senderMsg);

        // Target may be in a different region — schedule on their entity thread
        target.getScheduler().run(plugin, t -> target.sendMessage(receiverMsg), null);

        MsgSession.set(p.getUniqueId(), target.getUniqueId());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(sender))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}