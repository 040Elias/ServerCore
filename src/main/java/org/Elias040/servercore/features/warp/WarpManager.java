package org.Elias040.servercore.features.warp;

import org.Elias040.servercore.database.AbstractLocationManager;
import org.Elias040.servercore.database.DatabaseManager;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;

public class WarpManager extends AbstractLocationManager {

    public WarpManager(JavaPlugin plugin, DatabaseManager db) {
        super(plugin, db, "warps");
    }

    public List<String> getWarpNames() {
        return getNames();
    }

    public void setWarp(String name, Location loc) {
        save(name, loc);
    }

    public boolean deleteWarp(String name) {
        return delete(name);
    }

    public Optional<Location> getWarp(String name) {
        return getLocation(name);
    }
}
