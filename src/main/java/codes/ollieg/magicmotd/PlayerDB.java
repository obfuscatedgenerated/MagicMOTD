package codes.ollieg.magicmotd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A KV store of player IP addresses to names.
 */
public class PlayerDB {
    private final MagicMOTD plugin;
    private final String url;
    private boolean ready = false;

    /**
     * Constructs a new {@link PlayerDB}.
     * @param plugin the plugin
     * @throws IllegalArgumentException if the plugin is null
     * @throws RuntimeException if the H2 database driver cannot be found
     */
    public PlayerDB(@NotNull MagicMOTD plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }

        this.plugin = plugin;

        // use MySQL mode to allow for ON DUPLICATE KEY UPDATE
        File db_path = new File(this.plugin.getDataFolder(), "MagicMOTD");
        url = "jdbc:h2:" + db_path.getAbsolutePath() + ";mode=MySQL";

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns whether the database is ready to be used.
     * @return whether the database is ready
     */
    public boolean isReady() {
        return this.ready;
    }

    /**
     * Creates the database and its table if they don't exist.
     * @throws SQLException if an error occurs while creating the database or table
     * @throws RuntimeException if the connection fails
     */
    public void createIfNotExists() throws SQLException {
        // with h2, there's little benefit to pooling connections
        // using a try-with-resources block will automatically close the connection safely
        // TODO: this was true for sqlite, but is it true for h2?
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn == null) {
                throw new RuntimeException("Connection is null!");
            }

            DatabaseMetaData metadata = conn.getMetaData();
            this.plugin.getLogger().info("DB using driver: " + metadata.getDriverName());

            // create table if it doesn't exist
            // the table is a simple KV store of ip -> name
            Statement statement = conn.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS PLAYERS (ip TEXT PRIMARY KEY, name TEXT NOT NULL)");

            this.ready = true;
        }
    }

    /**
     * Updates the player name for the given IP address.
     *
     * @param ip    the player's IP address
     * @param name  the player name
     * @throws SQLException if an error occurs while updating the player name
     * @throws IllegalArgumentException if the IP or name is null
     * @throws IllegalStateException if the database is not ready
     * @throws RuntimeException if the connection fails
     */
    public void setNameForIP(@NotNull String ip, @NotNull String name) throws SQLException {
        if (ip == null) {
            throw new IllegalArgumentException("IP cannot be null!");
        }

        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        }

        if (!this.ready) {
            throw new IllegalStateException("Database is not ready!");
        }


        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn == null) {
                throw new RuntimeException("Connection is null!");
            }

            PreparedStatement statement = conn.prepareStatement("INSERT INTO PLAYERS (ip, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = ?");
            statement.setString(1, ip);
            statement.setString(2, name);
            statement.setString(3, name);
            statement.execute();
        }
    }

    /**
     * Returns the player name for the given IP address, or null if the IP address is not in the database.
     * @param ip the player's IP address
     * @return the player name, or null if the IP address is not in the database
     */
    public @Nullable String getNameForIP(@NotNull String ip) {
        if (ip == null) {
            throw new IllegalArgumentException("IP cannot be null!");
        }

        if (!this.ready) {
            throw new IllegalStateException("Database is not ready!");
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn == null) {
                throw new RuntimeException("Connection is null!");
            }

            PreparedStatement statement = conn.prepareStatement("SELECT name FROM PLAYERS WHERE ip = ?");
            statement.setString(1, ip);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Returns a list of IPs known for the given player name.
     * @param name the player name
     * @return a list of IPs for the given player name
     * @throws IllegalArgumentException if the name is null
     * @throws IllegalStateException if the database is not ready
     * @throws RuntimeException if the connection fails
     */
    public @NotNull List<String> getIPsForName(@NotNull String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        }

        if (!this.ready) {
            throw new IllegalStateException("Database is not ready!");
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn == null) {
                throw new RuntimeException("Connection is null!");
            }

            PreparedStatement statement = conn.prepareStatement("SELECT ip FROM PLAYERS WHERE name = ?");
            statement.setString(1, name);
            ResultSet result = statement.executeQuery();

            // convert result set to list
            List<String> ips = new ArrayList<>();
            while (result.next()) {
                ips.add(result.getString("ip"));
            }

            return ips;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Erases the given IP address from the database.
     * @param ip the IP address to erase
     * @throws IllegalArgumentException if the IP is null
     * @throws IllegalStateException if the database is not ready
     * @throws RuntimeException if the connection fails
     */
    public void eraseIP(@NotNull String ip) {
        if (ip == null) {
            throw new IllegalArgumentException("IP cannot be null!");
        }

        if (!this.ready) {
            throw new IllegalStateException("Database is not ready!");
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn == null) {
                throw new RuntimeException("Connection is null!");
            }

            PreparedStatement statement = conn.prepareStatement("DELETE FROM PLAYERS WHERE ip = ?");
            statement.setString(1, ip);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Erases all records (IP addresses) for the given player name.
     * @param name the player name
     * @throws IllegalArgumentException if the name is null
     * @throws IllegalStateException if the database is not ready
     * @throws RuntimeException if the connection fails
     */
    public void eraseName(@NotNull String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        }

        if (!this.ready) {
            throw new IllegalStateException("Database is not ready!");
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn == null) {
                throw new RuntimeException("Connection is null!");
            }

            PreparedStatement statement = conn.prepareStatement("DELETE FROM PLAYERS WHERE name = ?");
            statement.setString(1, name);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
