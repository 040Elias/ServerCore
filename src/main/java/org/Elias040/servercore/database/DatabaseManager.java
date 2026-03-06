package org.Elias040.servercore.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the single SQLite connection for the plugin.
 * Opens the connection, creates required tables, and exposes prepared statements.
 * All public methods are synchronized to be safe under Folia's multi-region threading.
 */
public final class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Opens the SQLite connection and creates tables if they do not exist. */
    public synchronized void open() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File dbFile = new File(dataFolder, "servercore.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            connection = DriverManager.getConnection(url);

            // Enable WAL mode for better concurrent read performance
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
            }

            createTables();
            plugin.getLogger().info("SQLite database opened: " + dbFile.getName());
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to open SQLite database: " + e.getMessage());
            throw new RuntimeException("DatabaseManager failed to initialise", e);
        }
    }

    /** Closes the SQLite connection. Call this in onDisable(). */
    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("SQLite database closed.");
            } catch (SQLException e) {
                plugin.getLogger().warning("Error closing SQLite database: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    /** Returns the live connection. Never null after {@link #open()} succeeds and before {@link #close()} is called.
     * @throws IllegalStateException if the connection is not open */
    public synchronized Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("DatabaseManager: connection is not open");
        }
        return connection;
    }

    // -------------------------------------------------------------------------
    // Prepared statement helpers (callers must close the statement themselves)
    // -------------------------------------------------------------------------

    public synchronized PreparedStatement prepare(String sql) throws SQLException {
        if (connection == null) {
            throw new IllegalStateException("DatabaseManager: cannot prepare statement — connection is not open");
        }
        return connection.prepareStatement(sql);
    }

    // -------------------------------------------------------------------------
    // Table creation
    // -------------------------------------------------------------------------

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS spawns (
                        name_key     TEXT PRIMARY KEY,
                        display_name TEXT NOT NULL,
                        world        TEXT NOT NULL,
                        x            REAL NOT NULL,
                        y            REAL NOT NULL,
                        z            REAL NOT NULL,
                        yaw          REAL NOT NULL,
                        pitch        REAL NOT NULL
                    )""");

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS warps (
                        name_key     TEXT PRIMARY KEY,
                        display_name TEXT NOT NULL,
                        world        TEXT NOT NULL,
                        x            REAL NOT NULL,
                        y            REAL NOT NULL,
                        z            REAL NOT NULL,
                        yaw          REAL NOT NULL,
                        pitch        REAL NOT NULL
                    )""");
        }
    }
}
