package org.Elias040.servercore.features.spawn;

import org.Elias040.servercore.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SpawnManager {

    private final JavaPlugin plugin;
    private final DatabaseManager db;

    public SpawnManager(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void shutdown() {}

    public boolean exists(String name) {
        String sql = "SELECT 1 FROM spawns WHERE name_key = ? LIMIT 1";
        try (PreparedStatement ps = db.prepare(sql)) {
            ps.setString(1, normalize(name));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SpawnManager.exists() failed: " + e.getMessage());
            return false;
        }
    }

    public List<String> getSpawnNames() {
        String sql = "SELECT name_key FROM spawns ORDER BY name_key";
        try (PreparedStatement ps = db.prepare(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> names = new ArrayList<>();
            while (rs.next()) {
                names.add(rs.getString("name_key"));
            }
            return Collections.unmodifiableList(names);
        } catch (SQLException e) {
            plugin.getLogger().severe("SpawnManager.getSpawnNames() failed: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<String> getDisplayNames() {
        String sql = "SELECT display_name FROM spawns ORDER BY name_key";
        try (PreparedStatement ps = db.prepare(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> names = new ArrayList<>();
            while (rs.next()) {
                names.add(rs.getString("display_name"));
            }
            return Collections.unmodifiableList(names);
        } catch (SQLException e) {
            plugin.getLogger().severe("SpawnManager.getDisplayNames() failed: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public void setSpawn(String name, Location loc) {
        String sql = """
                INSERT INTO spawns (name_key, display_name, world, x, y, z, yaw, pitch)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(name_key) DO UPDATE SET
                    display_name = excluded.display_name,
                    world        = excluded.world,
                    x            = excluded.x,
                    y            = excluded.y,
                    z            = excluded.z,
                    yaw          = excluded.yaw,
                    pitch        = excluded.pitch""";
        try (PreparedStatement ps = db.prepare(sql)) {
            ps.setString(1, normalize(name));
            ps.setString(2, name);
            ps.setString(3, loc.getWorld().getName());
            ps.setDouble(4, loc.getX());
            ps.setDouble(5, loc.getY());
            ps.setDouble(6, loc.getZ());
            ps.setFloat (7, loc.getYaw());
            ps.setFloat (8, loc.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("SpawnManager.setSpawn() failed: " + e.getMessage());
        }
    }

    public boolean deleteSpawn(String name) {
        String sql = "DELETE FROM spawns WHERE name_key = ?";
        try (PreparedStatement ps = db.prepare(sql)) {
            ps.setString(1, normalize(name));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("SpawnManager.deleteSpawn() failed: " + e.getMessage());
            return false;
        }
    }

    public Optional<Location> getSpawn(String name) {
        String sql = "SELECT world, x, y, z, yaw, pitch FROM spawns WHERE name_key = ? LIMIT 1";
        try (PreparedStatement ps = db.prepare(sql)) {
            ps.setString(1, normalize(name));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                String worldName = rs.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) return Optional.empty();

                double x     = rs.getDouble("x");
                double y     = rs.getDouble("y");
                double z     = rs.getDouble("z");
                float  yaw   = rs.getFloat("yaw");
                float  pitch = rs.getFloat("pitch");

                return Optional.of(new Location(world, x, y, z, yaw, pitch));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SpawnManager.getSpawn() failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    public String getDisplayName(String name) {
        String sql = "SELECT display_name FROM spawns WHERE name_key = ? LIMIT 1";
        try (PreparedStatement ps = db.prepare(sql)) {
            ps.setString(1, normalize(name));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("display_name");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SpawnManager.getDisplayName() failed: " + e.getMessage());
        }
        return name;
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
