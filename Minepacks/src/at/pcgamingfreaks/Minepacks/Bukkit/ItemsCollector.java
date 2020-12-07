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

package at.pcgamingfreaks.Minepacks.Bukkit;

import at.pcgamingfreaks.Minepacks.Bukkit.API.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.ItemFilter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;

public class ItemsCollector extends BukkitRunnable
{
	private final Minepacks plugin;
	private final double radius;
	private final BukkitTask task;
	private final ItemFilter itemFilter;

	public ItemsCollector(Minepacks plugin)
	{
		this.plugin = plugin;
		this.radius = plugin.getConfiguration().getFullInvRadius();
		task = runTaskTimer(plugin, plugin.getConfiguration().getFullInvCheckInterval(), plugin.getConfiguration().getFullInvCheckInterval());
		itemFilter = plugin.getItemFilter();
	}

	@Override
	public void run()
	{
		for(Player player : Bukkit.getServer().getOnlinePlayers())
		{
			if(plugin.isDisabled(player) != WorldBlacklistMode.None) return;
			if(player.getInventory().firstEmpty() == -1 && player.hasPermission(Permissions.USE) && player.hasPermission(Permissions.FULL_PICKUP))
			{
				// Only check loaded backpacks (loading them would take to much time for a repeating task, the backpack will be loaded async soon enough)
				Backpack backpack = (Backpack) plugin.getBackpackCachedOnly(player);
				if(backpack == null)
				{
					continue;
				}
				List<Entity> entities = player.getNearbyEntities(radius, radius, radius);
				for(Entity entity : entities)
				{
					if(entity instanceof Item)
					{
						Item item = (Item) entity;
						if(!item.isDead() && item.getPickupDelay() <= 0)
						{
							if(itemFilter != null && itemFilter.isItemBlocked(item.getItemStack())) continue;
							Map<Integer, ItemStack> full = backpack.addItems(item.getItemStack());
							backpack.setChanged();
							if(!full.isEmpty())
							{
								item.setItemStack(full.get(0));
							}
							else
							{
								item.remove();
							}
						}
					}
				}
			}
		}
	}

	public void close()
	{
		task.cancel();
	}
}