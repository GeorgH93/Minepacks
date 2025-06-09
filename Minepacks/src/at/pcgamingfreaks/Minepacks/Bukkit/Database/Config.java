/*
 *   Copyright (C) 2024 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.MinecraftMaterial;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Config.Configuration;
import at.pcgamingfreaks.Config.ILanguageConfiguration;
import at.pcgamingfreaks.Config.YamlFileManager;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.DatabaseConnectionConfiguration;
import at.pcgamingfreaks.Minepacks.Bukkit.API.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.OldFileUpdater;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.ShrinkApproach;
import at.pcgamingfreaks.Minepacks.MagicValues;
import at.pcgamingfreaks.Reflection;
import at.pcgamingfreaks.Version;

import org.bukkit.*;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

public class Config extends Configuration implements DatabaseConnectionConfiguration, ILanguageConfiguration
{
	private static final Version CONFIG_VERSION = new Version(MagicValues.CONFIG_VERSION), PRE_V2_VERSION = new Version(20);

	public Config(Minepacks plugin)
	{
		super(plugin, CONFIG_VERSION);
	}

	@Override
	protected void doUpdate()
	{
		// Nothing to update yet
	}

	@Override
	protected void doUpgrade(@NotNull YamlFileManager oldConfig)
	{
		if(oldConfig.getVersion().olderThan(PRE_V2_VERSION)) // Pre V2.0 config file
		{
			OldFileUpdater.updateConfig(oldConfig.getYamlE(), getConfigE());
		}
		else
		{
			Map<String, String> remappedKeys = new HashMap<>();
			if(oldConfig.getVersion().olderOrEqualThan(new Version(23))) remappedKeys.put("ItemFilter.Materials", "ItemFilter.Blacklist");
			if(oldConfig.getVersion().olderOrEqualThan(new Version(28))) remappedKeys.put("Misc.AutoUpdate.Enabled", "Misc.AutoUpdate");
			if(oldConfig.getVersion().olderOrEqualThan(new Version(30)))
			{
				remappedKeys.put("WorldSettings.FilteredWorlds", "WorldSettings.Blacklist");
				remappedKeys.put("WorldSettings.BockMode", "WorldSettings.BlacklistMode");
			}
			if(oldConfig.getVersion().olderOrEqualThan(new Version(33))) remappedKeys.put("Database.Cache.UnCache.Strategy", "Database.Cache.UnCache.Strategie");
			Collection<String> keysToKeep = oldConfig.getYamlE().getKeysFiltered("Database\\.SQL\\.(MaxLifetime|IdleTimeout)");
			keysToKeep.addAll(oldConfig.getYamlE().getKeysFiltered("Database\\.Tables\\.Fields\\..+"));
			doUpgrade(oldConfig, remappedKeys, keysToKeep);
		}
	}

	//region getter
	//region Database getter
	public int getAutoCleanupMaxInactiveDays()
	{
		return getConfigE().getInt("Database.AutoCleanup.MaxInactiveDays", -1);
	}

	public String getDatabaseType()
	{
		return getConfigE().getString("Database.Type", "sqlite").toLowerCase(Locale.ENGLISH);
	}

	public void setDatabaseType(String type)
	{
		getConfigE().set("Database.Type", type);
		try
		{
			save();
		}
		catch(FileNotFoundException e)
		{
			logger.log(Level.SEVERE, "Failed to set database type", e);
		}
	}

	public String getUserTable()
	{
		return getConfigE().getString("Database.Tables.User", "backpack_players");
	}

	public String getBackpackTable()
	{
		return getConfigE().getString("Database.Tables.Backpack", "backpacks");
	}

	public String getCooldownTable()
	{
		return getConfigE().getString("Database.Tables.Cooldown", "backpack_cooldowns");
	}

	public String getDBFields(String sub, String def)
	{
		return getConfigE().getString("Database.Tables.Fields." + sub, def);
	}

	public boolean useOnlineUUIDs()
	{
		String type = getConfigE().getString("Database.UUID_Type", "auto").toLowerCase(Locale.ENGLISH);
		if(type.equals("auto"))
		{
			if(isBungeeCordModeEnabled())
			{
				Boolean detectedOnlineMode = Utils.getBungeeOrVelocityOnlineMode();
				if (detectedOnlineMode != null)
				{
					logger.log(Level.INFO, "Detected online mode in paper config: {0}", detectedOnlineMode);
					return detectedOnlineMode;
				}
				logger.warning("When using BungeeCord please make sure to set the UUID_Type config option explicitly!");
			}
			return Bukkit.getServer().getOnlineMode();
		}
		return type.equals("online");
	}

	public boolean getUseUUIDSeparators()
	{
		return getConfigE().getBoolean("Database.UseUUIDSeparators", false);
	}

	public boolean isForceSaveOnUnloadEnabled()
	{
		return getConfigE().getBoolean("Database.ForceSaveOnUnload", false);
	}

	public String getUnCacheStrategy()
	{
		return getConfigE().getString("Database.Cache.UnCache.Strategy", "interval").toLowerCase(Locale.ENGLISH);
	}

	public long getUnCacheInterval()
	{
		return getConfigE().getLong("Database.Cache.UnCache.Interval", 600) * 20L;
	}

	public long getUnCacheDelay()
	{
		return getConfigE().getLong("Database.Cache.UnCache.Delay", 600) * 20L;
	}
	//endregion

	public String getBPTitleOther()
	{
		return ChatColor.translateAlternateColorCodes('&', getConfigE().getString("BackpackTitleOther", "{OwnerName} Backpack").replace("%", "%%").replace("{OwnerName}", "%s"));
	}

	public String getBPTitle()
	{
		return ChatColor.translateAlternateColorCodes('&', getConfigE().getString("BackpackTitle", "Backpack").replace("%", "%%").replace("{OwnerName}", "%s"));
	}

	public boolean useDynamicBPTitle()
	{
		return getConfigE().getBoolean("Database.UseDynamicTitle", true);
	}

	public boolean getDropOnDeath()
	{
		return getConfigE().getBoolean("DropOnDeath", true);
	}

	public boolean getHonorKeepInventoryOnDeath()
	{
		return getConfigE().getBoolean("HonorKeepInventoryOnDeath", false);
	}

	public int getBackpackMaxSize()
	{
		int size = getConfigE().getInt("MaxSize", 6);
		if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_14)) size = Math.min(6, size);
		if(size > 6)
		{
			logger.info("Starting with MC 1.14 backpacks with more than 6 rows will no longer be possible. A feature to allow bigger backpacks through multiple pages is currently in development.");
		}
		return Math.max(1, size);
	}

	public ShrinkApproach getShrinkApproach()
	{
		if(MCVersion.isOlderThan(MCVersion.MC_1_8)) return ShrinkApproach.FAST;
		String approach = getConfigE().getString("ShrinkApproach", "SORT");
		try
		{
			return ShrinkApproach.valueOf(approach.toUpperCase(Locale.ENGLISH));
		}
		catch(IllegalArgumentException ignored)
		{
			logger.log(Level.WARNING, "Unknown ShrinkApproach \"{0}\"!", approach);
			return ShrinkApproach.SORT;
		}
	}

	//region Misc getters
	public boolean useUpdater()
	{
		return (MCVersion.isNewerOrEqualThan(MCVersion.getFromVersionName(MagicValues.MIN_MC_VERSION_FOR_UPDATES))) && getConfigE().getBoolean("Misc.AutoUpdate.Enabled", getConfigE().getBoolean("Misc.AutoUpdate", true));
	}

	public String getUpdateChannel()
	{
		String channel = getConfigE().getString("Misc.AutoUpdate.Channel", "Release");
		if("Release".equals(channel) || "Master".equals(channel) || "Dev".equals(channel))
		{
			return channel;
		}
		else logger.log(Level.INFO, "Unknown update Channel: {0}", channel);
		return null;
	}

	public boolean isBungeeCordModeEnabled()
	{
		boolean useBungee = getConfigE().getBoolean("Misc.UseBungeeCord", false);
		boolean runsProxy = Utils.detectBungeeCord() || Utils.detectVelocity();
		boolean shareableDB = getDatabaseType().equals("mysql") || getDatabaseType().equals("global");
		if(useBungee && !runsProxy)
		{
			logger.warning("You have BungeeCord enabled for the plugin, but it looks like you have not enabled it in your spigot.yml! You probably should check your configuration.");
		}
		else if(!useBungee && runsProxy && shareableDB)
		{
			logger.warning("Your server is running behind a BungeeCord server. If you are using the plugin on more than one server with a shared database, please make sure to also enable the 'UseBungeeCord' config option.");
		}
		else if(useBungee && !shareableDB)
		{
			logger.info("You have enabled BungeeCord mode for the plugin, but are not using a shared MySQL database.");
			return false; // No need to enable BungeeCord mode if the database does not support it
		}
		return useBungee;
	}
	//endregion

	public long getCommandCooldown()
	{
		return getConfigE().getInt("Cooldown.Command", -1) * 1000L;
	}

	public boolean isCommandCooldownSyncEnabled()
	{
		return getConfigE().getBoolean("Cooldown.Sync", false);
	}

	public boolean isCommandCooldownClearOnLeaveEnabled()
	{
		return getConfigE().getBoolean("Cooldown.ClearOnLeave", false);
	}

	public boolean isCommandCooldownAddOnJoinEnabled()
	{
		return getConfigE().getBoolean("Cooldown.AddOnJoin", true);
	}

	public long getCommandCooldownCleanupInterval()
	{
		return getConfigE().getInt("Cooldown.CleanupInterval", 600) * 20L;
	}

	public Collection<GameMode> getAllowedGameModes()
	{
		Collection<GameMode> gameModes = new HashSet<>();
		for(String string : getConfigE().getStringList("AllowedGameModes", new LinkedList<>()))
		{
			GameMode gm = null;
			try
			{
				//noinspection deprecation
				gm = GameMode.getByValue(Integer.parseInt(string));
			}
			catch(NumberFormatException ignored) {}
			if(gm == null)
			{
				try
				{
					gm = GameMode.valueOf(string.toUpperCase(Locale.ROOT));
				}
				catch(IllegalArgumentException ignored)
				{
					logger.warning("Unknown game-mode '" + string + "'");
				}
			}
			if(gm != null)
			{
				gameModes.add(gm);
			}
		}
		if(gameModes.isEmpty())
		{
			logger.info("No game-mode's allowed, allowing: " + GameMode.SURVIVAL.name());
			gameModes.add(GameMode.SURVIVAL);
		}
		return gameModes;
	}

	//region Full inventory handling
	public boolean getFullInvCollect()
	{
		return getConfigE().getBoolean("FullInventory.CollectItems", false);
	}

	public long getFullInvCheckInterval()
	{
		return getConfigE().getInt("FullInventory.CheckInterval", 1) * 20L; // in seconds
	}

	public double getFullInvRadius()
	{
		return getConfigE().getDouble("FullInventory.CollectRadius", 1.5); // in blocks
	}

	public boolean isFullInvToggleAllowed()
	{
		return getConfigE().getBoolean("FullInventory.IsToggleAllowed", false);
	}

	public boolean isFullInvEnabledOnJoin()
	{
		return getFullInvCollect();
	}
	//endregion

	//region Shulkerboxes
	public boolean isShulkerboxesPreventInBackpackEnabled()
	{ // Shulkerboxes are only available in MC 1.11 and newer
		return MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) && getConfigE().getBoolean("Shulkerboxes.PreventInBackpack", true);
	}

	public boolean isShulkerboxesDisable()
	{ // Shulkerboxes are only available in MC 1.11 and newer
		return MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) && getConfigE().getBoolean("Shulkerboxes.DisableShulkerboxes", false);
	}

	public boolean isShulkerboxesExistingDropEnabled()
	{
		return getConfigE().getString("Shulkerboxes.Existing", "Ignore").equalsIgnoreCase("Destroy");
	}

	public boolean isShulkerboxesExistingDestroyEnabled()
	{
		return getConfigE().getString("Shulkerboxes.Existing", "Ignore").equalsIgnoreCase("Destroy") || getConfigE().getString("Shulkerboxes.Existing", "Ignore").equalsIgnoreCase("Remove");
	}
	//endregion

	//region Item filter
	public boolean isItemFilterEnabledNoShulker()
	{
		return getConfigE().getBoolean("ItemFilter.Enabled", false);
	}

	public boolean isItemFilterEnabled()
	{
		return isItemFilterEnabledNoShulker() || getConfigE().getBoolean("Shulkerboxes.PreventInBackpack", true);
	}

	public Collection<MinecraftMaterial> getItemFilterMaterials()
	{
		if(!isItemFilterEnabledNoShulker()) return new LinkedList<>();
		List<String> stringMaterialList = getConfigE().getStringList("ItemFilter.Materials", new LinkedList<>());
		if(isItemFilterModeWhitelist()) stringMaterialList.add("air");
		Collection<MinecraftMaterial> blacklist = new LinkedList<>();
		for(String item : stringMaterialList)
		{
			MinecraftMaterial mat = MinecraftMaterial.fromInput(item);
			if(mat != null) blacklist.add(mat);
		}
		return blacklist;
	}

	public Set<String> getItemFilterNames()
	{
		if(!isItemFilterEnabledNoShulker()) return new HashSet<>();
		Set<String> names = new HashSet<>();
		getConfigE().getStringList("ItemFilter.Names", new LinkedList<>()).forEach(name -> names.add(ChatColor.translateAlternateColorCodes('&', name)));
		return names;
	}

	public Set<String> getItemFilterLore()
	{
		if(!isItemFilterEnabledNoShulker()) return new HashSet<>();
		Set<String> loreSet = new HashSet<>();
		getConfigE().getStringList("ItemFilter.Lore", new LinkedList<>()).forEach(lore -> loreSet.add(ChatColor.translateAlternateColorCodes('&', lore)));
		return loreSet;
	}

	public boolean isItemFilterModeWhitelist()
	{
		return getConfigE().getString("ItemFilter.Mode", "blacklist").toLowerCase(Locale.ENGLISH).equals("whitelist") && isItemFilterEnabledNoShulker();
	}
	//endregion

	//region World settings
	public boolean isWorldWhitelistMode()
	{
		return getConfigE().getString("WorldSettings.FilterType", "blacklist").equalsIgnoreCase("whitelist");
	}

	public Set<String> getWorldFilteredList()
	{
		Set<String> worldList = new HashSet<>();
		for(String world : getConfigE().getStringList("WorldSettings.FilteredWorlds", new ArrayList<>(0)))
		{
			worldList.add(world.toLowerCase(Locale.ROOT));
		}
		return worldList;
	}

	public Set<String> getWorldBlacklist()
	{
		if(isWorldWhitelistMode())
		{
			Set<String> whitelist = getWorldFilteredList(), blacklist = new HashSet<>();
			for(World world : Bukkit.getServer().getWorlds())
			{
				String worldName = world.getName().toLowerCase(Locale.ROOT);
				if(!whitelist.contains(worldName)) blacklist.add(worldName);
			}
			return blacklist;
		}
		else return getWorldFilteredList();
	}

	public WorldBlacklistMode getWorldBlockMode()
	{
		String mode = getConfigE().getString("WorldSettings.BlockMode", "Message");
		WorldBlacklistMode blacklistMode = WorldBlacklistMode.Message;
		try
		{
			blacklistMode = WorldBlacklistMode.valueOf(mode);
		}
		catch(IllegalArgumentException ignored)
		{
			logger.warning(ConsoleColor.YELLOW + "Unsupported mode \"" + mode + "\" for option \"WorldSettings.BlockMode\"" + ConsoleColor.RESET);
		}
		return blacklistMode;
	}
	//endregion

	//region ItemShortcut settings
	public boolean isItemShortcutEnabled()
	{
		return MCVersion.isNewerOrEqualThan(MCVersion.MC_1_8) && getConfigE().getBoolean("ItemShortcut.Enabled", true);
	}

	public String getItemShortcutItemName()
	{
		return getConfigE().getString("ItemShortcut.ItemName", "&eBackpack");
	}

	public String getItemShortcutHeadValue()
	{
		return getConfigE().getString("ItemShortcut.HeadTextureValue", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGRjYzZlYjQwZjNiYWRhNDFlNDMzOTg4OGQ2ZDIwNzQzNzU5OGJkYmQxNzVjMmU3MzExOTFkNWE5YTQyZDNjOCJ9fX0=");
	}

	public boolean isItemShortcutImproveDeathChestCompatibilityEnabled()
	{
		return getConfigE().getBoolean("ItemShortcut.ImproveDeathChestCompatibility", false);
	}

	public boolean isItemShortcutBlockAsHatEnabled()
	{
		return getConfigE().getBoolean("ItemShortcut.BlockAsHat", false);
	}

	public boolean isItemShortcutRightClickOnContainerAllowed()
	{
		return getConfigE().getBoolean("ItemShortcut.OpenContainerOnRightClick", false) && MCVersion.isNewerOrEqualThan(MCVersion.MC_1_13);
	}

	public int getItemShortcutPreferredSlotId()
	{
		return getConfigE().getInt("ItemShortcut.PreferredSlotId", -1);
	}

	public boolean getItemShortcutBlockItemFromMoving()
	{
		return getConfigE().getBoolean("ItemShortcut.BlockItemFromMoving", false);
	}
	//endregion

	//region Sound settings
	private Sound getSound(String option, String autoValue)
	{
		if(!getConfigE().getBoolean("Sound.Enabled", true)) return null;
		String soundName = getConfigE().getString("Sound." + option, "auto").toUpperCase(Locale.ENGLISH);
		if(soundName.equals("AUTO")) soundName = autoValue;
		if(soundName.equals("DISABLED") || soundName.equals("FALSE")) return null;
		try
		{
			if (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_21))
			{
				Field f = Reflection.getField(Sound.class, soundName);
				if (f != null) return (Sound) f.get(null);
			}
			else
			{
				return Sound.valueOf(soundName);
			}
		}
		catch(Exception ignored)
		{
			logger.warning("Unknown sound: " + soundName);
		}
		return null;
	}

	private static final @NotNull String DEFAULT_SOUND_OPEN = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) ? "BLOCK_SHULKER_BOX_OPEN" : (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9_2) ? "BLOCK_CHEST_OPEN" : "CHEST_OPEN");
	private static final @NotNull String DEFAULT_SOUND_CLOSE = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) ? "BLOCK_SHULKER_BOX_CLOSE" : (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9_2) ? "BLOCK_CHEST_CLOSE" : "CHEST_CLOSE");

	public Sound getOpenSound()
	{
		return getSound("OpenSound", DEFAULT_SOUND_OPEN);
	}

	public Sound getCloseSound()
	{
		return getSound("CloseSound", DEFAULT_SOUND_CLOSE);
	}
	//endregion

	//region InventoryManagement settings
	public boolean isInventoryManagementClearCommandEnabled()
	{
		return getConfigE().getBoolean("InventoryManagement.ClearCommand.Enabled", true);
	}
	//endregion
	//endregion
}