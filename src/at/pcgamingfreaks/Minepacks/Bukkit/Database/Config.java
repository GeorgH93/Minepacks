/*
 *   Copyright (C) 2016 GeorgH93
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

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Config extends Configuration
{
	private static final int CONFIG_VERSION = 14;

	public Config(JavaPlugin plugin)
	{
		super(plugin, CONFIG_VERSION, CONFIG_VERSION);
	}

	@Override
	protected void doUpdate()
	{
		// Nothing to update yet
	}

	@Override
	protected void doUpgrade(at.pcgamingfreaks.Configuration oldConfig)
	{
		Set<String> keys = oldConfig.getConfig().getKeys();
		for(String key : keys)
		{
			String newKey = key;
			if(key.equals("Version")) continue;
			if(oldConfig.getVersion() < 11)
			{
				if(key.equals("UseUUIDs") || key.equals("BackpackTitle")) continue;
				newKey = key.replace(".MySQL.", ".SQL.");
			}
			try
			{
				config.set(newKey, oldConfig.getConfig().getString(key));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(!oldConfig.getConfig().isSet("Database.UseUUIDs"))
		{
			if(oldConfig.getConfig().isSet("UseUUIDs"))
			{
				config.set("Database.UseUUIDs", oldConfig.getConfig().getBoolean("UseUUIDs", true));
			}
		}
		if(oldConfig.getVersion() < 11)
		{
			config.set("BackpackTitleOther", oldConfig.getConfig().getString("BackpackTitle", "&b{OwnerName} Backpack").replaceAll("%s", "{OwnerName}"));
		}
	}

	//region getter
	//region Database getter
	public int getAutoCleanupMaxInactiveDays()
	{
		return config.getInt("Database.AutoCleanup.MaxInactiveDays", -1);
	}

	public String getDatabaseType()
	{
		return config.getString("Database.Type", "sqlite");
	}

	public String getSQLHost()
	{
		return config.getString("Database.SQL.Host", "localhost");
	}

	public String getSQLDatabase()
	{
		return config.getString("Database.SQL.Database", "minecraft");
	}

	public String getSQLUser()
	{
		return config.getString("Database.SQL.User", "minecraft");
	}

	public String getSQLPassword()
	{
		return config.getString("Database.SQL.Password", "");
	}

	public int getSQLMaxConnections()
	{
		return config.getInt("Database.SQL.MaxConnections", 4);
	}

	public String getUserTable()
	{
		return config.getString("Database.Tables.User", "backpack_players");
	}

	public String getBackpackTable()
	{
		return config.getString("Database.Tables.Backpack", "backpacks");
	}

	public String getCooldownTable()
	{
		return config.getString("Database.Tables.Cooldown", "backpack_cooldowns");
	}

	public String getDBFields(String sub, String def)
	{
		return config.getString("Database.Tables.Fields." + sub, def);
	}

	public boolean getUpdatePlayer()
	{
		return config.getBoolean("Database.UpdatePlayer", true);
	}

	public boolean getUseUUIDs()
	{
		return config.getBoolean("Database.UseUUIDs", true);
	}

	public boolean getUseUUIDSeparators()
	{
		return config.getBoolean("Database.UseUUIDSeparators", false);
	}

	public String getUnCacheStrategie()
	{
		return config.getString("Database.Cache.UnCache.Strategie", "interval").toLowerCase();
	}

	public long getUnCacheInterval()
	{
		return config.getLong("Database.Cache.UnCache.Interval", 600) * 20L;
	}

	public long getUnCacheDelay()
	{
		return config.getLong("Database.Cache.UnCache.Delay", 600) * 20L;
	}
	//endregion

	public String getBPTitleOther()
	{
		return ChatColor.translateAlternateColorCodes('&', config.getString("BackpackTitleOther", "{OwnerName} Backpack").replaceAll("%", "%%").replaceAll("\\{OwnerName\\}", "%s"));
	}

	public String getBPTitle()
	{
		return ChatColor.translateAlternateColorCodes('&', config.getString("BackpackTitle", "Backpack"));
	}

	public boolean getDropOnDeath()
	{
		return config.getBoolean("drop_on_death", true);
	}

	public int getBackpackMaxSize()
	{
		return config.getInt("max_size", 6);
	}

	public boolean getAutoUpdate()
	{
		return config.getBoolean("auto-update", true);
	}

	public long getCommandCooldown()
	{
		return config.getInt("command_cooldown", -1) * 1000L;
	}

	public boolean isCommandCooldownSyncEnabled()
	{
		return config.getBoolean("sync_cooldown", false);
	}

	public Collection<GameMode> getAllowedGameModes()
	{
		Collection<GameMode> gameModes = new HashSet<>();
		for(String string : config.getStringList("allowed_game_modes", new LinkedList<String>()))
		{
			GameMode gm = null;
			try
			{
				//noinspection deprecation
				gm = GameMode.getByValue(Integer.valueOf(string));
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
			logger.info("No game-mode allowed, allowing " + GameMode.SURVIVAL.name());
			gameModes.add(GameMode.SURVIVAL);
		}
		return gameModes;
	}

	//region Full inventory handling
	public boolean getFullInvCollect()
	{
		return config.getBoolean("full_inventory.collect_items", false);
	}

	public long getFullInvCheckInterval()
	{
		return config.getInt("full_inventory.check_interval", 1) * 20L; // in seconds
	}

	public double getFullInvRadius()
	{
		return config.getDouble("full_inventory.collect_radius", 1.5); // in blocks
	}
	//endregion

	//region Shulkerboxes
	public boolean isShulkerboxesPreventInBackpackEnabled()
	{
		return config.getBoolean("Shulkerboxes.PreventInBackpack", true);
	}

	public boolean isShulkerboxesDisable()
	{
		return config.getBoolean("Shulkerboxes.DisableShulkerboxes", false);
	}

	public boolean isShulkerboxesExistingRemoveEnabled()
	{
		return config.getBoolean("Shulkerboxes.Existing.Remove", true);
	}

	public boolean isShulkerboxesExistingDestroyEnabled()
	{
		return config.getBoolean("Shulkerboxes.Existing.Destroy", true);
	}
	//endregion

	//region Item filter
	public boolean isItemFilterEnabled()
	{
		return config.getBoolean("ItemFilter.Enable", false) || config.getBoolean("Shulkerboxes.PreventInBackpack", true);
	}

	public Collection<Material> getItemFilterBlacklist()
	{
		List<String> stringBlacklist = config.getStringList("ItemFilter.Blacklist", new LinkedList<String>());
		Collection<Material> blacklist = new LinkedList<>();
		for(String item : stringBlacklist)
		{
			Material mat = Material.matchMaterial(item);
			if(mat != null) blacklist.add(mat);
		}
		return blacklist;
	}
	//endregion
	//endregion
}