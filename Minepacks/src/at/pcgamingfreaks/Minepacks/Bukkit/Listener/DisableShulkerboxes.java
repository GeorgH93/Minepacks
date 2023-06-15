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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Listener;

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.Util.InventoryUtils;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

public class DisableShulkerboxes extends MinepacksListener
{ //TODO handle existing shulkerboxes in inventory
	protected static final Collection<Material> SHULKER_BOX_MATERIALS = new HashSet<>();

	static
	{
		if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11))
		{
			SHULKER_BOX_MATERIALS.add(Material.BLACK_SHULKER_BOX);
			SHULKER_BOX_MATERIALS.add(Material.BLUE_SHULKER_BOX);
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
			if(MCVersion.isOlderThan(MCVersion.MC_1_13))
			{
				SHULKER_BOX_MATERIALS.add(Material.valueOf("SILVER_SHULKER_BOX"));
			}
			else
			{
				SHULKER_BOX_MATERIALS.add(Material.LIGHT_GRAY_SHULKER_BOX);
				SHULKER_BOX_MATERIALS.add(Material.SHULKER_BOX);
			}
		}
	}

	private boolean removeExisting, dropExistingContent;

	public DisableShulkerboxes(final Minepacks plugin)
	{
		super(plugin);
		removeExisting = plugin.getConfiguration().isShulkerboxesExistingDestroyEnabled();
		dropExistingContent = plugin.getConfiguration().isShulkerboxesExistingDropEnabled();
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
		if(event.getInventory().getHolder() != null && event.getInventory().getHolder().getClass().getName().toLowerCase(Locale.ROOT).contains("shulker"))
		{
			if(removeExisting)
			{
				Block shulkerBlock = ((ShulkerBox) event.getInventory().getHolder()).getBlock();
				if(dropExistingContent)
				{
					InventoryUtils.dropInventory(event.getInventory(), shulkerBlock.getLocation());
				}
				event.getInventory().clear();
				shulkerBlock.setType(Material.AIR);
			}
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
			event.getItemInHand().setType(Material.AIR);
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockMultiPlace(BlockMultiPlaceEvent event)
	{
		if(SHULKER_BOX_MATERIALS.contains(event.getBlock().getType()))
		{
			event.getItemInHand().setType(Material.AIR);
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(handleShulkerBlock(event.getBlock())) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockDamage(BlockDamageEvent event)
	{
		if(handleShulkerBlock(event.getBlock())) event.setCancelled(true);
	}

	private boolean handleShulkerBlock(Block block)
	{
		if(SHULKER_BOX_MATERIALS.contains(block.getType()))
		{
			if(removeExisting)
			{
				ShulkerBox shulkerBox = (ShulkerBox) block.getState();
				if(dropExistingContent) InventoryUtils.dropInventory(shulkerBox.getInventory(), shulkerBox.getLocation());
				shulkerBox.getInventory().clear();
				block.setType(Material.AIR);
			}
			return true;
		}
		return false;
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
	public void onPrepareItemCraftEvent(PrepareItemCraftEvent event)
	{
		if(event.getRecipe() == null) return;
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