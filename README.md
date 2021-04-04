# strike-tab
Per player tablist addon for StrikePractice plugin. The plugin has separate tabs for lobby and fights.

See below for FAQ section.

## Installing the plugin
1. Download the latest version from [releases](https://github.com/toppev/strike-tab/releases) (`Assets -> StrikeTab-x.y.z-SNAPSHOT-all.jar`)
2. Click "Watch" to be notified of new updates (optional).
3. Drop the jar file in your `plugins/` directory.
4. Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) and [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) plugins (required).
5. Start the server and configure the `plugins/StrikeTab/config.yml`.
6. Restart the server or reload the configuration with `/striketab reload`.

## Development

Feel free to open pull requests.

### Getting started (developers)
Just clone this repository :)  
All dependencies are downloaded from the maven repositories (except StrikePracticeAPI is in libs/)

### Building
Run `./gradlew build` and see `build/libs/` directory.  
The file is almost 2 megabytes as the Kotlin Standard Library is included.

### Debugging
You can toggle debugging with `/striketab debug`. The plugin will log more information when the debugging mode is enabled.

## FAQ
**Q: How can I display players in the tab?**  
A: If the line is completely empty ('', no spaces) the line will be replaced with a player. See config for the rank priorities (players with higher rank are shown first).

**Q: Does this work on X version?**  
A: Tested on 1.8.8 (1.8.R3) server with 1.8 and 1.16 clients (ViaVersion) and on 1.16.4 server with 1.16 client. It should work in with 
all 1.8.8-1.16.4 versions. There is experimental support for legacy clients (1.7.10) .

## Rules/EULA
- You may **not sell or redistribute** this product or any fork of it.
- You may modify the source code.
 Message me if your developer wants access to the source code.
