/*
 *   Copyright (C) 2023 GeorgH93
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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Database.UnCacheStrategies;

import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Database;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class OnDisconnect extends UnCacheStrategy implements Listener
{
	public OnDisconnect(Database cache)
	{
		super(cache);
		Bukkit.getPluginManager().registerEvents(this, Minepacks.getInstance());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerLeaveEvent(PlayerQuitEvent event)
	{
		Backpack backpack = cache.getBackpack(event.getPlayer());
		if(backpack != null && !backpack.isOpen())
		{
			cache.unloadBackpack(backpack);
		}
	}

	@Override
	public void close()
	{
		HandlerList.unregisterAll(this);
	}
}