/*
 *   Copyright (C) 2014-2017 GeorgH93
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

package at.pcgamingfreaks.MinePacks.Database;

import at.pcgamingfreaks.Bukkit.Configuration;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Config extends Configuration
{
	private static final int CONFIG_VERSION = 15;

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
			if(key.equals("UseUUIDs") || key.equals("Version") || (key.equals("BackpackTitle") && oldConfig.getVersion() < 11)) continue;
			try
			{
				config.set(key, oldConfig.getConfig().getString(key));
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
			else
			{
				config.set("Database.UseUUIDs", Bukkit.getServer().getOnlineMode() && isBukkitVersionUUIDCompatible());
			}
		}
		if(oldConfig.getVersion() < 11)
		{
			config.set("BackpackTitleOther", oldConfig.getConfig().getString("BackpackTitle", "&b{OwnerName} Backpack").replaceAll("%s", "{OwnerName}"));
		}
	}

	// Getter
	public int getAutoCleanupMaxInactiveDays()
	{
		return config.getInt("Database.AutoCleanup.MaxInactiveDays", -1);
	}

	public String getDatabaseType()
	{
		return config.getString("Database.Type", "sqlite");
	}

	public String getMySQLHost()
	{
		return config.getString("Database.MySQL.Host", "localhost");
	}

	public String getMySQLDatabase()
	{
		return config.getString("Database.MySQL.Database", "minecraft");
	}

	public String getMySQLUser()
	{
		return config.getString("Database.MySQL.User", "minecraft");
	}

	public String getMySQLPassword()
	{
		return config.getString("Database.MySQL.Password", "");
	}

	public int getMySQLMaxConnections()
	{
		return config.getInt("Database.MySQL.MaxConnections", 2);
	}

	public String getMySQLProperties()
	{
		List<String> list = config.getStringList("Database.MySQL.Properties", null);
		StringBuilder str = new StringBuilder();
		if(list == null) return "";
		for(String s : list)
		{
			str.append("&").append(s);
		}
		return str.toString();
	}

	public String getUserTable()
	{
		return config.getString("Database.Tables.User", "backpack_players");
	}

	public String getBackpackTable()
	{
		return config.getString("Database.Tables.Backpack", "backpacks");
	}

	public String getDBFields(String sub)
	{
		return config.getString("Database.Tables.Fields." + sub, "");
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

	public long getCommandCooldownAfterJoin()
	{
		return config.getInt("command_cooldown_after_join", -1) * 1000L;
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

	public boolean getShowCloseMessage()
	{
		return config.getBoolean("show_close_message", true);
	}

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

	public boolean isV2InfoDisabled()
	{
		return config.getBoolean("Misc.DisableV2Info", false);
	}
}