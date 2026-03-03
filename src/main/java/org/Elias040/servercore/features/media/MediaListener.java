package org.Elias040.servercore.features.media;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.PlaceholderUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class MediaListener implements Listener {

    private static final int ACTION_SLOT = 13;
    private final Main plugin;

    public MediaListener(Main plugin) {
        this.plugin = plugin;
    }

    private boolean isMediaGui(Inventory inv) {
        return inv != null && inv.getHolder() instanceof MediaGui;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        Inventory top = e.getView().getTopInventory();
        if (!isMediaGui(top)) return;

        e.setCancelled(true);

        if (e.getClick() == ClickType.NUMBER_KEY
                || e.getClick() == ClickType.DROP
                || e.getClick() == ClickType.CONTROL_DROP
                || e.getClick() == ClickType.SWAP_OFFHAND
                || e.getClick() == ClickType.DOUBLE_CLICK
                || e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            return;
        }

        if (e.getClickedInventory() != top) return;

        if (e.getRawSlot() == ACTION_SLOT) {
            handleActionClick(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!isMediaGui(top)) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoveItem(InventoryMoveItemEvent e) {
        if (isMediaGui(e.getDestination()) || isMediaGui(e.getSource())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        Inventory top = p.getOpenInventory().getTopInventory();
        if (isMediaGui(top)) {
            e.setCancelled(true);
        }
    }

    private void handleActionClick(Player player) {
        var cfg = plugin.getConfig();
        String rawCmd = cfg.getString("media.item.command", "");
        if (rawCmd == null || rawCmd.isBlank()) return;

        String resolvedCmd = PlaceholderUtil.apply(rawCmd, Map.of("player", player.getName()));
        if (resolvedCmd.startsWith("/")) resolvedCmd = resolvedCmd.substring(1);

        boolean runAsConsole = cfg.getBoolean("media.item.runAsConsole", false);
        final String finalCmd = resolvedCmd;

        player.getScheduler().run(plugin, t -> player.closeInventory(), null);

        if (runAsConsole) {
            plugin.getServer().getGlobalRegionScheduler().execute(plugin,
                    () -> plugin.getServer().dispatchCommand(
                            plugin.getServer().getConsoleSender(), finalCmd));
        } else {
            player.getScheduler().run(plugin, t -> player.performCommand(finalCmd), null);
        }
    }
}