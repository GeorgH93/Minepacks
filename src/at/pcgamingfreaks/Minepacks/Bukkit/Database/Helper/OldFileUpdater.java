/*
 *   Copyright (C) 2017 GeorgH93
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

public class OldFileUpdater
{
	public static void updateConfig(YAML oldYAML, YAML newYAML)
	{
		int oldVersion = oldYAML.getInt("Version", -1);
		for(String key : oldYAML.getKeys())
		{
			String newKey = key;
			if(key.equals("Version")) continue;
			if(oldVersion < 11)
			{
				if(key.equals("UseUUIDs") || key.equals("BackpackTitle") || key.equals("DisableV2Info")) continue;
				newKey = key.replace(".MySQL.", ".SQL.");
			}
			switch(key)
			{
				case "command_cooldown": newKey = "CommandCooldown"; break;
				case "sync_cooldown": newKey = "SyncCooldown"; break;
				case "drop_on_death": newKey = "DropOnDeath"; break;
				case "max_size": newKey = "MaxSize"; break;
				case "allowed_game_modes": newKey = "AllowedGameModes"; break;
				case "full_inventory.collect_items": newKey = "FullInventory.CollectItems"; break;
				case "full_inventory.check_interval": newKey = "FullInventory.CheckInterval"; break;
				case "full_inventory.collect_radius": newKey = "FullInventory.CollectRadius"; break;
				case "auto-update": newKey = "Misc.AutoUpdate"; break;
				case "BungeeCordMode": newKey = "Misc.UseBungeeCord"; break;
				case "Language": newKey = "Language.Language"; break;
				case "LanguageUpdateMode": newKey = "Language.UpdateMode"; break;
			}
			try
			{
				newYAML.set(newKey, oldYAML.getString(key));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(!oldYAML.isSet("Database.UseUUIDs"))
		{
			if(oldYAML.isSet("UseUUIDs"))
			{
				newYAML.set("Database.UseUUIDs", oldYAML.getBoolean("UseUUIDs", true));
			}
		}
		if(oldVersion < 11)
		{
			oldYAML.set("BackpackTitleOther", oldYAML.getString("BackpackTitle", "&b{OwnerName} Backpack").replaceAll("%s", "{OwnerName}"));
		}
	}
}