/*
 *   Copyright (C) 2021 GeorgH93
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

import at.pcgamingfreaks.Bukkit.Configuration;
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.MinecraftMaterial;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.Cache.IUnCacheStrategyConfig;
import at.pcgamingfreaks.Database.Cache.UnCacheStrategy;
import at.pcgamingfreaks.Database.DatabaseConnectionConfiguration;
import at.pcgamingfreaks.Minepacks.Bukkit.API.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Enums.DatabaseType;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Enums.ShrinkApproach;
import at.pcgamingfreaks.Version;
import at.pcgamingfreaks.YamlFileManager;

import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.*;

public class Config extends Configuration implements DatabaseConnectionConfiguration, IUnCacheStrategyConfig
{
	private static final int CONFIG_VERSION = 35, UPGRADE_THRESHOLD = CONFIG_VERSION, PRE_V2_VERSION = 20;

	public Config(JavaPlugin plugin)
	{
		super(plugin, CONFIG_VERSION, UPGRADE_THRESHOLD);
		languageKey = "Language.Language";
		languageUpdateKey = "Language.UpdateMode";
	}

	@Override
	protected void doUpdate()
	{
		// Nothing to update yet
	}

	@Override
	protected void doUpgrade(@NotNull YamlFileManager oldConfig)
	{
		if(oldConfig.version().olderThan(new Version(PRE_V2_VERSION))) // Pre V2.0 config file
		{
			throw new IllegalStateException("Upgrading from Minepacks v1.x is not supported!");
		}
		else
		{
			Map<String, String> remappedKeys = new HashMap<>();
			if(oldConfig.version().olderOrEqualThan(new Version(23))) remappedKeys.put("ItemFilter.Materials", "ItemFilter.Blacklist");
			if(oldConfig.version().olderOrEqualThan(new Version(28))) remappedKeys.put("Misc.AutoUpdate.Enabled", "Misc.AutoUpdate");
			if(oldConfig.version().olderOrEqualThan(new Version(30)))
			{
				remappedKeys.put("WorldSettings.FilteredWorlds", "WorldSettings.Blacklist");
				remappedKeys.put("WorldSettings.BockMode", "WorldSettings.BlacklistMode");
			}
			if(oldConfig.version().olderOrEqualThan(new Version(34))) remappedKeys.put("Database.Cache.UnCache.Strategy", "Database.Cache.UnCache.Strategie");
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

	public @NotNull String getDatabaseTypeName()
	{
		return getConfigE().getString("Database.Type", "sqlite").toLowerCase(Locale.ENGLISH);
	}

	public @NotNull DatabaseType getDatabaseType()
	{
		return DatabaseType.fromName(getDatabaseTypeName());
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
			e.printStackTrace();
		}
	}

	public @NotNull String getDBTable(final @NotNull String table, final @NotNull String defaultValue)
	{
		return getConfigE().getString("Database.Tables." + table, defaultValue);
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
				logger.warning("When using BungeeCord please make sure to set the UUID_Type config option explicitly!");
			}
			return plugin.getServer().getOnlineMode();
		}
		return type.equals("online");
	}

	public boolean getUseUUIDSeparators()
	{
		return getConfigE().getBoolean("Database.UseUUIDSeparators", false);
	}
	//endregion

	public String getBPTitleOther()
	{
		return ChatColor.translateAlternateColorCodes('&', getConfigE().getString("BackpackTitleOther", "{OwnerName} Backpack").replaceAll("%", "%%").replaceAll("\\{OwnerName}", "%s"));
	}

	public String getBPTitle()
	{
		return ChatColor.translateAlternateColorCodes('&', getConfigE().getString("BackpackTitle", "Backpack"));
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
			logger.warning("Unknown ShrinkApproach \"" + approach + "\"!");
			return ShrinkApproach.SORT;
		}
	}

	//region Misc getters
	public boolean useUpdater()
	{
		return getConfigE().getBoolean("Misc.AutoUpdate.Enabled", getConfigE().getBoolean("Misc.AutoUpdate", true));
	}

	public String getUpdateChannel()
	{
		String channel = getConfigE().getString("Misc.AutoUpdate.Channel", "Release");
		if("Release".equals(channel) || "Master".equals(channel) || "Dev".equals(channel))
		{
			return channel;
		}
		else logger.info("Unknown update Channel: " + channel);
		return null;
	}

	public boolean isBungeeCordModeEnabled()
	{
		boolean useBungee = getConfigE().getBoolean("Misc.UseBungeeCord", false);
		boolean spigotUsesBungee = Utils.detectBungeeCord();
		if(useBungee && !spigotUsesBungee)
		{
			logger.warning("You have BungeeCord enabled, but it looks like you have not enabled it in your spigot.yml! You probably should check your configuration.");
		}
		else if(!useBungee && spigotUsesBungee && getDatabaseType() == DatabaseType.MYSQL)
		{
			logger.warning("Your server is running behind a BungeeCord server. If you are using the plugin on more than one server please make sure to also enable the 'UseBungeeCord' config option.");
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

	public boolean isCommandCooldownAddOnJoinEnabled()
	{
		return getConfigE().getBoolean("Cooldown.AddOnJoin", true) && !isCommandCooldownSyncEnabled();
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
		if(gameModes.size() < 1)
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
	public boolean isItemShortcutPlayerChoiceEnabled()
	{
		return getConfigE().getBoolean("ItemShortcut.AllowPlayersToChoseItem", true);
	}

	public boolean isItemShortcutPlayerDisableItemEnabled()
	{
		return getConfigE().getBoolean("ItemShortcut.AllowPlayersToDisableItem", true);
	}
	//endregion

	//region Sound settings
	private @Nullable Sound getSound(final @NotNull String option, final @NotNull String autoValue)
	{
		if(!getConfigE().getBoolean("Sound.Enabled", true)) return null;
		String soundName = getConfigE().getString("Sound." + option, "auto").toUpperCase(Locale.ENGLISH);
		if(soundName.equals("AUTO")) soundName = autoValue;
		if(soundName.equals("DISABLED") || soundName.equals("FALSE")) return null;
		try
		{
			return Sound.valueOf(soundName);
		}
		catch(Exception ignored)
		{
			logger.warning("Unknown sound: " + soundName);
		}
		return null;
	}

	public @Nullable Sound getOpenSound()
	{
		return getSound("OpenSound", MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) ? "BLOCK_SHULKER_BOX_OPEN" : (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9_2) ? "BLOCK_CHEST_OPEN" : "CHEST_OPEN"));
	}

	public @Nullable Sound getCloseSound()
	{
		return getSound("CloseSound", MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) ? "BLOCK_SHULKER_BOX_CLOSE" : (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9_2) ? "BLOCK_CHEST_CLOSE" : "CHEST_CLOSE"));
	}

	public @Nullable Sound getAutoCollectSound()
	{
		return getSound("AutoCollectSound", MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9_2) ? "ENTITY_ITEM_PICKUP" : "ITEM_PICKUP");
	}

	public @Nullable Sound getDragAndDropSound()
	{
		return getSound("DragAndDropSound", MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9_2) ? "ENTITY_ITEM_PICKUP" : "ITEM_PICKUP");
	}
	//endregion


	@Override
	public @NotNull UnCacheStrategy getUnCacheStrategy()
	{
		if(isBungeeCordModeEnabled()) return UnCacheStrategy.ON_DISCONNECT;
		return IUnCacheStrategyConfig.super.getUnCacheStrategy();
	}

	//region InventoryManagement settings
	public boolean isInventoryManagementClearCommandEnabled()
	{
		return getConfigE().getBoolean("InventoryManagement.ClearCommand.Enabled", true);
	}
	//endregion
	//endregion
}