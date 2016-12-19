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

import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;

public class DisableShulkerboxes extends ShulkerboxesListener implements Listener
{
	public DisableShulkerboxes(final Minepacks plugin)
	{
		super(plugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onCraft(CraftItemEvent event)
	{
		if(event.getCurrentItem() != null && SHULKER_BOX_MATERIALS.contains(event.getCurrentItem().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event)
	{
		if(event.getCurrentItem() != null && SHULKER_BOX_MATERIALS.contains(event.getCurrentItem().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event)
	{
		if(event.getInventory().getHolder().getClass().getName().toLowerCase().contains("shulker"))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryMoveItemEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getItem().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryDragEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getCursor().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPickup(InventoryPickupItemEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getItem().getItemStack().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getEntity().getItemStack().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getBlockPlaced().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getBlock().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getBlock().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockCanBuild(BlockCanBuildEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getBlock().getType()))
		{
			event.setBuildable(false);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockDispense(BlockDispenseEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getBlock().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockMultiPlaceEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getBlock().getType()))
		{
			event.setCancelled(true);
		}
	}
}