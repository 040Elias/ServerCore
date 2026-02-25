package org.Elias040.servercore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.title.Title;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;

public class BroadcastCommand implements CommandExecutor {

    private final Main plugin;

    public BroadcastCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("servercore.broadcast")) {
            if (sender instanceof Player p) {
                p.sendMessage(plugin.messages().component("no-permission", Map.of()));
                SoundUtil.playError(plugin, p);
            } else {
                sender.sendMessage(plugin.messages().raw("no-permission"));
            }
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(TextUtil.toComponent("&cUsage: /broadcast <message>"));
            return true;
        }

        String message = String.join(" ", args);

        Component titleComponent = plugin.messages().component("broadcast-title", Map.of());
        Component subtitleComponent = TextUtil.toComponent(message);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofSeconds(4),
                Duration.ofMillis(500)
        );
        Title title = Title.title(titleComponent, subtitleComponent, times);

        Component prefix = plugin.messages().component("broadcast-chat-prefix", Map.of());
        Component chatLine = Component.join(
                JoinConfiguration.separator(Component.text(" ")),
                prefix,
                TextUtil.toComponent(message)
        );

        String soundName = plugin.getConfig().getString("broadcast.sound", "block.note_block.bell");

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.showTitle(title);
            player.sendMessage(chatLine);
            SoundUtil.play(player, soundName);
        }

        return true;
    }
}