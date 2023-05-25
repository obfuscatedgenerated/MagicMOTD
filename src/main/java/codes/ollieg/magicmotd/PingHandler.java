package codes.ollieg.magicmotd;

import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PingHandler implements Listener {
    private final ConfigLoader config_loader;

    private int force_motd_index = -1;

    public PingHandler(@NotNull MagicMOTD plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }

        this.config_loader = plugin.getConfigLoader();
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


    private String getMOTD(int index, ProxyPingEvent event) {
        String motd = this.config_loader.getParsedConfig().getMOTDs().get(index);

        // get the player's ip address
        String ip = event.getConnection().getSocketAddress().toString();

        // TODO: resolve player name
        String name = this.config_loader.getParsedConfig().getDefaultPlayerName();

        // get player count
        int player_count = event.getConnection().getListener().getTabListSize();
        int max_players = event.getConnection().getListener().getMaxPlayers();

        return this.config_loader.substituteTemplates(motd, name, player_count, max_players);
    }


    @EventHandler
    private void onPing(@NotNull ProxyPingEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null!");
        }


        if (!this.config_loader.isParsed()) {
            throw new IllegalStateException("Config has not been parsed yet!");
        }

        List<String> motds = this.config_loader.getParsedConfig().getMOTDs();

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

        // set the MOTD
        event.getResponse().setDescription(getMOTD(index, event));
    }
}
