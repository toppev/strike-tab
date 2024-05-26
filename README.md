# strike-tab
Per player tablist addon for StrikePractice plugin. The plugin has separate tabs for lobby and fights.

![Screenshot](./screenshot.png) 

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

### Making a release

1. Search for the current version (e.g., "0.3.12") and replace it with the new version appropriately.
2. Write a very short release note in `version.json`.
3. Commit the changes and tag the commit with the new version:
```bash
git add .
git commit -m "bump version to 0.3.13"
git tag -a -m "some short message e.g., the one from version.json" v0.3.13
git push
git push --tags
```
4. Wait for the CI to build the release and upload the jar file to the release on GitHub.

## FAQ
**Q: How can I display players in the tab?**  
A: If the line is completely empty ('', no spaces) the line will be replaced with a player. See config for the rank priorities (players with higher rank are shown first).

**Q: Does this work on X version?**  
A: Tested on 1.8.8 (1.8.R3) server with 1.8 and 1.16 clients (ViaVersion) and on 1.16.4 server with 1.16 client. It should work in with 
all 1.8.8-1.16.4 versions. There is experimental support for legacy clients (1.7.10) .
