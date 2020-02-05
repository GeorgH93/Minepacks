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

package at.pcgamingfreaks.Minepacks.Bukkit.Listener;

import at.pcgamingfreaks.Bukkit.ItemNameResolver;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.MinecraftMaterial;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;

public class ItemFilter extends MinepacksListener implements at.pcgamingfreaks.Minepacks.Bukkit.API.ItemFilter
{
	private final Message messageNotAllowedInBackpack;
	private final Collection<MinecraftMaterial> blockedMaterials = new HashSet<>();
	private final ItemNameResolver itemNameResolver;

	public ItemFilter(final Minepacks plugin)
	{
		super(plugin);

		if(plugin.getConfiguration().isShulkerboxesPreventInBackpackEnabled())
		{
			for(Material mat : DisableShulkerboxes.SHULKER_BOX_MATERIALS)
			{
				blockedMaterials.add(new MinecraftMaterial(mat, (short) -1));
			}
		}
		blockedMaterials.addAll(plugin.getConfiguration().getItemFilterBlacklist());

		messageNotAllowedInBackpack = plugin.getLanguage().getMessage("Ingame.NotAllowedInBackpack").replaceAll("\\{ItemName}", "%s");

		/*if[STANDALONE]
		itemNameResolver = new ItemNameResolver();
		if (at.pcgamingfreaks.Bukkit.MCVersion.isOlderThan(at.pcgamingfreaks.Bukkit.MCVersion.MC_1_13))
		{
			at.pcgamingfreaks.Bukkit.Language itemNameLanguage = new at.pcgamingfreaks.Bukkit.Language(plugin, 1, 1, java.io.File.separator + "lang", "items_", "legacy_items_");
			itemNameLanguage.setFileDescription("item name language");
			itemNameLanguage.load(plugin.getConfiguration().getLanguage(), at.pcgamingfreaks.YamlFileUpdateMethod.OVERWRITE);
			itemNameResolver.loadLegacy(itemNameLanguage, plugin.getLogger());
		}
		else
		{
			at.pcgamingfreaks.Bukkit.Language itemNameLanguage = new at.pcgamingfreaks.Bukkit.Language(plugin, 2, java.io.File.separator + "lang", "items_");
			itemNameLanguage.setFileDescription("item name language");
			itemNameLanguage.load(plugin.getConfiguration().getLanguage(), at.pcgamingfreaks.YamlFileUpdateMethod.OVERWRITE);
			itemNameResolver.load(itemNameLanguage, plugin.getLogger());
		}
		else[STANDALONE]*/
		itemNameResolver = at.pcgamingfreaks.PluginLib.Bukkit.ItemNameResolver.getInstance();
		/*end[STANDALONE]*/
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryMoveItemEvent event)
	{
		if(event.getDestination().getHolder() instanceof Backpack && isItemBlocked(event.getItem()))
		{
			if(event.getSource().getHolder() instanceof Player)
			{
				messageNotAllowedInBackpack.send((Player) event.getSource().getHolder(), itemNameResolver.getName(event.getItem()));
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryClickEvent event)
	{
		if(event.getInventory().getHolder() instanceof Backpack)
		{
			if((event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP) && event.getHotbarButton() != -1)
			{
				ItemStack item = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
				if(item != null && isItemBlocked(item))
				{
					event.setCancelled(true);
					messageNotAllowedInBackpack.send(event.getView().getPlayer(), itemNameResolver.getName(item));
				}
			}
			else if(event.getCurrentItem() != null && isItemBlocked(event.getCurrentItem()))
			{
				if(event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Backpack) return; // Allow user to move blacklisted items out of the backpack

				messageNotAllowedInBackpack.send(event.getView().getPlayer(), itemNameResolver.getName(event.getCurrentItem()));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMove(InventoryDragEvent event)
	{
		if(event.getInventory().getHolder() instanceof Backpack && event.getOldCursor() != null && isItemBlocked(event.getOldCursor()))
		{
			messageNotAllowedInBackpack.send(event.getView().getPlayer(), itemNameResolver.getName(event.getOldCursor()));
			event.setCancelled(true);
		}
	}

	@Override
	public boolean isItemBlocked(ItemStack item)
	{
		return blockedMaterials.contains(new MinecraftMaterial(item));
	}
}