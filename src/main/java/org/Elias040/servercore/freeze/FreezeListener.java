package org.Elias040.servercore.freeze;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.Duration;
import java.util.Map;

public class FreezeListener implements Listener {

    private final Main plugin;

    public FreezeListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String name = player.getName(); // capture before scheduling
        // PlayerJoinEvent fires on global-region thread — PDC read and showTitle are entity ops
        player.getScheduler().run(plugin, t -> {
            FreezeManager.syncFromPdc(player);
            if (FreezeManager.isFrozen(player)) {
                player.showTitle(buildFreezeTitle());
                notifyStaff("freeze-staff-rejoin", name);
            }
        }, null);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        // Conservative: use entity scheduler to guarantee correct thread
        if (FreezeManager.isFrozen(player)) {
            player.getScheduler().run(plugin, t -> player.showTitle(buildFreezeTitle()), null);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        // isFrozen reads ConcurrentHashMap — thread-safe from any thread
        if (FreezeManager.isFrozen(player)) {
            notifyStaff("freeze-staff-leave", player.getName());
        }
        FreezeManager.evict(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        // Fires on player's entity thread — no scheduler needed
        Player player = e.getPlayer();
        if (!FreezeManager.isFrozen(player)) return;

        Location from = e.getFrom();
        Location to = e.getTo();

        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            e.setTo(from);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        // Fires on player's entity thread — no scheduler needed
        Player player = e.getPlayer();
        if (!FreezeManager.isFrozen(player)) return;
        if (player.hasPermission("servercore.freeze.bypass")) return;

        e.setCancelled(true);
        player.sendMessage(plugin.messages().component("freeze-command-blocked", Map.of()));
    }

    private void notifyStaff(String messageKey, String playerName) {
        // Pre-build message outside the loop — Component is immutable and thread-safe
        Component msg = plugin.messages().component(messageKey, Map.of("player", playerName));
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            // Schedule each sendMessage on the respective player's entity thread
            online.getScheduler().run(plugin, t -> {
                if (online.hasPermission("servercore.freeze.notify")) {
                    online.sendMessage(msg);
                }
            }, null);
        }
    }

    public Title buildFreezeTitle() {
        return Title.title(
                TextUtil.toComponent(plugin.messages().raw("freeze-title")),
                TextUtil.toComponent(plugin.messages().raw("freeze-subtitle")),
                Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofSeconds(999999),
                        Duration.ZERO
                )
        );
    }
}