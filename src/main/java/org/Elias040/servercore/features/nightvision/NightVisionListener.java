package org.Elias040.servercore.features.nightvision;

import org.Elias040.servercore.Main;
import org.Elias040.servercore.utils.SchedulerCompat;
import org.Elias040.servercore.utils.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class NightVisionListener implements Listener {

    private final Main plugin;

    private final Set<UUID> respawnPollers = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, TaskHandle> totemTasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> totemDeadlineNanos = new ConcurrentHashMap<>();

    private static final long RESPAWN_PERIOD_TICKS = 40L;
    private static final long RESPAWN_TIMEOUT_TICKS = 20L * 30L;

    private static final long TOTEM_WINDOW_NANOS = 2_000_000_000L;
    private static final long TOTEM_PERIOD_TICKS = 5L;

    public NightVisionListener(Main plugin) {
        this.plugin = plugin;
    }

    private void ensureNv(Player p) {
        if (NightVisionManager.isEnabled(p) && !p.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            NightVisionManager.enable(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        SchedulerCompat.runForEntity(plugin, p, () -> ensureNv(p));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        respawnPollers.remove(id);
        totemDeadlineNanos.remove(id);
        TaskHandle task = totemTasks.remove(id);
        if (task != null) task.cancel();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p0 = e.getEntity();
        if (!NightVisionManager.isEnabled(p0)) return;

        UUID uuid = p0.getUniqueId();
        if (!respawnPollers.add(uuid)) return;

        AtomicLong elapsed = new AtomicLong(0L);

        SchedulerCompat.runGlobalAtFixedRate(plugin, task -> {
            long nowTicks = elapsed.getAndAdd(RESPAWN_PERIOD_TICKS) + 1L;

            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                respawnPollers.remove(uuid);
                task.cancel();
                return;
            }

            if (nowTicks > RESPAWN_TIMEOUT_TICKS) {
                respawnPollers.remove(uuid);
                task.cancel();
                return;
            }

            if (p.isDead()) return;

            SchedulerCompat.runForEntity(plugin, p, () -> ensureNv(p));

            respawnPollers.remove(uuid);
            task.cancel();
        }, 1L, RESPAWN_PERIOD_TICKS);
    }

    @EventHandler
    public void onResurrect(EntityResurrectEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!NightVisionManager.isEnabled(p)) return;

        UUID id = p.getUniqueId();
        long deadline = System.nanoTime() + TOTEM_WINDOW_NANOS;
        totemDeadlineNanos.put(id, deadline);

        totemTasks.compute(id, (k, existing) -> {
            if (existing != null) return existing;

            return SchedulerCompat.runForEntityAtFixedRate(plugin, p, task -> {
                Long dl = totemDeadlineNanos.get(id);
                if (dl == null || System.nanoTime() > dl || !p.isOnline()) {
                    TaskHandle cur = totemTasks.remove(id);
                    if (cur != null) cur.cancel();
                    totemDeadlineNanos.remove(id);
                    return;
                }
                ensureNv(p);
            }, 1L, TOTEM_PERIOD_TICKS);
        });
    }
}