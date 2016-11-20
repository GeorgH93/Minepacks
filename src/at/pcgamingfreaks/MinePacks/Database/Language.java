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

import at.pcgamingfreaks.yaml.YAML;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;

public class Language extends at.pcgamingfreaks.Bukkit.Language
{
	private static final int LANG_VERSION = 10, UPGRADE_THRESHOLD = 10;

	public Language(JavaPlugin plugin)
	{
		super(plugin, LANG_VERSION, UPGRADE_THRESHOLD);
	}

	@Override
	protected void doUpdate() {}

	@Override
	protected void doUpgrade(at.pcgamingfreaks.Language oldLang)
	{
		if(oldLang.getVersion() < UPGRADE_THRESHOLD)
		{
			YAML oldYAML = oldLang.getLang(), newYAML = getLang();
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
								switch(keys[2])
								{
									case "NoPermission": advancedConverter.put(key, ChatColor.RED + oldYAML.getString(key)); break;
									case "OwnBackPackClose": simpleConverter.put(key, key); break;
									case "PlayerBackPackClose": simpleConverter.put(key, key); break;
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
					plugin.getLogger().warning("Failed to convert the old \"" + key + "\" language value into the corresponding new one.");
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
				plugin.getLogger().warning("Failed to write the old language values into the new language file.");
				e.printStackTrace();
			}
		}
		else
		{
			super.doUpgrade(oldLang);
		}
	}
}