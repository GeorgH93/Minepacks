<!-- Variables (this block will not be visible in the readme -->
[banner]: https://pcgamingfreaks.at/images/minepacks.png
[spigot]: https://www.spigotmc.org/resources/minepacks.19286/
[license]: https://github.com/GeorgH93/Minepacks/blob/master/LICENSE
[licenseImg]: https://img.shields.io/github/license/GeorgH93/Minepacks.svg
[ci]: https://ci.pcgamingfreaks.at/job/Minepacks%20API/
[ciImg]: https://ci.pcgamingfreaks.at/job/Minepacks%20API/badge/icon
[apiVersionImg]: https://img.shields.io/badge/dynamic/xml.svg?label=api-version&query=%2F%2Frelease[1]&url=https%3A%2F%2Frepo.pcgamingfreaks.at%2Frepository%2Fmaven-releases%2Fat%2Fpcgamingfreaks%2FMinepacks-API%2Fmaven-metadata.xml
[apiJavaDoc]: https://ci.pcgamingfreaks.at/job/Minepacks%20API/javadoc/
[apiBuilds]: https://ci.pcgamingfreaks.at/job/Minepacks%20API/
<!-- End of variables block -->

[![Logo][banner]][spigot]

This branch holds the API for the Minepacks plugin.

[![ciImg]][ci] [![apiVersionImg]][apiJavaDoc] [![licenseImg]][license]

## Adding it to your plugin:
### Maven:
The API is available through maven.
#### Repository:
```
<repository>
	<id>pcgf-repo</id>
	<url>https://repo.pcgamingfreaks.at/repository/maven-everything</url>
</repository>
```
#### Dependency:
```
<!-- Minepacks API -->
<dependency>
    <groupId>at.pcgamingfreaks</groupId>
    <artifactId>Minepacks-API</artifactId>
    <version>2.2</version><!-- Check api-version shield for newest version -->
</dependency>
```

### Build from source:
```
git clone https://github.com/GeorgH93/Minepacks.git
cd Minepacks
mvn -pl Minepacks-API
```

## Usage:
### Get access to the API:
```java
public static MinepacksPlugin getMinepacks() {
    Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin("Minepacks");
    if(!(bukkitPlugin instanceof MinepacksPlugin)) {
    	// Do something if Minepacks is not available
        return null;
    }
    return (MinepacksPlugin) bukkitPlugin;
}
```
You can now use the returned `MinepacksPlugin` object to interact with the Minepacks plugin.

### Access a players backpack inventory:
```java
public static Inventory getPlayerBackpackInventory(Player player) {
    Backpack bp = getMinepacks().getBackpackCachedOnly(player);
    if(bp == null) return null; //Backpack not loaded (retry later)
    return bp.getInventory();
}
```
This will return null if the backpack is not loaded or the inventory of the backpack if the backpack is already loaded.

## Links:
* [JavaDoc][apiJavaDoc]
* [API Build Server][apiBuilds]
