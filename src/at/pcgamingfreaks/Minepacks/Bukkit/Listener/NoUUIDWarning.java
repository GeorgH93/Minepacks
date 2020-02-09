/*
 *   Copyright (C) 2020 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Listener;

import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NoUUIDWarning implements Listener
{
	public NoUUIDWarning(){}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onAdminJoin(PlayerJoinEvent event)
	{
		if(event.getPlayer().hasPermission("backpack.reload"))
		{
			final Player player = event.getPlayer();
			Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(Minepacks.getInstance(), () -> {
				if(player.isOnline())
				{
					String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.YELLOW + "Minepacks" + ChatColor.DARK_GRAY + "] " + ChatColor.RED;
					player.sendMessage(ChatColor.RED + "########## Warning ##########");
					player.sendMessage(prefix + "With the upcoming v2.2 update the option to disable UUIDs will be removed.");
					player.sendMessage(prefix + "If you are using BungeeCord or another proxy the plugin will not be able to autodetect if it should use online or offline mode UUIDs and you should set the 'UUID_Type' config option manually before updating to v2.2.");
					player.sendMessage(prefix + "Please note that if you have many old players in your database already it is possible that some have already changed their name and will fail to convert to UUID. You will have to remove them manually from your database after converting to UUIDs. Not doing so will make the plugin try to convert them to UUIDs on every startup/reload.");
					player.sendMessage(ChatColor.RED + "########## Warning ##########");
				}
			}, 40);
		}
	}
}