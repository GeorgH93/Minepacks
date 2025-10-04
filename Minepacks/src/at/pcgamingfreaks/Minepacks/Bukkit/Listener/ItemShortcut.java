/*
 *   Copyright (C) 2025 GeorgH93
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

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Util.HeadUtils;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Events.InventoryClearedEvent;
import at.pcgamingfreaks.Minepacks.Bukkit.API.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class ItemShortcut extends MinepacksListener
{
	private static final UUID MINEPACKS_UUID = UUID.nameUUIDFromBytes("Minepacks".getBytes());
	private final String itemName, itemNameNoReset, value, openCommand;
	private final Message messageDoNotRemoveItem;
	private final boolean improveDeathChestCompatibility, blockAsHat, allowRightClickOnContainers, blockItemFromMoving;
	private final int preferredSlotId;
	private final Set<Material> containerMaterials = new HashSet<>();

	public ItemShortcut(final @NotNull Minepacks plugin)
	{
		super(plugin);
		itemName = ChatColor.translateAlternateColorCodes('&', plugin.getConfiguration().getItemShortcutItemName());
		itemNameNoReset = itemName.replace(ChatColor.RESET.toString(), "");
		value = plugin.getConfiguration().getItemShortcutHeadValue();
		improveDeathChestCompatibility = plugin.getConfiguration().isItemShortcutImproveDeathChestCompatibilityEnabled();
		blockAsHat = plugin.getConfiguration().isItemShortcutBlockAsHatEnabled();
		allowRightClickOnContainers = plugin.getConfiguration().isItemShortcutRightClickOnContainerAllowed();
		preferredSlotId = plugin.getConfiguration().getItemShortcutPreferredSlotId();
		blockItemFromMoving = plugin.getConfiguration().getItemShortcutBlockItemFromMoving();
		openCommand = plugin.getLanguage().getCommandAliases("Backpack", "backpack")[0] + ' ' + plugin.getLanguage().getCommandAliases("Open", "open")[0];
		messageDoNotRemoveItem = plugin.getLanguage().getMessage("Ingame.DontRemoveShortcut");

		if(allowRightClickOnContainers)
		{
			containerMaterials.add(Material.CHEST);
			containerMaterials.add(Material.TRAPPED_CHEST);
			containerMaterials.add(Material.ENDER_CHEST);
			containerMaterials.add(Material.CRAFTING_TABLE);
			containerMaterials.add(Material.FURNACE);
			containerMaterials.add(Material.BLAST_FURNACE);
			containerMaterials.add(Material.DISPENSER);
			containerMaterials.add(Material.DROPPER);
			containerMaterials.add(Material.HOPPER);
			if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11)) containerMaterials.addAll(DisableShulkerboxes.SHULKER_BOX_MATERIALS);
		}
	}

	public boolean isItemShortcut(final @Nullable ItemStack stack)
	{
		if(stack == null || stack.getType() != HeadUtils.HEAD_MATERIAL || !stack.hasItemMeta()) return false;
		String itemDisplayName = stack.getItemMeta().getDisplayName();
		return itemDisplayName != null && itemNameNoReset.equals(itemDisplayName.replace(ChatColor.RESET.toString(), ""));
	}

	public void addItem(Player player)
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
					if(itemStack.getAmount() > 1) itemStack.setAmount(1);
					break;
				}
			}
			if(!item && empty)
			{
				if(preferredSlotId >= 0 && preferredSlotId < 36)
				{
					ItemStack stack = player.getInventory().getItem(preferredSlotId);
					if(stack == null || stack.getType() == Material.AIR)
					{
						player.getInventory().setItem(preferredSlotId, HeadUtils.fromBase64(value, itemName, MINEPACKS_UUID));
						return;
					}
				}
				player.getInventory().addItem(HeadUtils.fromBase64(value, itemName, MINEPACKS_UUID));
			}
		}
		else
		{
			for(ItemStack itemStack : player.getInventory())
			{
				if(isItemShortcut(itemStack))
				{
					player.getInventory().remove(itemStack);
					return;
				}
			}
		}
	}

	private void removeItem(Player player)
	{
		for(ItemStack itemStack : player.getInventory())
		{
			if(isItemShortcut(itemStack))
			{
				itemStack.setAmount(0);
				return;
			}
		}
	}

	//region Add backpack item
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent event)
	{
		if(plugin.isDisabled(event.getPlayer()) != WorldBlacklistMode.None) return;
		Minepacks.getScheduler().runLater(() -> Minepacks.getScheduler().runAtEntity(event.getPlayer(), task -> addItem(event.getPlayer())), 2L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onSpawn(PlayerRespawnEvent event)
	{
		if(plugin.isDisabled(event.getPlayer()) != WorldBlacklistMode.None) return;
		Minepacks.getScheduler().runAtEntityLater(event.getPlayer(), task -> addItem(event.getPlayer()), 2L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldChange(final PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();

		Minepacks.getScheduler().runAtEntityLater(player, () -> {
				if(!player.isOnline()) return;
				if(player.hasPermission(Permissions.USE) && plugin.isDisabled(player) == WorldBlacklistMode.None)
					addItem(player);
				else
					removeItem(player);
			}, 2L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClear(InventoryClearedEvent event)
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
			if(allowRightClickOnContainers && event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				//noinspection ConstantConditions
				if(containerMaterials.contains(event.getClickedBlock().getType())) return;
			}
			event.getPlayer().performCommand(openCommand);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArmorStandManipulation(PlayerArmorStandManipulateEvent event)
	{
		if(isItemShortcut(event.getPlayerItem()))
		{
			event.getPlayer().performCommand(openCommand);
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
			event.getPlayer().performCommand(openCommand);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(isItemShortcut(event.getItemInHand()))
		{
			event.getPlayer().performCommand(openCommand);
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
							ItemStack full = backpack.addItem(stack);
							stack.setAmount((full == null) ? 0 : full.getAmount());
							event.setCursor(full);
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
					Minepacks.getScheduler().runAtEntity(player, task -> player.performCommand(openCommand));
					event.setCancelled(true);
				}
				else if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
				{
					event.setCancelled(true);
					messageDoNotRemoveItem.send(player);
				}
				if(blockItemFromMoving) event.setCancelled(true);
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
			else if((event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP) && event.getClick().name().equals("SWAP_OFFHAND"))
			{
				if(isItemShortcut(player.getInventory().getItemInOffHand()))
				{
					event.setCancelled(true);
					messageDoNotRemoveItem.send(player);
				}
			}
			else if(isItemShortcut(event.getCursor()))
			{
				if(!player.getInventory().equals(event.getClickedInventory()))
				{
					event.setCancelled(true);
					messageDoNotRemoveItem.send(player);
				}
				else if(event.getSlotType() == InventoryType.SlotType.ARMOR && blockAsHat)
				{
					event.setCancelled(true);
				}
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
		//region prevent drop
		Iterator<ItemStack> itemStackIterator = event.getDrops().iterator();
		while(itemStackIterator.hasNext())
		{
			if(isItemShortcut(itemStackIterator.next()))
			{
				itemStackIterator.remove();
				break;
			}
		}
		//endregion
		if(improveDeathChestCompatibility)
		{ // improveDeathChestCompatibility
			for(ItemStack itemStack : event.getEntity().getInventory())
			{
				if(isItemShortcut(itemStack))
				{
					itemStack.setAmount(0);
					itemStack.setType(Material.AIR);
				}
			}
		}
	}
}