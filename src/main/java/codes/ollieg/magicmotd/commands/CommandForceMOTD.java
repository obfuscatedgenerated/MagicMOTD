package codes.ollieg.magicmotd.commands;

import codes.ollieg.magicmotd.ConfigLoader;
import codes.ollieg.magicmotd.MagicMOTD;
import codes.ollieg.magicmotd.PingHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandForceMOTD extends Command {
    private final PingHandler ping_handler;
    private final ConfigLoader config_loader;

    public CommandForceMOTD(@NotNull MagicMOTD plugin) {
        // set the name and permission
        super("forcemotd", "magicmotd.force", "fmotd");

        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }

        this.ping_handler = plugin.getPingHandler();
        this.config_loader = plugin.getConfigLoader();
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
            sender.sendMessage(new ComponentBuilder("The config has not been parsed yet! Please contact the proxy administrator.").color(ChatColor.RED).create());
            return;
        }

        if (args.length > 1) {
            sender.sendMessage(new ComponentBuilder("Too many arguments!").color(ChatColor.RED).create());
            sender.sendMessage(new ComponentBuilder("Usage: /forcemotd [index]").color(ChatColor.RED).create());
            return;
        }

        // if args are empty, reset the forced MOTD
        if (args.length == 0) {
            this.ping_handler.setForceMOTDIndex(-1);
            sender.sendMessage(new ComponentBuilder("Reset the forced MOTD.").color(ChatColor.RED).create());
            return;
        }

        // if args are not empty, try to parse the index
        int index;
        try {
            index = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new ComponentBuilder("Invalid index!").color(ChatColor.RED).create());
            return;
        }

        List<String> motds = this.config_loader.getParsedConfig().getMessages();

        if (index < 1 || index >= motds.size()) {
            sender.sendMessage(new ComponentBuilder("Index out of range (1 to number of MOTDs). Available MOTDs: " + motds.size()).color(ChatColor.RED).create());
            return;
        }

        // set the forced MOTD
        this.ping_handler.setForceMOTDIndex(index);

        sender.sendMessage(new ComponentBuilder("Set the forced MOTD to index " + index + ".").color(ChatColor.GREEN).create());
        sender.sendMessage(new ComponentBuilder("Content: " + motds.get(index)).color(ChatColor.GREEN).create());
        sender.sendMessage(new ComponentBuilder("To reset the forced MOTD, run /forcemotd without any arguments.").color(ChatColor.GREEN).create());
    }
}
