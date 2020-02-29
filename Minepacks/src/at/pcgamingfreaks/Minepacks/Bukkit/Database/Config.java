/*
 *   Copyright (C) 2020 GeorgH93
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
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.DatabaseConnectionConfiguration;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.OldFileUpdater;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.ShrinkApproach;
import at.pcgamingfreaks.YamlFileManager;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.*;

public class Config extends Configuration implements DatabaseConnectionConfiguration
{
	private static final int CONFIG_VERSION = 26, UPGRADE_THRESHOLD = CONFIG_VERSION, PRE_V2_VERSION = 20;

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
		if(oldConfig.getVersion() < PRE_V2_VERSION) // Pre V2.0 config file
		{
			OldFileUpdater.updateConfig(oldConfig.getYamlE(), getConfigE());
		}
		else
		{
			Map<String, String> remappedKeys = new HashMap<>();
			if(oldConfig.getVersion() <= 23) remappedKeys.put("ItemFilter.Materials", "ItemFilter.Blacklist");
			doUpgrade(oldConfig, remappedKeys);
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
		return getConfigE().getString("Database.Type", "sqlite");
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
			return plugin.getServer().getOnlineMode();
		}
		return type.equals("online");
	}

	public boolean getUseUUIDSeparators()
	{
		return getConfigE().getBoolean("Database.UseUUIDSeparators", false);
	}

	public String getUnCacheStrategie()
	{
		return getConfigE().getString("Database.Cache.UnCache.Strategie", "interval").toLowerCase(Locale.ENGLISH);
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

	public boolean getAutoUpdate()
	{
		return getConfigE().getBoolean("Misc.AutoUpdate", true);
	}

	public boolean isBungeeCordModeEnabled()
	{
		return getConfigE().getBoolean("Misc.UseBungeeCord", false);
	}

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
	public Collection<String> getWorldBlacklist()
	{
		HashSet<String> blacklist = new HashSet<>();
		for(String world : getConfigE().getStringList("WorldSettings.Blacklist", new LinkedList<>()))
		{
			blacklist.add(world.toLowerCase(Locale.ROOT));
		}
		return blacklist;
	}

	public WorldBlacklistMode getWorldBlacklistMode()
	{
		String mode = getConfigE().getString("WorldSettings.BlacklistMode", "Message");
		WorldBlacklistMode blacklistMode = WorldBlacklistMode.Message;
		try
		{
			blacklistMode = WorldBlacklistMode.valueOf(mode);
		}
		catch(IllegalArgumentException ignored)
		{
			logger.warning(ConsoleColor.YELLOW + "Unsupported mode \"" + mode + "\" for option \"WorldSettings.BlacklistMode\"" + ConsoleColor.RESET);
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
			return Sound.valueOf(soundName);
		}
		catch(Exception ignored)
		{
			logger.warning("Unknown sound: " + soundName);
		}
		return null;
	}

	public Sound getOpenSound()
	{
		return getSound("OpenSound", MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) ? "BLOCK_SHULKER_BOX_OPEN" : (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9_2) ? "BLOCK_CHEST_OPEN" : "CHEST_OPEN"));
	}

	public Sound getCloseSound()
	{
		return getSound("CloseSound", MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) ? "BLOCK_SHULKER_BOX_CLOSE" : (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9_2) ? "BLOCK_CHEST_CLOSE" : "CHEST_CLOSE"));
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