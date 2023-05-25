package codes.ollieg.magicmotd;

import codes.ollieg.magicmotd.commands.CommandForceMOTD;
import codes.ollieg.magicmotd.commands.CommandReloadMOTD;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.sql.SQLException;

public final class MagicMOTD extends Plugin {
    private final ConfigLoader config_loader = new ConfigLoader(this);
    private final PlayerDB player_db = new PlayerDB(this);
    private final PingHandler ping_handler = new PingHandler(this);

    public ConfigLoader getConfigLoader() {
        return this.config_loader;
    }

    public PingHandler getPingHandler() {
        return this.ping_handler;
    }

    public PlayerDB getPlayerDB() {
        return this.player_db;
    }


    @Override
    public void onEnable() {
        this.config_loader.reloadConfig();

        PluginManager plugin_manager = getProxy().getPluginManager();

        // register listeners and commands
        plugin_manager.registerListener(this, this.ping_handler);

        plugin_manager.registerCommand(this, new CommandReloadMOTD(this.config_loader));
        plugin_manager.registerCommand(this, new CommandForceMOTD(this));

        // create the database if it doesn't exist
        try {
            this.player_db.createIfNotExists();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        getLogger().info("MagicMOTD has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MagicMOTD has been disabled!");
    }
}
