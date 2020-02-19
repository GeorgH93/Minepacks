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

import at.pcgamingfreaks.Bukkit.HeadUtils;
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ItemShortcut implements Listener
{
	private static final UUID MINEPACKS_UUID = UUID.nameUUIDFromBytes("Minepacks".getBytes());
	private final Minepacks plugin;
	private final String itemName, value;
	private final Message messageDoNotRemoveItem;

	public ItemShortcut(Minepacks plugin)
	{
		this.plugin = plugin;
		itemName = ChatColor.translateAlternateColorCodes('&', plugin.getConfiguration().getItemShortcutItemName());
		value = plugin.getConfiguration().getItemShortcutHeadValue();
		messageDoNotRemoveItem = plugin.getLanguage().getMessage("Ingame.DontRemoveShortcut");
	}

	private boolean isItemShortcut(@Nullable ItemStack stack)
	{
		//noinspection ConstantConditions
		return stack != null && stack.getType() == HeadUtils.HEAD_MATERIAL && stack.hasItemMeta() && stack.getItemMeta().getDisplayName().equals(itemName);
	}

	private void addItem(Player player)
	{
		if(player.hasPermission(Permissions.USE))
		{
			boolean empty = false, item = false;
			for(ItemStack itemStack : player.getInventory())
			{
				if(itemStack == null || itemStack.getType() == Material.AIR) empty = true;
				else if(isItemShortcut(itemStack))
				{
					item = true;
					break;
				}
			}
			if(!item && empty) player.getInventory().addItem(HeadUtils.fromBase64(value, itemName, MINEPACKS_UUID));
		}
	}

	//region Add backpack item
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent event)
	{
		if(plugin.isDisabled(event.getPlayer()) != WorldBlacklistMode.None) return;
		addItem(event.getPlayer());
	}

	@EventHandler
	public void onSpawn(PlayerRespawnEvent event)
	{
		if(plugin.isDisabled(event.getPlayer()) != WorldBlacklistMode.None) return;
		addItem(event.getPlayer());
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event)
	{
		if(plugin.isDisabled(event.getPlayer()) != WorldBlacklistMode.None) return;
		addItem(event.getPlayer());
	}
	//endregion

	//region Prevent placing of backpack item
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemInteract(PlayerInteractEvent event)
	{
		if ((event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) return;
		if(isItemShortcut(event.getItem()))
		{
			event.getPlayer().performCommand("backpack open");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArmorStandManipulation(PlayerArmorStandManipulateEvent event)
	{
		if(isItemShortcut(event.getPlayerItem()))
		{
			event.getPlayer().performCommand("backpack open");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemFrameInteract(PlayerInteractEntityEvent event)
	{
		Player player = event.getPlayer();
		ItemStack item;
		if(MCVersion.isDualWieldingMC())
		{
			item = (event.getHand() == EquipmentSlot.HAND) ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
		}
		else
		{
			item = player.getItemInHand();
		}
		if(isItemShortcut(item))
		{
			event.getPlayer().performCommand("backpack open");
			event.setCancelled(true);
		}
	}
	//endregion

	//region Handle inventory actions
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemClick(InventoryClickEvent event)
	{
		if(event.getWhoClicked() instanceof Player)
		{
			final Player player = (Player) event.getWhoClicked();
			if(isItemShortcut(event.getCurrentItem()))
			{
				if(event.getAction() == InventoryAction.SWAP_WITH_CURSOR)
				{
					if(plugin.isDisabled(player) != WorldBlacklistMode.None || !player.hasPermission(Permissions.USE) || !plugin.isPlayerGameModeAllowed(player)) return;
					Backpack backpack = plugin.getBackpackCachedOnly(player);
					if(backpack != null)
					{
						//TODO right click should place only one
						final ItemStack stack = event.getCursor();
						if(plugin.getItemFilter() == null || !plugin.getItemFilter().isItemBlocked(stack))
						{
							Map<Integer, ItemStack> full = backpack.getInventory().addItem(stack);
							stack.setAmount((full.isEmpty()) ? 0 : full.get(0).getAmount());
							event.setCancelled(true);
						}
						else
						{
							plugin.getItemFilter().messageNotAllowedInBackpack.send(player, plugin.getItemFilter().itemNameResolver.getName(stack));
						}
					}
				}
				else if(event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT)
				{
					player.performCommand("backpack open");
					event.setCancelled(true);
				}
				else if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
				{
					event.setCancelled(true);
					messageDoNotRemoveItem.send(player);
				}
			}
			else if((event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP) && event.getHotbarButton() != -1)
			{
				ItemStack item = player.getInventory().getItem(event.getHotbarButton());
				if(isItemShortcut(item))
				{
					event.setCancelled(true);
					messageDoNotRemoveItem.send(player);
				}
			}
			else if(isItemShortcut(event.getCursor()) && !player.getInventory().equals(event.getClickedInventory()))
			{
				event.setCancelled(true);
				messageDoNotRemoveItem.send(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemDrag(InventoryDragEvent event)
	{
		if(!event.getInventory().equals(event.getWhoClicked().getInventory()) && event.getRawSlots().containsAll(event.getInventorySlots()))
		{
			if(isItemShortcut(event.getCursor()) || isItemShortcut(event.getOldCursor()))
			{
				event.setCancelled(true);
				messageDoNotRemoveItem.send(event.getWhoClicked());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onDropItem(PlayerDropItemEvent event)
	{
		if(isItemShortcut(event.getItemDrop().getItemStack()))
		{
			event.setCancelled(true);
			messageDoNotRemoveItem.send(event.getPlayer());
		}
	}
	//endregion

	/**
	 * Removes the backpack item form the drops on death
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onDeath(PlayerDeathEvent event)
	{
		Iterator<ItemStack> itemStackIterator = event.getDrops().iterator();
		while(itemStackIterator.hasNext())
		{
			if(isItemShortcut(itemStackIterator.next()))
			{
				itemStackIterator.remove();
				break;
			}
		}
	}
}