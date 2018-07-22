/*
 *   Copyright (C) 2014-2018 GeorgH93
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
import at.pcgamingfreaks.YamlFileManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Config extends Configuration
{
	private static final int CONFIG_VERSION = 16;

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
	protected void doUpgrade(YamlFileManager oldConfig)
	{
		Set<String> keys = oldConfig.getYaml().getKeys();
		for(String key : keys)
		{
			if(key.equals("UseUUIDs") || key.equals("Version") || (key.equals("BackpackTitle") && oldConfig.getVersion() < 11)) continue;
			try
			{
				getConfig().set(key, oldConfig.getYaml().getString(key));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(!oldConfig.getYaml().isSet("Database.UseUUIDs"))
		{
			if(oldConfig.getYaml().isSet("UseUUIDs"))
			{
				getConfig().set("Database.UseUUIDs", oldConfig.getYaml().getBoolean("UseUUIDs", true));
			}
			else
			{
				getConfig().set("Database.UseUUIDs", Bukkit.getServer().getOnlineMode() && isBukkitVersionUUIDCompatible());
			}
		}
		if(oldConfig.getVersion() < 11)
		{
			getConfig().set("BackpackTitleOther", oldConfig.getYaml().getString("BackpackTitle", "&b{OwnerName} Backpack").replaceAll("%s", "{OwnerName}"));
		}
	}

	// Getter
	public int getAutoCleanupMaxInactiveDays()
	{
		return getConfig().getInt("Database.AutoCleanup.MaxInactiveDays", -1);
	}

	public String getDatabaseType()
	{
		return getConfig().getString("Database.Type", "sqlite");
	}

	public String getMySQLHost()
	{
		return getConfig().getString("Database.MySQL.Host", "localhost");
	}

	public String getMySQLDatabase()
	{
		return getConfig().getString("Database.MySQL.Database", "minecraft");
	}

	public String getMySQLUser()
	{
		return getConfig().getString("Database.MySQL.User", "minecraft");
	}

	public String getMySQLPassword()
	{
		return getConfig().getString("Database.MySQL.Password", "");
	}

	public int getMySQLMaxConnections()
	{
		return getConfig().getInt("Database.MySQL.MaxConnections", 2);
	}

	public String getMySQLProperties()
	{
		List<String> list = getConfig().getStringList("Database.MySQL.Properties", null);
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
		return getConfig().getString("Database.Tables.User", "backpack_players");
	}

	public String getBackpackTable()
	{
		return getConfig().getString("Database.Tables.Backpack", "backpacks");
	}

	public String getDBFields(String sub)
	{
		return getConfig().getString("Database.Tables.Fields." + sub, "");
	}

	public boolean getUpdatePlayer()
	{
		return getConfig().getBoolean("Database.UpdatePlayer", true);
	}

	public boolean getUseUUIDs()
	{
		return getConfig().getBoolean("Database.UseUUIDs", true);
	}

	public boolean getUseUUIDSeparators()
	{
		return getConfig().getBoolean("Database.UseUUIDSeparators", false);
	}

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
		return getConfig().getBoolean("drop_on_death", true);
	}

	public int getBackpackMaxSize()
	{
		return getConfig().getInt("max_size", 6);
	}

	public boolean getAutoUpdate()
	{
		return getConfig().getBoolean("auto-update", true);
	}

	public long getCommandCooldown()
	{
		return getConfig().getInt("command_cooldown", -1) * 1000L;
	}

	public long getCommandCooldownAfterJoin()
	{
		return getConfig().getInt("command_cooldown_after_join", -1) * 1000L;
	}

	public Collection<GameMode> getAllowedGameModes()
	{
		Collection<GameMode> gameModes = new HashSet<>();
		for(String string : getConfig().getStringList("allowed_game_modes", new LinkedList<String>()))
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
		return getConfig().getBoolean("show_close_message", true);
	}

	public boolean getFullInvCollect()
	{
		return getConfig().getBoolean("full_inventory.collect_items", false);
	}

	public long getFullInvCheckInterval()
	{
		return getConfig().getInt("full_inventory.check_interval", 1) * 20L; // in seconds
	}

	public double getFullInvRadius()
	{
		return getConfig().getDouble("full_inventory.collect_radius", 1.5); // in blocks
	}

	public boolean isV2InfoDisabled()
	{
		return getConfig().getBoolean("DisableV2Info", false);
	}

	public boolean isBungeeCordModeEnabled()
	{
		return getConfig().getBoolean("BungeeCordMode", false);
	}
}