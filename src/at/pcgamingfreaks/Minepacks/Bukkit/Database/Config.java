/*
 *   Copyright (C) 2019 GeorgH93
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
import at.pcgamingfreaks.YamlFileManager;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class Config extends Configuration implements DatabaseConnectionConfiguration
{
	private static final int CONFIG_VERSION = 21, UPGRADE_THRESHOLD = 21;

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
		if(oldConfig.getVersion() < 20) // Pre V2.0 config file
		{
			OldFileUpdater.updateConfig(oldConfig.getYaml(), getConfig());
		}
		else
		{
			super.doUpgrade(oldConfig);
		}
	}

	//region getter
	//region Database getter
	public int getAutoCleanupMaxInactiveDays()
	{
		return getConfig().getInt("Database.AutoCleanup.MaxInactiveDays", -1);
	}

	public String getDatabaseType()
	{
		return getConfig().getString("Database.Type", "sqlite");
	}

	public void setDatabaseType(String type)
	{
		getConfig().set("Database.Type", type);
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
		return getConfig().getString("Database.Tables.User", "backpack_players");
	}

	public String getBackpackTable()
	{
		return getConfig().getString("Database.Tables.Backpack", "backpacks");
	}

	public String getCooldownTable()
	{
		return getConfig().getString("Database.Tables.Cooldown", "backpack_cooldowns");
	}

	public String getDBFields(String sub, String def)
	{
		return getConfig().getString("Database.Tables.Fields." + sub, def);
	}

	public boolean getUseUUIDs()
	{
		return getConfig().getBoolean("Database.UseUUIDs", true);
	}

	public boolean getUseUUIDSeparators()
	{
		return getConfig().getBoolean("Database.UseUUIDSeparators", false);
	}

	public String getUnCacheStrategie()
	{
		return getConfig().getString("Database.Cache.UnCache.Strategie", "interval").toLowerCase();
	}

	public long getUnCacheInterval()
	{
		return getConfig().getLong("Database.Cache.UnCache.Interval", 600) * 20L;
	}

	public long getUnCacheDelay()
	{
		return getConfig().getLong("Database.Cache.UnCache.Delay", 600) * 20L;
	}
	//endregion

	public String getBPTitleOther()
	{
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString("BackpackTitleOther", "{OwnerName} Backpack").replaceAll("%", "%%").replaceAll("\\{OwnerName}", "%s"));
	}

	public String getBPTitle()
	{
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString("BackpackTitle", "Backpack"));
	}

	public boolean getDropOnDeath()
	{
		return getConfig().getBoolean("DropOnDeath", true);
	}

	public int getBackpackMaxSize()
	{
		return getConfig().getInt("MaxSize", 6);
	}

	public boolean getAutoUpdate()
	{
		return getConfig().getBoolean("Misc.AutoUpdate", true);
	}

	public boolean isBungeeCordModeEnabled()
	{
		return getConfig().getBoolean("Misc.UseBungeeCord", false);
	}

	public long getCommandCooldown()
	{
		return getConfig().getInt("Cooldown.Command", -1) * 1000L;
	}

	public boolean isCommandCooldownSyncEnabled()
	{
		return getConfig().getBoolean("Cooldown.Sync", false);
	}

	public boolean isCommandCooldownClearOnLeaveEnabled()
	{
		return getConfig().getBoolean("Cooldown.ClearOnLeave", false);
	}

	public boolean isCommandCooldownAddOnJoinEnabled()
	{
		return getConfig().getBoolean("Cooldown.AddOnJoin", true);
	}

	public long getCommandCooldownCleanupInterval()
	{
		return getConfig().getInt("Cooldown.CleanupInterval", 600) * 20L;
	}

	public Collection<GameMode> getAllowedGameModes()
	{
		Collection<GameMode> gameModes = new HashSet<>();
		for(String string : getConfig().getStringList("AllowedGameModes", new LinkedList<>()))
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
					gm = GameMode.valueOf(string.toUpperCase());
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
		return getConfig().getBoolean("FullInventory.CollectItems", false);
	}

	public long getFullInvCheckInterval()
	{
		return getConfig().getInt("FullInventory.CheckInterval", 1) * 20L; // in seconds
	}

	public double getFullInvRadius()
	{
		return getConfig().getDouble("FullInventory.CollectRadius", 1.5); // in blocks
	}
	//endregion

	//region Shulkerboxes
	public boolean isShulkerboxesPreventInBackpackEnabled()
	{ // Shulkerboxes are only available in MC 1.11 and newer
		return MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) && getConfig().getBoolean("Shulkerboxes.PreventInBackpack", true);
	}

	public boolean isShulkerboxesDisable()
	{ // Shulkerboxes are only available in MC 1.11 and newer
		return MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) && getConfig().getBoolean("Shulkerboxes.DisableShulkerboxes", false);
	}

	public boolean isShulkerboxesExistingDropEnabled()
	{
		return getConfig().getString("Shulkerboxes.Existing", "Ignore").equalsIgnoreCase("Destroy");
	}

	public boolean isShulkerboxesExistingDestroyEnabled()
	{
		return getConfig().getString("Shulkerboxes.Existing", "Ignore").equalsIgnoreCase("Destroy") || getConfig().getString("Shulkerboxes.Existing", "Ignore").equalsIgnoreCase("Remove");
	}
	//endregion

	//region Item filter
	public boolean isItemFilterEnabled()
	{
		return getConfig().getBoolean("ItemFilter.Enable", false) || getConfig().getBoolean("Shulkerboxes.PreventInBackpack", true);
	}

	public Collection<MinecraftMaterial> getItemFilterBlacklist()
	{
		List<String> stringBlacklist = getConfig().getStringList("ItemFilter.Blacklist", new LinkedList<>());
		Collection<MinecraftMaterial> blacklist = new LinkedList<>();
		for(String item : stringBlacklist)
		{
			MinecraftMaterial mat = MinecraftMaterial.fromInput(item);
			if(mat != null) blacklist.add(mat);
		}
		return blacklist;
	}
	//endregion

	//region World settings
	public Collection<String> getWorldBlacklist()
	{
		HashSet<String> blacklist = new HashSet<>();
		for(String world : getConfig().getStringList("WorldSettings.Blacklist", new LinkedList<>()))
		{
			blacklist.add(world.toLowerCase());
		}
		return blacklist;
	}

	public WorldBlacklistMode getWorldBlacklistMode()
	{
		String mode = getConfig().getString("WorldSettings.BlacklistMode", "Message");
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
	//endregion
}