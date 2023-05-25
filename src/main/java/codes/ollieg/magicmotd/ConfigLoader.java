package codes.ollieg.magicmotd;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Responsible for loading and parsing the YAML config file.
 */
public class ConfigLoader {
    private final MagicMOTD plugin;
    private Configuration config;


    /**
     * Constructs a new {@link ConfigLoader}.
     *
     * @param plugin the plugin
     * @throws RuntimeException         if the config fails to load
     * @throws IllegalArgumentException if the plugin is null
     */
    public ConfigLoader(@NotNull MagicMOTD plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }

        this.plugin = plugin;


        boolean success = reloadConfig();

        if (!success) {
            throw new RuntimeException("Failed to load config!");
        }
    }


    /**
     * Saves the default config file from resources if it doesn't exist.
     *
     * @throws IOException
     */
    private void saveDefaultConfig() throws IOException {
        // create data folder if it doesn't exist
        if (!this.plugin.getDataFolder().exists()) {
            boolean success = this.plugin.getDataFolder().mkdir();

            if (!success) {
                throw new IOException("Failed to create data folder!");
            }

            this.plugin.getLogger().info("Created data folder!");
        }


        File file = new File(this.plugin.getDataFolder(), "config.yml");

        // copy default config from resources if it doesn't exist
        if (!file.exists()) {
            this.plugin.getLogger().info("Config file not found, creating default config file...");

            // create the file
            boolean success = file.createNewFile();

            if (!success) {
                throw new IOException("Failed to create config file!");
            }

            this.plugin.getLogger().info("Created config file!");

            // copy default config from resources
            ConfigurationProvider yaml = ConfigurationProvider.getProvider(YamlConfiguration.class);
            Configuration default_conf = yaml.load(this.plugin.getResourceAsStream("config.yml"));
            yaml.save(default_conf, file);

            this.plugin.getLogger().info("Copied default config!");
        }
    }


    /**
     * (Re)loads the config file into memory.
     *
     * @param parse whether to parse the config or not (uses {@link #parseConfig()})
     *
     * @return true if successful, false otherwise
     */
    public boolean reloadConfig(boolean parse) {
        try {
            saveDefaultConfig();
        } catch (IOException e) {
            this.plugin.getLogger().severe("Failed to save default config!");
            e.printStackTrace();
            return false;
        }

        // use the yaml config loader
        ConfigurationProvider yaml = ConfigurationProvider.getProvider(YamlConfiguration.class);

        // load the config
        File config_file = new File(this.plugin.getDataFolder(), "config.yml");
        try {
            this.config = yaml.load(config_file);
        } catch (IOException e) {
            this.plugin.getLogger().severe("Failed to load config!");
            e.printStackTrace();
            return false;
        }

        if (parse) {
            try {
                parseConfig();
            } catch (RuntimeException e) {
                this.plugin.getLogger().severe("Failed to parse config!");
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * (Re)loads and parses the config file into memory.<br>
     * (Overload, parse = true)
     *
     * @return true if successful, false otherwise
     */
    public boolean reloadConfig() {
        return reloadConfig(true);
    }


    /**
     * Gets the config as a {@link Configuration}.
     *
     * @return the config
     */
    @NotNull public Configuration getRawConfig() {
        return this.config;
    }


    /**
     * Represents a parsed configuration.
     */
    public static class ParsedConfig {
        private String default_player_name = "Player";
        private List<String> motds = new ArrayList<>();
        private Map<String, String> messages = new HashMap<>();


        /**
         * Gets the default player name.
         * @return the default player name
         */
        @NotNull public String getDefaultPlayerName() {
            return this.default_player_name;
        }

        /**
         * Sets the default player name.
         * @param default_player_name the default player name
         * @throws IllegalArgumentException if the default player name is null
         */
        public void setDefaultPlayerName(@NotNull String default_player_name) {
            if (default_player_name == null) {
                throw new IllegalArgumentException("Default player name cannot be null!");
            }

            this.default_player_name = default_player_name;
        }


        /**
         * Gets the MOTDs.
         * @return the MOTDs
         */
        @NotNull public List<String> getMOTDs() {
            return this.motds;
        }

        /**
         * Replaces the MOTDs with the given list.
         * @param motds the MOTDs
         * @throws IllegalArgumentException if the MOTDs are null
         */
        public void replaceMOTDs(@NotNull List<String> motds) {
            if (motds == null) {
                throw new IllegalArgumentException("MOTDs cannot be null!");
            }

            this.motds = motds;
        }


        /**
         * Gets the message with the given key, returning the key if it doesn't exist.
         *
         * @param key the key
         * @return the message
         * @throws IllegalArgumentException if the key is null
         */
        @NotNull public String getMessage(@NotNull String key) {
            if (key == null) {
                throw new IllegalArgumentException("Key cannot be null!");
            }

            if (this.messages.containsKey(key)) {
                return this.messages.get(key);
            } else {
                return key;
            }
        }

        /**
         * Inserts the message with the given key.
         *
         * @param key the key
         * @param message the message
         * @throws IllegalArgumentException if the key or message is null
         */
        public void insertMessage(@NotNull String key, @NotNull String message) {
            if (key == null) {
                throw new IllegalArgumentException("Key cannot be null!");
            }

            if (message == null) {
                throw new IllegalArgumentException("Message cannot be null!");
            }

            this.messages.put(key, message);
        }

        /**
         * Replaces the messages with the given map.
         * @param messages the messages
         * @throws IllegalArgumentException if the messages are null
         */
        public void replaceMessages(@NotNull Map<String, String> messages) {
            if (messages == null) {
                throw new IllegalArgumentException("Messages cannot be null!");
            }

            this.messages = messages;
        }
    }

    private boolean is_parsed = false;
    private final ParsedConfig parsed_config = new ParsedConfig();

    /**
     * Gets the {@link ParsedConfig parsed config}.
     *
     * @return the {@link ParsedConfig parsed config}
     * @throws IllegalStateException if the config has not been parsed yet
     */
    @NotNull public ParsedConfig getParsedConfig() {
        if (!this.is_parsed) {
            throw new IllegalStateException("Config has not been parsed yet!");
        }

        return this.parsed_config;
    }


    private final List<String> KNOWN_TEMPLATES = Arrays.asList("player", "online", "max", "ping");
    private final Pattern TEMPLATE_REGEX = Pattern.compile("(?<!\\\\)(?:\\\\{2})*%(?:(?<!\\\\)(?:\\\\{2})*\\\\%|[^%])+(?<!\\\\)(?:\\\\{2})*%");
    private final Pattern BETWEEN_PERCENT_REGEX = Pattern.compile("%(.*?)%");


    /**
     * Checks all templates in the message for validity.
     *
     * @param message the message to check
     * @return true if all templates are valid, false otherwise
     *
     * @throws IllegalArgumentException if the message is null
     */
    public boolean validateTemplates(@NotNull String message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null!");
        }

        // check each template found in the string for its existence
        Matcher template_matcher = TEMPLATE_REGEX.matcher(message);

        while (template_matcher.find()) {
            String match = template_matcher.group(1);

            // refine the match down to the content between the percent signs
            Matcher between_percent_matcher = BETWEEN_PERCENT_REGEX.matcher(match);

            if (between_percent_matcher.find()) {
                match = between_percent_matcher.group(1);
            } else {
                return false;
            }


            if (!KNOWN_TEMPLATES.contains(match)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Substitutes all templates in the message with the given values.
     *
     * @param message           The message to substitute
     * @param player_name       The player name
     * @param online_players    The number of online players
     * @param max_players       The maximum number of players
     *
     * @return The substituted message
     */
    public String substituteTemplates(String message, String player_name, int online_players, int max_players) {
        return message.replaceAll("%player%", player_name)
                .replaceAll("%online%", String.valueOf(online_players))
                .replaceAll("%max%", String.valueOf(max_players));
    }


    /**
     * Parses the config for use by the plugin.
     *
     * @throws RuntimeException if the config is invalid
     */
    public void parseConfig() {
        // get the default player name
        String default_player_name = this.config.getString("default_player_name");

        if (default_player_name == null) {
            this.plugin.getLogger().warning("Default player name not found in config, using \"Player\".");
            default_player_name = "Player";
        }

        this.parsed_config.setDefaultPlayerName(default_player_name);


        if (!this.config.contains("motds")) {
            throw new RuntimeException("motds not found in config!");
        }

        if (!this.config.contains("messages")) {
            throw new RuntimeException("messages not found in config!");
        }

        // load each motd and validate templates
        List<String> motds = this.config.getStringList("motds");

        for (String motd : motds) {
            // check if the motd is empty
            if (motd.isEmpty()) {
                throw new RuntimeException("Empty message found in config!");
            }

            if (!validateTemplates(motd)) {
                throw new RuntimeException("Invalid template \"" + motd + "\" found in config!\\nYou may need to escape percent signs with a backslash (\\\\). E.g: \\\\%");
            }

            // push the motd to the list of motds
            this.parsed_config.getMOTDs().add(motd);
        }

        // load each message, pushing nested messages with dots (e.g. reload.success)
        Configuration messages = this.config.getSection("messages");

        // get all keys, including nested keys
        Collection<String> top_level_keys = messages.getKeys();
        List<String> keys = new ArrayList<>();

        // check each key for nested keys
        for (String key: top_level_keys) {
            if (messages.getSection(key) != null) {
                keys.addAll(messages.getSection(key).getKeys());
            } else {
                keys.add(key);
            }
        }

        for (String key : keys) {
            String message = messages.getString(key);

            // check if the message is empty
            if (message.isEmpty()) {
                throw new RuntimeException("Empty message found in config!");
            }

            this.parsed_config.insertMessage(key, message);
        }

        this.is_parsed = true;
    }

    /**
     * Checks if the config has been parsed yet.
     *
     * @return true if the config has been parsed, false otherwise
     */
    public boolean isParsed() {
        return this.is_parsed;
    }
}
