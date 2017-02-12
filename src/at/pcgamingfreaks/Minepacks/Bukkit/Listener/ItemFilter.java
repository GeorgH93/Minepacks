/*
 *   Copyright (C) 2016-2017 GeorgH93
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

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

import java.util.Collection;
import java.util.HashSet;

public class ItemFilter extends MinepacksListener
{
	private final Message messageNotAllowedInBackpack;
	private final Collection<Material> blockedMaterials = new HashSet<>();

	public ItemFilter(final Minepacks plugin)
	{
		super(plugin);

		if(plugin.getConfiguration().isShulkerboxesPreventInBackpackEnabled())
		{
			blockedMaterials.addAll(DisableShulkerboxes.SHULKER_BOX_MATERIALS);
		}
		blockedMaterials.addAll(plugin.getConfiguration().getItemFilterBlacklist());

		messageNotAllowedInBackpack = plugin.lang.getMessage("Ingame.Shulkerboxes.NotAllowedInBackpack"); //TODO change message
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryMoveItemEvent event)
	{
		if(event.getDestination().getHolder() instanceof Backpack && blockedMaterials.contains(event.getItem().getType()))
		{
			if(event.getSource().getHolder() instanceof Player)
			{
				messageNotAllowedInBackpack.send((Player) event.getSource().getHolder());
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryClickEvent event)
	{
		if(event.getInventory().getHolder() instanceof Backpack && event.getCurrentItem() != null && blockedMaterials.contains(event.getCurrentItem().getType()))
		{
			messageNotAllowedInBackpack.send(event.getView().getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryDragEvent event)
	{
		if(event.getInventory().getHolder() instanceof Backpack && event.getOldCursor() != null && blockedMaterials.contains(event.getOldCursor().getType()))
		{
			messageNotAllowedInBackpack.send(event.getView().getPlayer());
			event.setCancelled(true);
		}
	}
}