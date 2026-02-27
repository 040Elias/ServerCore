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
        // PlayerRespawnEvent fires on the player's entity thread — no scheduler needed
        if (NightVisionManager.isEnabled(e.getPlayer())) {
            NightVisionManager.enable(e.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // PlayerJoinEvent fires on global-region thread —
        // PDC read (isEnabled) and addPotionEffect (enable) are entity ops
        var player = e.getPlayer();
        player.getScheduler().run(plugin, t -> {
            if (NightVisionManager.isEnabled(player)) {
                NightVisionManager.enable(player);
            }
        }, null);
    }
}