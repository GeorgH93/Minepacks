/*
 *   Copyright (C) 2017, 2018 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper;

import at.pcgamingfreaks.yaml.YAML;

import org.bukkit.ChatColor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class OldFileUpdater
{
	public static void updateConfig(YAML oldYAML, YAML newYAML)
	{
		int oldVersion = oldYAML.getInt("Version", -1);
		for(String key : newYAML.getKeys())
		{
			String oldKey = key;
			if(key.equals("Version")) continue;
			if(oldVersion < 11)
			{
				if(key.equals("BackpackTitle") || key.equals("BackpackTitleOther")) continue;
				oldKey = key.replace(".SQL.", ".MySQL.");
			}
			switch(key)
			{
				case "CommandCooldown": oldKey = "command_cooldown"; break;
				case "SyncCooldown": oldKey = "sync_cooldown"; break;
				case "DropOnDeath": oldKey = "drop_on_death"; break;
				case "MaxSize": oldKey = "max_size"; break;
				case "AllowedGameModes": oldKey = "allowed_game_modes"; break;
				case "FullInventory.CollectItems": oldKey = "full_inventory.collect_items"; break;
				case "FullInventory.CheckInterval": oldKey = "full_inventory.check_interval"; break;
				case "FullInventory.CollectRadius": oldKey = "full_inventory.collect_radius"; break;
				case "Misc.AutoUpdate": oldKey = "auto-update"; break;
				case "Misc.UseBungeeCord": oldKey = "BungeeCordMode"; break;
				case "Language.Language": oldKey = "Language"; break;
				case "Language.UpdateMode": oldKey = "LanguageUpdateMode"; break;
				case "Database.UseUUIDs": if(!oldYAML.isSet("Database.UseUUIDs") && oldYAML.isSet("UseUUIDs")) oldKey = "UseUUIDs"; break;
			}
			try
			{
				newYAML.set(key, oldYAML.getString(oldKey));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(oldVersion < 11)
		{
			newYAML.set("BackpackTitleOther", oldYAML.getString("BackpackTitle", "&b{OwnerName} Backpack").replaceAll("%s", "{OwnerName}"));
		}
	}

	public static void updateLanguage(YAML oldYAML, YAML newYAML, Logger logger)
	{
		Map<String, String> simpleConverter = new LinkedHashMap<>(), advancedConverter = new LinkedHashMap<>();
		String[] keys;
		String helper;
		for(String key : oldYAML.getKeys(true))
		{
			try
			{
				keys = key.split("\\.");
				if(keys.length == 3)
				{
					switch(keys[1])
					{
						case "Console":
							switch(keys[2])
							{
								case "NotFromConsole": advancedConverter.put("Language.NotFromConsole", ChatColor.RED + oldYAML.getString(key)); break;
							}
							break;
						case "Ingame":
							helper = keys[0] + "." + keys[1] + ".";
							switch(keys[2])
							{
								case "NoPermission": advancedConverter.put(key, ChatColor.RED + oldYAML.getString(key)); break;
								case "OwnBackPackClose": simpleConverter.put(helper + "OwnBackpackClose", key); break;
								case "PlayerBackPackClose": advancedConverter.put(helper + "PlayerBackpackClose", oldYAML.getString(key).replace("%s", "{OwnerName}")); break;
								case "InvalidBackpack": simpleConverter.put(key, key); break;
								case "BackpackCleaned": simpleConverter.put(key, key); break;
								case "Cooldown": advancedConverter.put(key, ChatColor.DARK_GREEN + oldYAML.getString(key)); break;
							}
							break;
						case "Description":
							helper = "Language.Commands.Description.";
							simpleConverter.put(helper + keys[2], key);
							break;
					}
				}
			}
			catch(Exception e)
			{
				logger.warning("Failed to convert the old \"" + key + "\" language value into the corresponding new one.");
				e.printStackTrace();
			}
		}

		// Patch them into the lang file
		try
		{
			for(Map.Entry<String, String> entry : advancedConverter.entrySet())
			{
				newYAML.set(entry.getKey(), entry.getValue());
			}
			for(Map.Entry<String, String> entry : simpleConverter.entrySet())
			{
				newYAML.set(entry.getKey(), oldYAML.getString(entry.getValue()));
			}
		}
		catch(Exception e)
		{
			logger.warning("Failed to write the old language values into the new language file.");
			e.printStackTrace();
		}
	}
}