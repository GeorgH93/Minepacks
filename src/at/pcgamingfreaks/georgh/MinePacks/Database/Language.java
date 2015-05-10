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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.io.Files;

import at.pcgamingfreaks.georgh.MinePacks.MinePacks;

public class Language
{
	private MinePacks MP;
	private FileConfiguration lang;
	private static final int LANG_VERSION = 2;

	public Language(MinePacks mp) 
	{
		MP = mp;
		LoadFile();
	}
	
	public String Get(String Option)
	{
		return lang.getString("Language." + Option, "§cMessage not found!");
	}
	
	public void Reload()
	{
		LoadFile();
	}
	
	private void LoadFile()
	{
		File file = new File(MP.getDataFolder() + File.separator + "Lang", MP.config.GetLanguage()+".yml");
		if(!file.exists())
		{
			ExtractLangFile(file);
		}
		lang = YamlConfiguration.loadConfiguration(file);
		UpdateLangFile(file);
	}
	
	private void ExtractLangFile(File Target)
	{
		try
		{
			MP.saveResource("Lang" + File.separator + MP.config.GetLanguage() + ".yml", true);
		}
		catch(Exception ex)
		{
			try
			{
				File file_en = new File(MP.getDataFolder() + File.separator + "Lang", "en.yml");
				if(!file_en.exists())
				{
					MP.saveResource("Lang" + File.separator + "en.yml", true);
				}
				Files.copy(file_en, Target);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private boolean UpdateLangFile(File file)
	{
		if(lang.getInt("Version") != LANG_VERSION)
		{
			if(MP.config.GetLanguageUpdateMode().equalsIgnoreCase("overwrite"))
			{
				ExtractLangFile(file);
				LoadFile();
				MP.log.info(Get("Console.LangUpdated"));
				return true;
			}
			else
			{
				switch(lang.getInt("Version"))
				{
					case 1: lang.set("Language.Ingame.Cooldown", "Please wait till you reopen your backpack.");
						break;
					default: MP.log.warning("Language File Version newer than expected!"); return false;
				}
				lang.set("Version", LANG_VERSION);
				try 
				{
					lang.save(file);
					MP.log.info(Get("Console.LangUpdated"));
				}
		  	  	catch (IOException e) 
		  	  	{
		  	  		e.printStackTrace();
		  	  	}
				return true;
			}
		}
		return false;
	}
}
