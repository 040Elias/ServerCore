package org.Elias040.servercore.features.warp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class WarpQuitListener implements Listener {

    private final WarpCommand warpCommand;

    public WarpQuitListener(WarpCommand warpCommand) {
        this.warpCommand = warpCommand;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        warpCommand.cleanup(e.getPlayer().getUniqueId());
    }
}
