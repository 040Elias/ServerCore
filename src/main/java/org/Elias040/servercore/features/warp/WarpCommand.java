package org.Elias040.servercore.features.warp;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.ConfigUtil;
import org.Elias040.servercore.utils.SoundUtil;
import org.Elias040.servercore.utils.TextUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WarpCommand implements CommandExecutor, TabCompleter {

    private static final double MOVE_CANCEL_DISTANCE_SQUARED = 9.0;

    private final Main plugin;

    private final ConcurrentHashMap<UUID, Boolean> teleporting = new ConcurrentHashMap<>();

    public WarpCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(TextUtil.toComponent("&cUsage: /warp <n>"));
            SoundUtil.playError(plugin, p);
            return true;
        }

        String warpName = args[0];

        Optional<Location> targetOpt = plugin.warps().getWarp(warpName);
        if (targetOpt.isEmpty()) {
            p.sendMessage(plugin.messages().component("warp-not-found", Map.of("warp_name", warpName)));
            SoundUtil.playError(plugin, p);
            return true;
        }

        String displayName = plugin.warps().getDisplayName(warpName);

        if (teleporting.getOrDefault(p.getUniqueId(), false)) return true;

        int delaySeconds = ConfigUtil.getInt(plugin, "warp.teleport-delay-seconds", 5);
        Location target = targetOpt.get();

        teleporting.put(p.getUniqueId(), true);
        startCountdownTeleport(p, displayName, target, delaySeconds);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return plugin.warps().getDisplayNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    private void startCountdownTeleport(Player player, String warpName, Location target, int delaySeconds) {
        if (delaySeconds <= 0) {
            teleporting.remove(player.getUniqueId());
            doTeleport(player, warpName, target);
            return;
        }

        final Location start     = player.getLocation().clone();
        final int[]    remaining = {delaySeconds};

        player.getScheduler().runAtFixedRate(plugin, (task) -> {
            if (!player.isOnline()) {
                teleporting.remove(player.getUniqueId());
                task.cancel();
                return;
            }

            if (movedTooFarXZ(start, player.getLocation())) {
                teleporting.remove(player.getUniqueId());
                task.cancel();
                player.sendMessage(plugin.messages().component("warp-move", Map.of("warp_name", warpName)));
                SoundUtil.playError(plugin, player);
                return;
            }

            if (remaining[0] <= 0) {
                teleporting.remove(player.getUniqueId());
                task.cancel();
                doTeleport(player, warpName, target);
                return;
            }

            player.sendActionBar(plugin.messages().component("warp-teleport-actionbar", Map.of(
                    "warp_name", warpName,
                    "warp_teleport_time_remaining", String.valueOf(remaining[0])
            )));
            SoundUtil.playTeleporting(plugin, player, "warp.teleporting-sound");
            remaining[0]--;

        }, null, 1L, 20L);
    }

    private void doTeleport(Player player, String warpName, Location target) {
        player.getScheduler().run(plugin, (task) ->
                player.teleportAsync(target).thenRun(() ->
                        player.getScheduler().run(plugin, t -> {
                            player.sendMessage(plugin.messages().component("warp-teleport-success",
                                    Map.of("warp_name", warpName)));
                            SoundUtil.playTeleportSuccess(plugin, player);
                        }, null)
                ), null);
    }

    public void cleanup(UUID uuid) {
        teleporting.remove(uuid);
    }

    private boolean movedTooFarXZ(Location start, Location current) {
        if (start.getWorld() != current.getWorld()) return true;
        double dx = start.getX() - current.getX();
        double dz = start.getZ() - current.getZ();
        return (dx * dx + dz * dz) >= MOVE_CANCEL_DISTANCE_SQUARED;
    }
}