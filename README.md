<!-- Variables (this block will not be visible in the readme -->
[banner]: https://pcgamingfreaks.at/images/minepacks.png
[spigot]: https://www.spigotmc.org/resources/minepacks.19286/
<!--[spigotRatingImg]: https://img.shields.io/spiget/stars/19286.svg
[spigotDownloadsImg]: https://img.shields.io/spiget/downloads/19286.svg?label=downloads%20%28spigotmc.org%29-->
[spigotRatingImg]: https://img.shields.io/badge/dynamic/json.svg?color=brightgreen&label=rating&query=%24.rating.average&suffix=%20%2F%205&url=https%3A%2F%2Fapi.spiget.org%2Fv2%2Fresources%2F19286
[spigotDownloadsImg]: https://img.shields.io/badge/dynamic/json.svg?color=brightgreen&label=downloads%20%28spigotmc.org%29&query=%24.downloads&url=https%3A%2F%2Fapi.spiget.org%2Fv2%2Fresources%2F19286
[bukkit]: http://dev.bukkit.org/bukkit-plugins/minepacks/
[issues]: https://github.com/GeorgH93/Minepacks/issues
[wiki]: https://github.com/GeorgH93/Minepacks/wiki
[wikiFAQ]: https://github.com/GeorgH93/Minepacks/wiki/faq
[wikiPermissions]: https://github.com/GeorgH93/Minepacks/wiki/permissions
[release]: https://github.com/GeorgH93/Minepacks/releases/latest
[releaseImg]: https://img.shields.io/github/release/GeorgH93/Minepacks.svg?label=github%20release
[license]: https://github.com/GeorgH93/Minepacks/blob/master/LICENSE
[licenseImg]: https://img.shields.io/github/license/GeorgH93/Minepacks.svg
[ci]: https://ci.pcgamingfreaks.at/job/Minepacks%20V2/
[ciImg]: https://ci.pcgamingfreaks.at/job/Minepacks%20V2/badge/icon
[apiVersionImg]: https://img.shields.io/badge/dynamic/xml.svg?label=api-version&query=%2F%2Frelease[1]&url=https%3A%2F%2Frepo.pcgamingfreaks.at%2Frepository%2Fmaven-releases%2Fat%2Fpcgamingfreaks%2FMinepacks-API%2Fmaven-metadata.xml
[api]: https://github.com/GeorgH93/Minepacks/tree/API
[apiJavaDoc]: https://ci.pcgamingfreaks.at/job/Minepacks%20API/javadoc/
[apiBuilds]: https://ci.pcgamingfreaks.at/job/Minepacks%20API/
[bugReports]: https://github.com/GeorgH93/Minepacks/issues?q=is%3Aissue+is%3Aopen+label%3Abug
[bugReportsImg]: https://img.shields.io/github/issues/GeorgH93/Minepacks/bug.svg?label=bug%20reports
[reportBug]: https://github.com/GeorgH93/Minepacks/issues/new?labels=bug&template=bug.md
[featureRequests]: https://github.com/GeorgH93/Minepacks/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement
[featureRequestsImg]: https://img.shields.io/github/issues/GeorgH93/Minepacks/enhancement.svg?label=feature%20requests&color=informational
[requestFeature]: https://github.com/GeorgH93/Minepacks/issues/new?labels=enhancement&template=feature.md
[config]: https://github.com/GeorgH93/Minepacks/blob/master/resources/config.yml
<!-- End of variables block -->

[![Logo][banner]][spigot]

Minepacks is a backpack plugin for minecraft server running bukkit or spigot.

[![ciImg]][ci] [![releaseImg]][release]
[![apiVersionImg]][api] [![licenseImg]][license]

[![featureRequestsImg]][featureRequests] [![bugReportsImg]][bugReports]
[![spigotRatingImg]][spigot] [![spigotDownloadsImg]][spigot]

## Features:
* [Configuration][config]
* Backpack size controlled by [permissions][wikiPermissions]
* Auto item-collect on full inventory (can be enabled in the config)
* SQLite/MySQL database
* Multi Language (English and German Language File included)
* Preserves the NBT data of items (everything that can be stored in a chest can be stored in the backpack)
* Support for name changing / UUIDs
* Auto-updater
* [API][api] for developers

## Build from source:
The plugin can be build in 3 different configurations.
### Normal version:
```
git clone https://github.com/GeorgH93/Minepacks.git
cd Minepacks
mvn package
```
The final file will be in the `target` folder, named `Minepacks-<CurrentVersion>.jar`.

### Standalone version:
This version works without the PCGF-PluginLib, however some API features are not available.
```
git clone https://github.com/GeorgH93/Minepacks.git
cd Minepacks
mvn package -P Standalone,ExcludeBadRabbit
```
The final file will be in the `target` folder, named `Minepacks-<CurrentVersion>-Standalone.jar`.

### Release version:
This is the version of the plugin published on dev.bukkit.org and spigotmc.org.
```
git clone https://github.com/GeorgH93/Minepacks.git
cd Minepacks
mvn clean install -P Standalone,ExcludeBadRabbit
mvn clean package -P Release
```
The final file will be in the `target` folder, named `Minepacks-<CurrentVersion>-Release.jar`.

## API:
Minepacks V2 comes with an API that allows you to interact with this plugin.
If you think there is something missing in the API feel free to open a [feature request][requestFeature].
Please do not access the data of the plugin over other ways than the provided API, the inner workings will change and I won't keep track of what you are using in your plugin.
For more details about the API please check the following links:

[Source Code & Details][api] ⚫ [JavaDoc][apiJavaDoc] ⚫ [Build Server][apiBuilds]

## Support
* [Wiki][wiki]
* [Issue tracker][issues]
  * [new feature request][requestFeature]
  * [new bug report][reportBug]
* [Faq][wikiFAQ]

## Links
* [Spigot][spigot]
* [Dev Bukkit][bukkit]
* [Build Server][ci]
