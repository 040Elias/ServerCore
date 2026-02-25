package org.Elias040.servercore.nightvision;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class NightVisionListener implements Listener {

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (NightVisionManager.isEnabled(e.getPlayer())) {
            NightVisionManager.enable(e.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (NightVisionManager.isEnabled(e.getPlayer())) {
            NightVisionManager.enable(e.getPlayer());
        }
    }
}