package codes.ollieg.magicmotd;

import codes.ollieg.magicmotd.commands.CommandForceMOTD;
import codes.ollieg.magicmotd.commands.CommandReloadMOTD;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public final class MagicMOTD extends Plugin {
    private final ConfigLoader config_loader = new ConfigLoader(this);
    private final PingHandler ping_handler = new PingHandler(this);

    public ConfigLoader getConfigLoader() {
        return this.config_loader;
    }

    public PingHandler getPingHandler() {
        return this.ping_handler;
    }


    @Override
    public void onEnable() {
        this.config_loader.reloadConfig();

        PluginManager plugin_manager = getProxy().getPluginManager();

        plugin_manager.registerListener(this, this.ping_handler);

        plugin_manager.registerCommand(this, new CommandReloadMOTD(this.config_loader));
        plugin_manager.registerCommand(this, new CommandForceMOTD(this));

        getLogger().info("MagicMOTD has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MagicMOTD has been disabled!");
    }
}
