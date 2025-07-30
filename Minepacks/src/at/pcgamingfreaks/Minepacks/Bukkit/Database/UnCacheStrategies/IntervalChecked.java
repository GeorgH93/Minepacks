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
import at.pcgf.libs.com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.OfflinePlayer;

public class IntervalChecked extends UnCacheStrategy implements Runnable
{
	private final long delay;
	private final WrappedTask task;

	public IntervalChecked(Database cache)
	{
		super(cache);
		long delayTicks = Minepacks.getInstance().getConfiguration().getUnCacheDelay();
		task = Minepacks.getScheduler().runTimer(this, delayTicks, Minepacks.getInstance().getConfiguration().getUnCacheInterval());
		this.delay = delayTicks * 50L;
	}

	@Override
	public void run()
	{
		long currentTime = System.currentTimeMillis() - delay;
		for(Backpack backpack : cache.getLoadedBackpacks())
		{
			OfflinePlayer owner = backpack.getOwner();
			if(!owner.isOnline() && owner.getLastPlayed() < currentTime && !backpack.isOpen())
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