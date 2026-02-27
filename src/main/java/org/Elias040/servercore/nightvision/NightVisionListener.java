package org.Elias040.servercore.nightvision;

import org.Elias040.servercore.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class NightVisionListener implements Listener {

    private final Main plugin;

    public NightVisionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        var player = e.getPlayer();
        player.getScheduler().runDelayed(plugin, t -> {
            if (NightVisionManager.isEnabled(player)) {
                NightVisionManager.enable(player);
            }
        }, null, 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        player.getScheduler().run(plugin, t -> {
            if (NightVisionManager.isEnabled(player)) {
                NightVisionManager.enable(player);
            }
        }, null);
    }
}