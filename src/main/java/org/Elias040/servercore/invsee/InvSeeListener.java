package org.Elias040.servercore.invsee;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

public class InvSeeListener implements Listener {

    private boolean isInvSeeTop(Player viewer, InventoryView view) {
        var session = InvSeeSessions.get(viewer.getUniqueId());
        if (session == null) return false;

        return view.getTopInventory() == session.gui();
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player viewer)) return;
        if (!isInvSeeTop(viewer, e.getView())) return;

        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player viewer)) return;
        if (!isInvSeeTop(viewer, e.getView())) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player viewer)) return;

        var session = InvSeeSessions.get(viewer.getUniqueId());
        if (session == null) return;

        if (e.getView().getTopInventory() != session.gui()) return;

        InvSeeSessions.close(viewer.getUniqueId());
    }
}