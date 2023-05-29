package codes.ollieg.magicmotd.handlers;

import codes.ollieg.magicmotd.ConfigLoader;
import codes.ollieg.magicmotd.FontLib;
import codes.ollieg.magicmotd.MagicMOTD;
import codes.ollieg.magicmotd.PlayerDB;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

/**
 * Listens for the {@link ProxyPingEvent} and handles it.<br>
 * Rewrites the MOTD.
 */
public class PingHandler implements Listener {
    private final ConfigLoader config_loader;
    private final PlayerDB player_db;
    private final Logger logger;

    private int force_motd_index = -1;

    /**
     * Constructs a new {@link PingHandler}.
     * @param plugin the {@link MagicMOTD} instance
     */
    public PingHandler(@NotNull MagicMOTD plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }

        this.config_loader = plugin.getConfigLoader();
        this.player_db = plugin.getPlayerDB();
        this.logger = plugin.getLogger();
    }

    /**
     * Get the index of the MOTD that is being forcibly used.
     *
     * @return the index of the MOTD
     */
    public int getForceMOTDIndex() {
        return this.force_motd_index;
    }

    /**
     * Forces the MOTD at the index to be used.
     *
     * @param index the index of the MOTD to use
     */
    public void setForceMOTDIndex(int index) {
        this.force_motd_index = index;
    }


    /**
     * Gets the MOTD to use for the ping event.
     *
     * @param index the index of the MOTD to use
     * @param event the {@link ProxyPingEvent} instance
     * @return the templated MOTD to use
     */
    private String getMOTD(int index, ProxyPingEvent event) {
        String motd = this.config_loader.getParsedConfig().getMOTDs().get(index);

        // get the player's ip address by removing the port and the first character (which is a '/')
        String address = event.getConnection().getSocketAddress().toString();
        try {
            address = address.substring(0, address.indexOf("/"));
        } catch (StringIndexOutOfBoundsException e) {
            this.logger.warning("Could not substring IP: " + address);
        }

        // resolve the player's name
        String name;
        try {
            name = this.player_db.getNameForIP(address);
        } catch (Exception e) {
            this.logger.warning("Could not resolve name for IP: " + address);
            this.logger.warning(e.getMessage());
            name = null;
        }

        // fall back to the default player name if not found
        if (name == null) {
            name = this.config_loader.getParsedConfig().getDefaultPlayerName();
        }

        // get player counts
        ServerPing.Players player_counts = event.getResponse().getPlayers();
        int player_count = player_counts.getOnline();
        int max_players = player_counts.getMax();

        String substituted = this.config_loader.substituteTemplates(motd, name, player_count, max_players);

        // for each line, if it starts with %C% ignoring case, center it
        String[] lines = substituted.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.toLowerCase().startsWith("%c%")) {
                line = line.substring(3);
                line = FontLib.centerText(line);
            }

            lines[i] = line;
        }

        return String.join("\n", lines);
    }


    /**
     * Called by BungeeCord when a {@link ProxyPingEvent} is fired.
     * @param event the {@link ProxyPingEvent}
     */
    @EventHandler
    public void onPing(@NotNull ProxyPingEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null!");
        }


        if (!this.config_loader.isParsed()) {
            throw new IllegalStateException("Config has not been parsed yet!");
        }

        List<String> motds = this.config_loader.getParsedConfig().getMOTDs();

        // if there are no motds, abort
        if (motds.size() == 0) {
            return;
        }


        // if forcing an MOTD, check if the index is valid
        // if invalid, fall back to random MOTD
        int index = this.force_motd_index;

        if (this.force_motd_index < 0 || this.force_motd_index >= motds.size()) {
            index = -1;
        }

        // if not forcing an MOTD, get a random MOTD
        if (index == -1) {
            index = (int) (Math.random() * motds.size());
        }

        // build the MOTD
        BaseComponent[] components = TextComponent.fromLegacyText(this.getMOTD(index, event));

        // join the components
        TextComponent final_component = new TextComponent(components);
        event.getResponse().setDescriptionComponent(final_component);
    }
}
