package codes.ollieg.magicmotd;

import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

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


    @EventHandler
    private void onPing(ProxyPingEvent event) {
        // TODO: serve MOTD
    }
}
