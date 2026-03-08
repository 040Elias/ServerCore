package org.Elias040.servercore.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Scheduler compatibility layer that works on both Folia and Paper.
 * <p>
 * On Folia, delegates to {@code EntityScheduler} and {@code GlobalRegionScheduler}.
 * On Paper, falls back to the {@code BukkitScheduler} (main-thread).
 */
public final class SchedulerCompat {

    private static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }

    private SchedulerCompat() {}

    public static boolean isFolia() {
        return FOLIA;
    }

    /**
     * Runs a one-shot task on the entity's owning thread (Folia) or the main thread (Paper).
     */
    public static void runForEntity(Plugin plugin, Entity entity, Runnable task) {
        if (FOLIA) {
            entity.getScheduler().run(plugin, t -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Runs a repeating task tied to an entity.
     * The consumer receives a {@link TaskHandle} that can be used for self-cancellation.
     *
     * @return a {@link TaskHandle} for external cancellation
     */
    public static TaskHandle runForEntityAtFixedRate(Plugin plugin, Entity entity,
                                                     Consumer<TaskHandle> task,
                                                     long initialDelayTicks, long periodTicks) {
        if (FOLIA) {
            io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask =
                    entity.getScheduler().runAtFixedRate(plugin,
                            t -> task.accept(t::cancel), null, initialDelayTicks, periodTicks);
            return foliaTask::cancel;
        } else {
            org.bukkit.scheduler.BukkitTask[] ref = new org.bukkit.scheduler.BukkitTask[1];
            ref[0] = Bukkit.getScheduler().runTaskTimer(plugin,
                    () -> task.accept(() -> ref[0].cancel()), initialDelayTicks, periodTicks);
            return () -> ref[0].cancel();
        }
    }

    /**
     * Runs a one-shot task on the global region thread (Folia) or the main thread (Paper).
     */
    public static void runGlobal(Plugin plugin, Runnable task) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().execute(plugin, task);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Runs a repeating global task.
     * The consumer receives a {@link TaskHandle} for self-cancellation.
     *
     * @return a {@link TaskHandle} for external cancellation
     */
    public static TaskHandle runGlobalAtFixedRate(Plugin plugin, Consumer<TaskHandle> task,
                                                  long initialDelayTicks, long periodTicks) {
        if (FOLIA) {
            io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask =
                    Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
                            t -> task.accept(t::cancel), initialDelayTicks, periodTicks);
            return foliaTask::cancel;
        } else {
            org.bukkit.scheduler.BukkitTask[] ref = new org.bukkit.scheduler.BukkitTask[1];
            ref[0] = Bukkit.getScheduler().runTaskTimer(plugin,
                    () -> task.accept(() -> ref[0].cancel()), initialDelayTicks, periodTicks);
            return () -> ref[0].cancel();
        }
    }
}
