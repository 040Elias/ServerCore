package org.Elias040.servercore.commands;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.ConfigUtil;
import org.Elias040.servercore.utils.SoundUtil;
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

public class SpawnCommand implements CommandExecutor, TabCompleter {

    private static final double MOVE_CANCEL_DISTANCE_SQUARED = 9.0;

    private final Main plugin;

    private final ConcurrentHashMap<UUID, Long> lastUseMillis = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Boolean> teleporting = new ConcurrentHashMap<>();

    public SpawnCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.messages().raw("only-players"));
            return true;
        }

        String spawnName;
        if (args.length == 0) {
            spawnName = plugin.getConfig().getString("spawn.default-spawn", "1");
        } else if (args.length == 1) {
            spawnName = args[0];
        } else {
            p.sendMessage(plugin.messages().plainComponent("&cUsage: /spawn <name>"));
            SoundUtil.playError(plugin, p);
            return true;
        }

        Optional<Location> targetOpt = plugin.spawns().getSpawn(spawnName);
        if (targetOpt.isEmpty()) {
            p.sendMessage(plugin.messages().component("spawn-not-found", Map.of("spawn_name", spawnName)));
            SoundUtil.playError(plugin, p);
            return true;
        }

        if (teleporting.getOrDefault(p.getUniqueId(), false)) return true;

        int cooldownSeconds = ConfigUtil.getInt(plugin, "spawn.cooldown-seconds", 20);
        long now = System.currentTimeMillis();
        long last = lastUseMillis.getOrDefault(p.getUniqueId(), 0L);
        long elapsedSec = (now - last) / 1000L;

        if (elapsedSec < cooldownSeconds) {
            long remaining = cooldownSeconds - elapsedSec;
            p.sendMessage(plugin.messages().component("cooldown-active", Map.of("cooldown_remaining", String.valueOf(remaining))));
            SoundUtil.playError(plugin, p);
            return true;
        }

        int delaySeconds = ConfigUtil.getInt(plugin, "spawn.teleport-delay-seconds", 3);
        Location target = targetOpt.get();

        lastUseMillis.put(p.getUniqueId(), System.currentTimeMillis());
        teleporting.put(p.getUniqueId(), true);

        startCountdownTeleport(p, spawnName, target, delaySeconds);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return plugin.spawns().getSpawnNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    private void startCountdownTeleport(Player player, String spawnName, Location target, int delaySeconds) {
        if (delaySeconds <= 0) {
            teleporting.remove(player.getUniqueId());
            doTeleport(player, spawnName, target);
            return;
        }

        final Location start = player.getLocation().clone();
        final int[] remaining = {delaySeconds};

        player.getScheduler().runAtFixedRate(plugin, (task) -> {
            if (!player.isOnline()) {
                teleporting.remove(player.getUniqueId());
                task.cancel();
                return;
            }

            if (movedTooFarXZ(start, player.getLocation())) {
                teleporting.remove(player.getUniqueId());
                task.cancel();
                player.sendMessage(plugin.messages().component("spawn-move", Map.of("spawn_name", spawnName)));
                SoundUtil.playError(plugin, player);
                return;
            }

            if (remaining[0] <= 0) {
                teleporting.remove(player.getUniqueId());
                task.cancel();
                doTeleport(player, spawnName, target);
                return;
            }

            player.sendActionBar(plugin.messages().component("spawn-teleport-actionbar", Map.of(
                    "spawn_name", spawnName,
                    "spawn_teleport_time_remaining", String.valueOf(remaining[0])
            )));
            SoundUtil.playTeleporting(plugin, player);
            remaining[0]--;

        }, null, 1L, 20L);
    }

    private void doTeleport(Player player, String spawnName, Location target) {
        player.getScheduler().run(plugin, (task) -> {
            player.teleportAsync(target).thenRun(() -> {
                player.sendMessage(plugin.messages().component("spawn-teleport-success", Map.of("spawn_name", spawnName)));
            });
        }, null);
    }

    private boolean movedTooFarXZ(Location start, Location current) {
        if (start.getWorld() != current.getWorld()) return true;
        double dx = start.getX() - current.getX();
        double dz = start.getZ() - current.getZ();
        return (dx * dx + dz * dz) >= MOVE_CANCEL_DISTANCE_SQUARED;
    }
}