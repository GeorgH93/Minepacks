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
	public final Logger log = getLogger();
	public final Config config = new Config(this);
	public Language lang;
	public Database DB;
	
	public HashMap<Player, Long> cooldowns = new HashMap<Player, Long>();
	
	public static String BackpackTitle;
	public String Message_InvalidBackpack;

	@Override
	public void onEnable()
	{
		lang = new Language(this);
		DB = Database.getDatabase(this);
		getCommand("backpack").setExecutor(new OnCommand(this));
		getServer().getPluginManager().registerEvents(new EventListener(this), this);

		if(config.getFullInvCollect())
		{
			(new ItemsCollector(this)).runTaskTimerAsynchronously(this, config.getFullInvCheckInterval(), config.getFullInvCheckInterval());
		}
		
		BackpackTitle = config.getBPTitle();
		Message_InvalidBackpack = ChatColor.translateAlternateColorCodes('&', ChatColor.RED + lang.Get("Ingame.InvalidBackpack"));
		getServer().getServicesManager().register(MinePacks.class, this, this, ServicePriority.Normal);
		log.info(lang.Get("Console.Enabled"));
	}

	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		DB.Close();
		if(config.getAutoUpdate())
		{
			new Bukkit_Updater(this, 83445, this.getFile(), UpdateType.DEFAULT, true);
		}
		log.info(lang.Get("Console.Disabled"));
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
		if(player.hasPermission("backpack.size.9"))
		{
			return 81;
		}
		else if(player.hasPermission("backpack.size.8"))
		{
			return 72;
		}
		else if(player.hasPermission("backpack.size.7"))
		{
			return 63;
		}
		else if(player.hasPermission("backpack.size.6"))
		{
			return 54;
		}
		else if(player.hasPermission("backpack.size.5"))
		{
			return 45;
		}
		else if(player.hasPermission("backpack.size.4"))
		{
			return 36;
		}
		else if(player.hasPermission("backpack.size.3"))
		{
			return 27;
		}
		else if(player.hasPermission("backpack.size.2"))
		{
			return 18;
		}
		else
		{
			return 9;
		}
	}
}