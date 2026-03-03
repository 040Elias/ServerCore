package org.Elias040.servercore.features.spawn;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class SpawnQuitListener implements Listener {

    private final SpawnCommand spawnCommand;

    public SpawnQuitListener(SpawnCommand spawnCommand) {
        this.spawnCommand = spawnCommand;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        spawnCommand.cleanup(e.getPlayer().getUniqueId());
    }
}