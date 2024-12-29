/*
 *   Copyright (C) 2024 GeorgH93
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

import at.pcgamingfreaks.Bukkit.ItemNameResolver;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.Placeholder.Processors.ItemDisplayNamePlaceholderProcessor;
import at.pcgamingfreaks.Bukkit.Message.Placeholder.Processors.ItemNamePlaceholderProcessor;
import at.pcgamingfreaks.Bukkit.MinecraftMaterial;
import at.pcgamingfreaks.Message.Placeholder.Placeholder;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ItemFilter extends MinepacksListener implements at.pcgamingfreaks.Minepacks.Bukkit.API.ItemFilter
{
	public final Message messageNotAllowedInBackpack;
	public final ItemNameResolver itemNameResolver;
	private final at.pcgamingfreaks.Bukkit.ItemFilter filter;

	public ItemFilter(final Minepacks plugin)
	{
		super(plugin);

		boolean whitelistMode = plugin.getConfiguration().isItemFilterModeWhitelist();
		filter = new at.pcgamingfreaks.Bukkit.ItemFilter(whitelistMode);
		Collection<MinecraftMaterial> filteredMaterials = plugin.getConfiguration().getItemFilterMaterials();
		if(plugin.getConfiguration().isShulkerboxesPreventInBackpackEnabled() && !whitelistMode)
		{
			for(Material mat : DisableShulkerboxes.SHULKER_BOX_MATERIALS)
			{
				filteredMaterials.add(new MinecraftMaterial(mat, (short) -1));
			}
		}
		filter.addFilteredMaterials(filteredMaterials);
		filter.addFilteredNames(plugin.getConfiguration().getItemFilterNames());
		filter.addFilteredLore(plugin.getConfiguration().getItemFilterLore());

		/*if[STANDALONE]
		itemNameResolver = new ItemNameResolver();
		itemNameResolver.load(plugin, plugin.getConfiguration());
		else[STANDALONE]*/
		itemNameResolver = at.pcgamingfreaks.PluginLib.Bukkit.ItemNameResolver.getInstance();
		/*end[STANDALONE]*/

		messageNotAllowedInBackpack = plugin.getLanguage().getMessage("Ingame.NotAllowedInBackpack").placeholders(new Placeholder("ItemName", new ItemNamePlaceholderProcessor(itemNameResolver)), new Placeholder("ItemDisplayName", new ItemDisplayNamePlaceholderProcessor(itemNameResolver)));
	}

	@Override
	public boolean isItemBlocked(final @Nullable ItemStack item)
	{
		return filter.isItemBlocked(item);
	}

	@Override
	public void sendNotAllowedMessage(@NotNull Player player, @NotNull ItemStack itemStack)
	{
		messageNotAllowedInBackpack.send(player, itemStack);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryMoveItemEvent event)
	{
		if(event.getDestination().getType() == InventoryType.CHEST && event.getDestination().getHolder() instanceof Backpack && isItemBlocked(event.getItem()))
		{
			if(event.getSource().getHolder() instanceof Player)
			{
				sendNotAllowedMessage((Player) event.getSource().getHolder(), event.getItem());
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemClick(InventoryClickEvent event)
	{
		if(!(event.getWhoClicked() instanceof Player)) return;
		if(event.getInventory().getType() == InventoryType.CHEST && event.getInventory().getHolder() instanceof Backpack)
		{
			Player player = (Player) event.getWhoClicked();
			if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && checkIsBlockedAndShowMessage(player, event.getCurrentItem()))
			{
				event.setCancelled(true);
			}
			else if((event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP) && event.getHotbarButton() != -1)
			{
				ItemStack item = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
				if(checkIsBlockedAndShowMessage(player, item))
				{
					event.setCancelled(true);
				}
			}
			else if((event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP) && event.getClick().name().equals("SWAP_OFFHAND"))
			{
				if(checkIsBlockedAndShowMessage(player, player.getInventory().getItemInOffHand()))
				{
					event.setCancelled(true);
				}
			}
			else if(!player.getInventory().equals(event.getClickedInventory()) && checkIsBlockedAndShowMessage(player, event.getCursor()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemDrag(InventoryDragEvent event)
	{
		if(event.getInventory().getType() == InventoryType.CHEST && event.getInventory().getHolder() instanceof Backpack && (isItemBlocked(event.getOldCursor()) || isItemBlocked(event.getCursor())) && event.getRawSlots().containsAll(event.getInventorySlots()))
		{
			sendNotAllowedMessage((Player) event.getView().getPlayer(), event.getOldCursor());
			event.setCancelled(true);
		}
	}
}