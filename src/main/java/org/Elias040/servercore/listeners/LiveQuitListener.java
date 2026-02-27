package org.Elias040.servercore.listeners;

import org.Elias040.servercore.commands.LiveCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LiveQuitListener implements Listener {

    private final LiveCommand liveCommand;

    public LiveQuitListener(LiveCommand liveCommand) {
        this.liveCommand = liveCommand;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        liveCommand.cleanup(e.getPlayer().getUniqueId());
    }
}