package codes.ollieg.magicmotd;

import codes.ollieg.magicmotd.commands.CommandForceMOTD;
import codes.ollieg.magicmotd.commands.CommandReloadMOTD;
import codes.ollieg.magicmotd.handlers.PingHandler;
import codes.ollieg.magicmotd.handlers.PostLoginHandler;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.bstats.bungeecord.Metrics;

import java.sql.SQLException;

/**
 * The main class for the MagicMOTD plugin.
 */
public final class MagicMOTD extends Plugin {
    private final ConfigLoader config_loader = new ConfigLoader(this);
    private final PlayerDB player_db = new PlayerDB(this);
    private final PingHandler ping_handler = new PingHandler(this);
    private Metrics metrics;

    /**
     * Gets the {@link ConfigLoader} instance associated with this plugin.
     * @return The {@link ConfigLoader} instance associated with this plugin.
     */
    public ConfigLoader getConfigLoader() {
        return this.config_loader;
    }

    /**
     * Gets the {@link PingHandler} instance associated with this plugin.
     * @return The {@link PingHandler} instance associated with this plugin.
     */
    public PingHandler getPingHandler() {
        return this.ping_handler;
    }

    /**
     * Gets the {@link PlayerDB} instance associated with this plugin.
     * @return The {@link PlayerDB} instance associated with this plugin.
     */
    public PlayerDB getPlayerDB() {
        return this.player_db;
    }


    /**
     * Called by the BungeeCord plugin system when this plugin is enabled.
     */
    @Override
    public void onEnable() {
        this.config_loader.reloadConfig();

        PluginManager plugin_manager = getProxy().getPluginManager();

        // register listeners and commands
        plugin_manager.registerListener(this, this.ping_handler);
        plugin_manager.registerListener(this, new PostLoginHandler(this));

        plugin_manager.registerCommand(this, new CommandReloadMOTD(this.config_loader));
        plugin_manager.registerCommand(this, new CommandForceMOTD(this));

        // ready & create the database if it doesn't exist
        try {
            this.player_db.readyConnections();
            this.player_db.createIfNotExists();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // connect to bstats
        this.metrics = new Metrics(this, 18579);

        getLogger().info("MagicMOTD has been enabled!");
    }

    /**
     * Called by the BungeeCord plugin system when this plugin is disabled.
     */
    @Override
    public void onDisable() {
        this.player_db.destroyConnections();
        getLogger().info("MagicMOTD has been disabled!");
    }
}
