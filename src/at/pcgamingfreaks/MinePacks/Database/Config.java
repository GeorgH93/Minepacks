/*
 *   Copyright (C) 2014-2016 GeorgH93
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class Config extends Configuration
{
	private static final int CONFIG_VERSION = 10;

	public Config(JavaPlugin plugin)
	{
		super(plugin, CONFIG_VERSION, 10);
	}

	@Override
	protected boolean newConfigCreated()
	{
		config.set("Database.UseUUIDs", Bukkit.getServer().getOnlineMode() && isBukkitVersionUUIDCompatible());
		return !(Bukkit.getServer().getOnlineMode() && isBukkitVersionUUIDCompatible());
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
			if(key.equals("UseUUIDs") || key.equals("Version"))
				continue;
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
				config.set("Database.UseUUIDs", config.getBoolean("UseUUIDs", true));
			}
			else
			{
				config.set("Database.UseUUIDs", Bukkit.getServer().getOnlineMode() && isBukkitVersionUUIDCompatible());
			}
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
		return config.getInt("Database.MySQL.MaxConnections", 4);
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

	public String getBPTitle()
	{
		return ChatColor.translateAlternateColorCodes('&', config.getString("BackpackTitle", "%s Backpack"));
	}

	public boolean getDropOnDeath()
	{
		return config.getBoolean("drop_on_death", true);
	}

	public boolean getAutoUpdate()
	{
		return config.getBoolean("auto-update", true);
	}

	public int getCommandCooldown()
	{
		return config.getInt("command_cooldown", -1) * 1000;
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
}