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
import at.pcgf.libs.com.tcoded.folialib.wrapper.task.WrappedTask;

public class Interval extends UnCacheStrategy implements Runnable
{
	private final WrappedTask task;

	public Interval(Database cache)
	{
		super(cache);
		task = Minepacks.getScheduler().runTimer(this, Minepacks.getInstance().getConfiguration().getUnCacheDelay(), Minepacks.getInstance().getConfiguration().getUnCacheInterval());
	}

	@Override
	public void run()
	{
		for(Backpack backpack : cache.getLoadedBackpacks())
		{
			if(backpack.getOwnerPlayer() == null && !backpack.isOpen())
			{
				this.cache.unloadBackpack(backpack);
			}
		}
	}

	@Override
	public void close()
	{
		Minepacks.getScheduler().cancelTask(task);
		super.close();
	}
}