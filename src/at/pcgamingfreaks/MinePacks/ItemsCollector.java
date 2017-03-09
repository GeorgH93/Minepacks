/*
 * Copyright (C) 2014-2015 GeorgH93
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MinePacks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;

public class ItemsCollector extends BukkitRunnable
{
	private double radius = 1.5;
	private MinePacks plugin;

	public ItemsCollector(MinePacks mp)
	{
		plugin = mp;
		radius = plugin.config.getFullInvRadius();
	}

	@Override
	public void run()
	{
		for(Player player : Bukkit.getServer().getOnlinePlayers())
		{
			if(player.getInventory().firstEmpty() == -1 && (player.hasPermission("backpack") || player.hasPermission("backpack.use")) && player.hasPermission("backpack.fullpickup"))
			{
				// Only check loaded backpacks (loading them would take to much time for a repeating task, the will be loaded async soon enough)
				Backpack backpack = plugin.DB.getBackpack(player);
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
							HashMap<Integer, ItemStack> full = backpack.getInventory().addItem(item.getItemStack());
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
}