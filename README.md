# MagicMOTD

> A BungeeCord proxy plugin that replaces the MOTD with dynamic messages!

This plugin was inspired by plugins like [SwiftMOTD](https://www.spigotmc.org/resources/swiftmotd.221/), that allow server operators to serve customised MOTDs to players.

As opposed to SwiftMOTD, the plugin stores the IP to player name map to the disk, so that it is recovered when the server restarts. Additionally, it uses up to date BungeeCord APIs, to avoid deprecation and eliminate the BungeeYAML dependency.

I don't know how performant this is in production. You may see some warnings that the plugin takes a while to respond to events, but these can usually be ignored.

## Features

- Send a random MOTD from a list of MOTDs
- Format the MOTD with colours and formatting codes
- Use templates to display dynamic information
- Force a specific MOTD with a command
- Access and edit the IP to player name database through the [plugin API](#api-usage)

## How does it work?

**Magic.** *(and H2)*

I opted to use H2 as it is lighter than SQLite and MapDB. I wanted a single-file DB (but not slow like JSON) to store KV. In the plugin JAR, H2 takes up a little over a megabyte (compared to upwards of 16MB for the other choices).

There are plans to allow for a choice of storage implementation, but for now you'll have to fork the repo if you wish to use a different DB.

## When player name detection might not work

- You forgot to enable `ip_forward` in BungeeCord's `config.yml`.
- Multiple players are using the same IP address (e.g. behind a NAT like a home, school or office network). Other players may see the wrong name in the MOTD.
- The player frequently changes their IP address (e.g. using a VPN or dynamic IP address)
- Your server is behind a reverse proxy (e.g. Cloudflare, ngrok, etc.)

These limitations cannot be overcome, as the plugin has no way of knowing which player is pinging the proxy. It collects their IP when they join, and uses that to determine their name next time they ping the proxy.

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

You can build the javadoc by running:

```shell
mvn javadoc:javadoc
```

The javadoc will be located in the `target/site/apidocs` directory.

### API Usage

The plugin exposes an API for other plugins to use. The API is exposed through the `MagicMOTD` class.

#### Getting the API

To use the API, you first need to specify MagicMOTD as a provided dependency in your build system.

Maven:

```xml
<!-- in your <repositories> section -->
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<!-- in your <dependencies> section -->
<dependency>
    <groupId>com.github.obfuscatedgenerated</groupId>
    <artifactId>MagicMOTD</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

Gradle:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.obfuscatedgenerated:MagicMOTD:VERSION'
}
```

#### Using the API

To get the API, you need to get the `MagicMOTD` instance from the plugin manager and cast it to the API interface. Something along the lines of:

```java
import codes.ollieg.magicmotd.MagicMOTD;

// ...

MagicMOTD api = (MagicMOTD) getProxy().getPluginManager().getPlugin("MagicMOTD");
```

Then, you can access the `PlayerDB` by calling `api.getPlayerDB()`. This returns an instance of `PlayerDB`:
    
```java
import codes.ollieg.magicmotd.PlayerDB;

// ...

PlayerDB playerDB = api.getPlayerDB();
```

**For documentation of the available methods, please [consult the javadoc](https://ollieg.codes/MagicMOTD/javadoc/).**
