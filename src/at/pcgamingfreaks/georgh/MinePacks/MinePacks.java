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

package at.pcgamingfreaks.georgh.MinePacks;

import java.util.HashMap;
import java.util.logging.Logger;

import net.gravitydevelopment.Updater.Bukkit_Updater;
import net.gravitydevelopment.Updater.UpdateType;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import at.pcgamingfreaks.georgh.MinePacks.Database.*;

public class MinePacks extends JavaPlugin
{
	public Logger log;
	public Config config;
	public Language lang;
	public Database DB;
	
	public HashMap<Player, Long> cooldowns = new HashMap<>();
	
	public static String BackpackTitle;
	public String Message_InvalidBackpack;

	@Override
	public void onEnable()
	{
		log = getLogger();
		config = new Config(this);
		lang = new Language(this);

		lang.load(config.getLanguage(), config.getLanguageUpdateMode());
		DB = Database.getDatabase(this);
		getCommand("backpack").setExecutor(new OnCommand(this));
		getServer().getPluginManager().registerEvents(new EventListener(this), this);

		if(config.getFullInvCollect())
		{
			(new ItemsCollector(this)).runTaskTimerAsynchronously(this, config.getFullInvCheckInterval(), config.getFullInvCheckInterval());
		}
		
		BackpackTitle = (config.getBPTitle().contains("%s") ? config.getBPTitle() : ChatColor.AQUA + "%s Backpack");
		Message_InvalidBackpack = lang.getTranslated("Ingame.InvalidBackpack");
		getServer().getServicesManager().register(MinePacks.class, this, this, ServicePriority.Normal);
		log.info(lang.get("Console.Enabled"));
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		DB.close();
		if(config.getAutoUpdate())
		{
			new Bukkit_Updater(this, 83445, this.getFile(), UpdateType.DEFAULT, true);
		}
		log.info(lang.get("Console.Disabled"));
	}
	
	public void OpenBackpack(Player opener, OfflinePlayer owner, boolean editable)
	{
		OpenBackpack(opener, DB.getBackpack(owner, false), editable);
	}
	
	public void OpenBackpack(Player opener, Backpack backpack, boolean editable)
	{
		if(backpack == null)
		{
			opener.sendMessage(Message_InvalidBackpack);
			return;
		}
		backpack.Open(opener, editable);
	}
	
	public int getBackpackPermSize(Player player)
	{
		for(int i = 9; i > 1; i--)
		{
			if(player.hasPermission("backpack.size." + i))
			{
				return i * 9;
			}
		}
		return 9;
	}
}