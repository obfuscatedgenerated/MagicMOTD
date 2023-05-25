package codes.ollieg.magicmotd.handlers;

import codes.ollieg.magicmotd.MagicMOTD;
import codes.ollieg.magicmotd.PlayerDB;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class PostLoginHandler implements Listener {
    private final PlayerDB player_db;
    private final Logger logger;

    public PostLoginHandler(@NotNull MagicMOTD plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }

        this.player_db = plugin.getPlayerDB();
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onPostLogin(@NotNull PostLoginEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null!");
        }

        ProxiedPlayer player = event.getPlayer();

        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null!");
        }

        // get the player's ip address by removing the port and the first character (which is a '/')
        String address = player.getSocketAddress().toString();
        try {
            address = address.substring(0, address.indexOf("/"));
        } catch (StringIndexOutOfBoundsException e) {
            this.logger.warning("Could not substring IP: " + address);
        }

        // insert the player's name into the database
        try {
            this.player_db.setNameForIP(address, player.getName());
        } catch (Exception e) {
            this.logger.warning("Could not put name for IP: " + address);
            this.logger.warning(e.getMessage());
        }
    }
}
