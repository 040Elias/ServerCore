package org.Elias040.servercore.features.spawn;

import org.Elias040.servercore.database.AbstractLocationManager;
import org.Elias040.servercore.database.DatabaseManager;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;

public class SpawnManager extends AbstractLocationManager {

    public SpawnManager(JavaPlugin plugin, DatabaseManager db) {
        super(plugin, db, "spawns");
    }

    public List<String> getSpawnNames() {
        return getNames();
    }

    public void setSpawn(String name, Location loc) {
        save(name, loc);
    }

    public boolean deleteSpawn(String name) {
        return delete(name);
    }

    public Optional<Location> getSpawn(String name) {
        return getLocation(name);
    }
}
