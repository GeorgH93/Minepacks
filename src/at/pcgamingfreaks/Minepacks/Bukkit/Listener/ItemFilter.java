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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ItemFilter extends MinepacksListener implements at.pcgamingfreaks.Minepacks.Bukkit.API.ItemFilter
{
	public final Message messageNotAllowedInBackpack;
	public final ItemNameResolver itemNameResolver;
	private final boolean whitelistMode;
	private final Collection<MinecraftMaterial> filteredMaterials = new HashSet<>();
	private final Set<String> filteredNames;

	public ItemFilter(final Minepacks plugin)
	{
		super(plugin);

		whitelistMode = plugin.getConfiguration().isItemFilterModeWhitelist();
		if(plugin.getConfiguration().isShulkerboxesPreventInBackpackEnabled() && !whitelistMode)
		{
			for(Material mat : DisableShulkerboxes.SHULKER_BOX_MATERIALS)
			{
				filteredMaterials.add(new MinecraftMaterial(mat, (short) -1));
			}
		}
		filteredMaterials.addAll(plugin.getConfiguration().getItemFilterMaterials());
		filteredNames = plugin.getConfiguration().getItemFilterNames();

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

	@Override
	public boolean isItemBlocked(final @Nullable ItemStack item)
	{
		if(item == null) return false;
		if(filteredMaterials.contains(new MinecraftMaterial(item))) return !whitelistMode;
		if(item.hasItemMeta())
		{
			ItemMeta meta = item.getItemMeta();
			assert meta != null; //TODO remove after testing
			if(meta.hasDisplayName() && filteredNames.contains(meta.getDisplayName())) return !whitelistMode;
		}
		return whitelistMode;
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
	public void onItemClick(InventoryClickEvent event)
	{
		if(!(event.getWhoClicked() instanceof Player)) return;
		if(event.getInventory().getHolder() instanceof Backpack)
		{
			Player player = (Player) event.getWhoClicked();
			if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && isItemBlocked(event.getCurrentItem()))
			{
				event.setCancelled(true);
				messageNotAllowedInBackpack.send(event.getView().getPlayer(), itemNameResolver.getName(event.getCurrentItem()));
			}
			else if((event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP) && event.getHotbarButton() != -1)
			{
				ItemStack item = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
				if(item != null && isItemBlocked(item))
				{
					event.setCancelled(true);
					messageNotAllowedInBackpack.send(event.getView().getPlayer(), itemNameResolver.getName(item));
				}
			}
			else if(!player.getInventory().equals(event.getClickedInventory()) && isItemBlocked(event.getCursor()))
			{
				messageNotAllowedInBackpack.send(event.getView().getPlayer(), itemNameResolver.getName(event.getCursor()));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemDrag(InventoryDragEvent event)
	{
		if(event.getInventory().getHolder() instanceof Backpack && (isItemBlocked(event.getOldCursor()) || isItemBlocked(event.getCursor())) && event.getRawSlots().containsAll(event.getInventorySlots()))
		{
			messageNotAllowedInBackpack.send(event.getView().getPlayer(), itemNameResolver.getName(event.getOldCursor()));
			event.setCancelled(true);
		}
	}
}