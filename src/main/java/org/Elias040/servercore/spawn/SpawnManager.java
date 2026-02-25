package org.Elias040.servercore.spawn;

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

public class SpawnManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration cfg;

    public SpawnManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data/spawns.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create data/spawns.yml: " + e.getMessage());
            }
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data/spawns.yml: " + e.getMessage());
        }
    }

    public boolean exists(String name) {
        return cfg.contains("spawns." + normalize(name) + ".world");
    }

    public List<String> getSpawnNames() {
        var section = cfg.getConfigurationSection("spawns");
        if (section == null) return Collections.emptyList();
        return List.copyOf(section.getKeys(false));
    }

    public void setSpawn(String name, Location loc) {
        String key = "spawns." + normalize(name);
        cfg.set(key + ".world", loc.getWorld().getName());
        cfg.set(key + ".x", loc.getX());
        cfg.set(key + ".y", loc.getY());
        cfg.set(key + ".z", loc.getZ());
        cfg.set(key + ".yaw", loc.getYaw());
        cfg.set(key + ".pitch", loc.getPitch());
        save();
    }

    public boolean deleteSpawn(String name) {
        String key = "spawns." + normalize(name);
        if (!cfg.contains(key)) return false;
        cfg.set(key, null);
        save();
        return true;
    }

    public Optional<Location> getSpawn(String name) {
        String key = "spawns." + normalize(name);
        String worldName = cfg.getString(key + ".world");
        if (worldName == null || worldName.isBlank()) return Optional.empty();

        World world = Bukkit.getWorld(worldName);
        if (world == null) return Optional.empty();

        double x = cfg.getDouble(key + ".x");
        double y = cfg.getDouble(key + ".y");
        double z = cfg.getDouble(key + ".z");
        float yaw   = (float) cfg.getDouble(key + ".yaw");
        float pitch = (float) cfg.getDouble(key + ".pitch");

        return Optional.of(new Location(world, x, y, z, yaw, pitch));
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}