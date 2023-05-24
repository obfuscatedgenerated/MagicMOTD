# MagicMOTD

> A BungeeCord proxy plugin that replaces the MOTD with dynamic messages!

This plugin was inspired by plugins like [SwiftMOTD](https://www.spigotmc.org/resources/swiftmotd.221/), that allow
server operators to serve customised MOTDs to players.

## Features

- Send a random MOTD from a list of MOTDs
- Format the MOTD with colours and formatting codes
- Use templates to display dynamic information
- Force a specific MOTD with a command

## Commands

| Command                   | Alias    | Description                                                                                                       | Permission         |
|---------------------------|----------|-------------------------------------------------------------------------------------------------------------------|--------------------|
| `/reloadmotd`             | `/rmotd` | Reloads the plugin configuration.                                                                                 | `magicmotd.reload` |
| `/forcemotd [motd index]` | `/fmotd` | Forces the MOTD at the position in the config to be displayed, or stops forcing an MOTD if the argument is blank. | `magicmotd.force`  |

## Development

Notes:
- This plugin uses [Maven](https://maven.apache.org/) to manage dependencies and build the plugin.
- When accessing an attribute of the current class, always use `this.` to avoid confusion with local variables.

### Setup

Clone the repository:

```shell
git clone https://github.com/obfuscatedgenerated/MagicMOTD.git
```

Install the dependencies:

```shell
mvn install
```

### Building

To build the plugin, run the following command:

```shell
mvn package
```

The built plugin will be located in the `target` directory with the name `MagicMOTD v<version>.jar`.
