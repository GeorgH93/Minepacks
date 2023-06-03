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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Database.UnCacheStrategies;

import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Database;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Bukkit;

public class IntervalChecked extends UnCacheStrategy implements Runnable
{
	private final long delay;
	private final int taskID;

	public IntervalChecked(Database cache)
	{
		super(cache);
		long delayTicks = Minepacks.getInstance().getConfiguration().getUnCacheDelay();
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Minepacks.getInstance(), this, delayTicks, Minepacks.getInstance().getConfiguration().getUnCacheInterval());
		this.delay = delayTicks * 50L;
	}

	@Override
	public void run()
	{
		long currentTime = System.currentTimeMillis() - delay;
		for(Backpack backpack : cache.getLoadedBackpacks())
		{
			if(!backpack.getOwner().isOnline() && backpack.getOwner().getPlayer().getLastPlayed() < currentTime && !backpack.isOpen())
			{
				this.cache.unloadBackpack(backpack);
			}
		}
	}

	@Override
	public void close()
	{
		Bukkit.getScheduler().cancelTask(taskID);
		super.close();
	}
}