/*
 *   Copyright (C) 2016, 2017 GeorgH93
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

package at.pcgamingfreaks.MinePacks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MinepacksV2IsAvailable implements Listener
{
	public static MinepacksV2IsAvailable instance = null;
	private MinePacks plugin;

	public MinepacksV2IsAvailable(MinePacks plugin)
	{
		if(plugin.config.isV2InfoDisabled()) return;
		instance = this;
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "#####################################");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Minepacks V2 has been released! " + ChatColor.YELLOW + ":)");
		Bukkit.getConsoleSender().sendMessage("Please download it form here:" + ChatColor.AQUA + " https://www.spigotmc.org/resources/19286/");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "#####################################");
	}

	public void announce(Player player)
	{
		if(player != null && player.isOnline())
		{
			player.sendMessage(ChatColor.GRAY + "#####################################");
			player.sendMessage(ChatColor.GOLD + "Minepacks V2 has been released! " + ChatColor.YELLOW + ":)");
			player.sendMessage("Please download it form here:" + ChatColor.AQUA + " https://www.spigotmc.org/resources/19286/");
			player.sendMessage(ChatColor.GRAY + "#####################################");
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();
		if(player.isOp())
		{
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					announce(player);
				}
			}, 100L);
		}
	}
}