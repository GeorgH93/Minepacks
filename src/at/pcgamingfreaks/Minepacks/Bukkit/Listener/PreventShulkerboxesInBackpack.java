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

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class PreventShulkerboxesInBackpack extends ShulkerboxesListener implements Listener
{
	private final Message messageNotAllowedInBackpack;

	public PreventShulkerboxesInBackpack(final Minepacks plugin)
	{
		super(plugin);
		messageNotAllowedInBackpack = plugin.lang.getMessage("Ingame.Shulkerboxes.NotAllowedInBackpack");
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryMoveItemEvent event)
	{
		if(event.getDestination().getHolder() instanceof Backpack && SHULKER_BOX_MATERIALS.contains(event.getItem().getType()))
		{
			//TODO get player and send him a message
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryDragEvent event)
	{
		if(event.getInventory().getHolder() instanceof Backpack && SHULKER_BOX_MATERIALS.contains(event.getCursor().getType()))
		{
			//TODO get player and send him a message
			event.setCancelled(true);
		}
	}
}