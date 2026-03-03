package org.Elias040.servercore.features.warp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WarpManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration cfg;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ServerCore-WarpIO");
        t.setDaemon(true);
        return t;
    });

    public WarpManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data/warps.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create data/warps.yml: " + e.getMessage());
            }
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        String yaml = cfg.saveToString();
        ioExecutor.submit(() -> {
            try (java.io.FileWriter fw = new java.io.FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                fw.write(yaml);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save data/warps.yml: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        ioExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("WarpManager IO executor did not terminate in time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean exists(String name) {
        return cfg.contains("warps." + normalize(name) + ".world");
    }

    public List<String> getWarpNames() {
        var section = cfg.getConfigurationSection("warps");
        if (section == null) return Collections.emptyList();
        return List.copyOf(section.getKeys(false));
    }

    public List<String> getDisplayNames() {
        return getWarpNames().stream()
                .map(key -> cfg.getString("warps." + key + ".name", key))
                .toList();
    }

    public void setWarp(String name, Location loc) {
        String key = "warps." + normalize(name);
        cfg.set(key + ".name",  name);
        cfg.set(key + ".world", loc.getWorld().getName());
        cfg.set(key + ".x",     loc.getX());
        cfg.set(key + ".y",     loc.getY());
        cfg.set(key + ".z",     loc.getZ());
        cfg.set(key + ".yaw",   loc.getYaw());
        cfg.set(key + ".pitch", loc.getPitch());
        save();
    }

    public boolean deleteWarp(String name) {
        String key = "warps." + normalize(name);
        if (!cfg.contains(key)) return false;
        cfg.set(key, null);
        save();
        return true;
    }

    public Optional<Location> getWarp(String name) {
        String key = "warps." + normalize(name);
        String worldName = cfg.getString(key + ".world");
        if (worldName == null || worldName.isBlank()) return Optional.empty();

        World world = Bukkit.getWorld(worldName);
        if (world == null) return Optional.empty();

        double x     = cfg.getDouble(key + ".x");
        double y     = cfg.getDouble(key + ".y");
        double z     = cfg.getDouble(key + ".z");
        float  yaw   = (float) cfg.getDouble(key + ".yaw");
        float  pitch = (float) cfg.getDouble(key + ".pitch");

        return Optional.of(new Location(world, x, y, z, yaw, pitch));
    }

    public String getDisplayName(String name) {
        return cfg.getString("warps." + normalize(name) + ".name", name);
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}