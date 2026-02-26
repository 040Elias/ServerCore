package org.Elias040.servercore.listeners;

import org.Elias040.servercore.msg.MsgSession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MsgQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        MsgSession.remove(e.getPlayer().getUniqueId());
    }
}