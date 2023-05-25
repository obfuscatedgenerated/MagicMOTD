package codes.ollieg.magicmotd.commands;

import codes.ollieg.magicmotd.ConfigLoader;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

/**
 * The /reloadmotd command.
 */
public class CommandReloadMOTD extends Command {
    private final ConfigLoader config_loader;

    public CommandReloadMOTD(@NotNull ConfigLoader config_loader) {
        super("reloadmotd", "magicmotd.reload", "rmotd");

        if (config_loader == null) {
            throw new IllegalArgumentException("Config loader cannot be null!");
        }

        this.config_loader = config_loader;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender cannot be null!");
        }

        if (args == null) {
            throw new IllegalArgumentException("Args cannot be null!");
        }


        if (!this.config_loader.isParsed()) {
            // we can't make this message customisable since the config hasn't been parsed yet
            sender.sendMessage(new ComponentBuilder("The config has not been parsed yet! Please contact the proxy administrator.").color(ChatColor.RED).create());
            return;
        }

        ConfigLoader.ParsedConfig config = this.config_loader.getParsedConfig();

        sender.sendMessage(new ComponentBuilder(config.getMessage("reload.pending")).color(ChatColor.GREEN).create());

        boolean success = this.config_loader.reloadConfig();

        if (!success) {
            sender.sendMessage(new ComponentBuilder(config.getMessage("reload.fail")).color(ChatColor.RED).create());
            return;
        }

        sender.sendMessage(new ComponentBuilder(config.getMessage("reload.success")).color(ChatColor.GREEN).create());
    }
}
