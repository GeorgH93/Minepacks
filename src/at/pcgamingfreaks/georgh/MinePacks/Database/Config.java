/*
 *   Copyright (C) 2014-2015 GeorgH93
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

package at.pcgamingfreaks.georgh.MinePacks.Database;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import at.pcgamingfreaks.georgh.MinePacks.MinePacks;

public class Config
{
	private MinePacks MP;
	private FileConfiguration config;
	private static final int CONFIG_VERSION = 3;
	
	public Config(MinePacks mp)
	{
		MP = mp;
		LoadConfig();
	}
	
	public void Reload()
	{
		LoadConfig();
	}
	
	private void LoadConfig()
	{
		File file = new File(MP.getDataFolder(), "config.yml");
		if(!file.exists())
		{
			NewConfig(file);
		}
		else
		{
			config = YamlConfiguration.loadConfiguration(file);
			UpdateConfig(file);
		}
	}
	
	private boolean UUIDComp()
	{
		try
		{
			String[] GameVersion = Bukkit.getBukkitVersion().split("-");
			GameVersion = GameVersion[0].split("\\.");
			if(Integer.parseInt(GameVersion[1]) > 7 || (Integer.parseInt(GameVersion[1]) == 7 && Integer.parseInt(GameVersion[2]) > 5))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	private void NewConfig(File file)
	{
		config = new YamlConfiguration();
		config.set("BackpackTitle", ChatColor.AQUA + "%s Backpack");
		config.set("drop_on_death", true);
		config.set("Language","en");
		config.set("LanguageUpdateMode","Overwrite");
		config.set("Database.Type","sqlite");
		config.set("Database.UpdatePlayer", true);
		config.set("Database.UseUUIDs", Bukkit.getServer().getOnlineMode() && UUIDComp());
		config.set("Database.UseUUIDSeparators", false);
		config.set("Database.MySQL.Host", "localhost:3306");
		config.set("Database.MySQL.Database", "minecraft");
		config.set("Database.MySQL.User", "minecraft");
		config.set("Database.MySQL.Password", "minecraft");
		config.set("Database.Tables.User", "backpack_players");
		config.set("Database.Tables.Backpack", "backpacks");
		config.set("Database.Tables.Fields.User.Player_ID", "player_id");
		config.set("Database.Tables.Fields.User.Name", "name");
		config.set("Database.Tables.Fields.User.UUID", "uuid");
		config.set("Database.Tables.Fields.Backpack.Owner_ID", "owner");
		config.set("Database.Tables.Fields.Backpack.ItemStacks", "itemstacks");
		config.set("Database.Tables.Fields.Backpack.Version", "version");
		config.set("Version",CONFIG_VERSION);
		
		try 
		{
			config.save(file);
			MP.log.info("Config File has been generated.");
		}
  	  	catch (IOException e) 
  	  	{
  	  		e.printStackTrace();
  	  	}
	}
	
	private boolean UpdateConfig(File file)
	{
		switch(config.getInt("Version"))
		{
			case 1:
				config.set("Database.Tables.Fields.User.Player_ID", "player_id");
				config.set("Database.Tables.Fields.User.Name", "name");
				config.set("Database.Tables.Fields.User.UUID", "uuid");
				config.set("Database.Tables.Fields.Backpack.Owner_ID", "owner");
				config.set("Database.Tables.Fields.Backpack.ItemStacks", "itemstacks");
				config.set("Database.Tables.Fields.Backpack.Version", "version");
			case 2:
				config.set("Database.UseUUIDSeparators", false);
			break;
			case CONFIG_VERSION: return false;
			default: MP.log.info("Config File Version newer than expected!"); return false;
		}
		config.set("Version", CONFIG_VERSION);
		try 
		{
			config.save(file);
			MP.log.info("Config File has been updated.");
		}
  	  	catch (IOException e) 
  	  	{
  	  		e.printStackTrace();
  	  		return false;
  	  	}
		return true;
	}
	
	public String GetLanguage()
	{
		return config.getString("Language");
	}
	
	public String GetLanguageUpdateMode()
	{
		return config.getString("LanguageUpdateMode");
	}
	
	public String GetDatabaseType()
	{
		return config.getString("Database.Type");
	}
	
	public String GetMySQLHost()
	{
		return config.getString("Database.MySQL.Host");
	}
	
	public String GetMySQLDatabase()
	{
		return config.getString("Database.MySQL.Database");
	}
	
	public String GetMySQLUser()
	{
		return config.getString("Database.MySQL.User");
	}
	
	public String GetMySQLPassword()
	{
		return config.getString("Database.MySQL.Password");
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
		return config.getString("Database.Tables.Fields." + sub);
	}
	
	public boolean getUpdatePlayer()
	{
		return config.getBoolean("Database.UpdatePlayer", true);
	}
	
	public boolean UseUUIDs()
	{
		if(config.isSet("Database.UseUUIDs"))
		{
			return config.getBoolean("Database.UseUUIDs");
		}
		return config.getBoolean("UseUUIDs");
	}
	
	public boolean getUseUUIDSeparators()
	{
		return config.getBoolean("Database.UseUUIDSeparators");
	}
	
	public String getBPTitle()
	{
		String BPTitle = config.getString("BackpackTitle", "%s Backpack");
		return BPTitle;
	}
	
	public boolean getDropOnDeath()
	{
		return config.getBoolean("drop_on_death", true);
	}
}