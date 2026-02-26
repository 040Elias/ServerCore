package org.Elias040.servercore.listeners;

import org.Elias040.servercore.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class JoinLeaveListener implements Listener {


    private final Main plugin;

    public JoinLeaveListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        e.joinMessage(null);

        if (player.hasPermission("servercore.join.silent")) return;

        if (!player.hasPlayedBefore()) {
            plugin.getServer().sendMessage(plugin.messages().component("first-join", Map.of(
                    "player", player.getName()
            )));
        } else {
            plugin.getServer().sendMessage(plugin.messages().component("join", Map.of(
                    "player", player.getName()
            )));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        e.quitMessage(null);

        if (player.hasPermission("servercore.join.silent")) return;

        plugin.getServer().sendMessage(plugin.messages().component("leave", Map.of(
                "player", player.getName()
        )));
    }
}