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

import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

//TODO remove shulkerboxes
public class DisableShulkerboxes extends MinepacksListener
{
	protected static final Collection<Material> SHULKER_BOX_MATERIALS = new HashSet<>();

	static
	{
		SHULKER_BOX_MATERIALS.add(Material.BLACK_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.BLUE_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.SILVER_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.BROWN_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.CYAN_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.GREEN_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.GRAY_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.LIGHT_BLUE_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.LIME_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.MAGENTA_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.ORANGE_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.PINK_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.PURPLE_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.RED_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.WHITE_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.YELLOW_SHULKER_BOX);
	}

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
		if(event.getInventory() != null && event.getInventory().getHolder() != null && event.getInventory().getHolder().getClass().getName().toLowerCase().contains("shulker"))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryMoveItemEvent event)
	{
		if(event.getItem() != null && SHULKER_BOX_MATERIALS.contains(event.getItem().getType()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryDragEvent event)
	{
		if(event.getCursor() != null && SHULKER_BOX_MATERIALS.contains(event.getCursor().getType()))
		{
			event.setCancelled(true);
		}
		else if(event.getOldCursor() != null && SHULKER_BOX_MATERIALS.contains(event.getOldCursor().getType()))
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

	@EventHandler(ignoreCancelled = true)
	public void onPrepareItemCraftEvent(PrepareItemCraftEvent event)
	{
		Material itemType = event.getRecipe().getResult().getType();
		if(itemType == Material.SHULKER_SHELL || SHULKER_BOX_MATERIALS.contains(itemType))
		{
			event.getInventory().setResult(new ItemStack(Material.AIR));
			//TODO message
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onDrop(PlayerDropItemEvent event)
	{
		//TODO null checks
		Material itemType = event.getItemDrop().getItemStack().getType();
		if(itemType == Material.SHULKER_SHELL || SHULKER_BOX_MATERIALS.contains(itemType))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onDrop(EntityDeathEvent event)
	{
		Iterator<ItemStack> it = event.getDrops().iterator();
		while(it.hasNext())
		{
			Material itemType = it.next().getType();
			if(itemType == Material.SHULKER_SHELL || SHULKER_BOX_MATERIALS.contains(itemType))
			{
				it.remove();
			}
		}
	}
}