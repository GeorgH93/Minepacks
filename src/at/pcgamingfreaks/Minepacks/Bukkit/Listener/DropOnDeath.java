/*
 *   Copyright (C) 2016 GeorgH93
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

import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DropOnDeath extends MinepacksListener
{
	public DropOnDeath(Minepacks plugin)
	{
		super(plugin);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		final Player player = event.getEntity();
		if (!player.hasPermission("backpack.keepOnDeath"))
		{
			plugin.getBackpack(player, new Callback<Backpack>()
			{
				@Override
				public void onResult(Backpack backpack)
				{
					Inventory backpackInventory = backpack.getInventory();
					for(ItemStack i : backpackInventory.getContents())
					{
						if(i != null)
						{
							player.getWorld().dropItemNaturally(player.getLocation(), i);
							backpackInventory.remove(i);
							backpack.setChanged();
						}
					}
					backpack.save();
				}

				@Override
				public void onFail() {}
			});
		}
	}
}