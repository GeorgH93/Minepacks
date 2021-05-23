/*
 *   Copyright (C) 2021 GeorgH93
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
import at.pcgamingfreaks.Bukkit.Util.InventoryUtils;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Events.InventoryClearedEvent;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Events.MinepacksPlayerJoinEvent;
import at.pcgamingfreaks.Minepacks.Bukkit.API.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.MinepacksPlayerData;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ItemShortcut extends MinepacksListener
{
	private final String openCommand;
	private final Message messageDoNotRemoveItem;
	private final boolean improveDeathChestCompatibility, blockAsHat, allowRightClickOnContainers, blockItemFromMoving;
	private final int preferredSlotId;
	private final Set<Material> containerMaterials = new HashSet<>();
	private final Sound dragAndDropSound;

	public ItemShortcut(final @NotNull Minepacks plugin)
	{
		super(plugin);
		improveDeathChestCompatibility = plugin.getConfiguration().isItemShortcutImproveDeathChestCompatibilityEnabled();
		blockAsHat = plugin.getConfiguration().isItemShortcutBlockAsHatEnabled();
		allowRightClickOnContainers = plugin.getConfiguration().isItemShortcutRightClickOnContainerAllowed();
		preferredSlotId = plugin.getConfiguration().getItemShortcutPreferredSlotId();
		blockItemFromMoving = plugin.getConfiguration().getItemShortcutBlockItemFromMoving();
		dragAndDropSound = plugin.getConfiguration().getDragAndDropSound();
		openCommand = plugin.getLanguage().getCommandAliases("Backpack", "backpack")[0] + ' ' + plugin.getLanguage().getCommandAliases("Open", "open")[0];
		messageDoNotRemoveItem = plugin.getLanguage().getMessage("Ingame.DontRemoveShortcut");

		if(!plugin.getBackpacksConfig().isAllowItemShortcut())
		{
			plugin.getLogger().severe("No shortcut item defined in the backpacks.yml file! Item shortcut will be disabled!");
			throw new IllegalArgumentException("The item is not defined.");
		}

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

	public boolean isItemShortcut(final @NotNull Player player, final @Nullable ItemStack stack)
	{
		MinepacksPlayerData playerData = plugin.getMinepacksPlayer(player);
		ItemConfig itemConfig = playerData.getBackpackStyle();
		if(itemConfig == null) return false;
		return isItemShortcut(stack, itemConfig);
	}

	public boolean isItemShortcut(final @Nullable ItemStack stack, final @NotNull ItemConfig itemConfig)
	{
		if(stack == null || !stack.hasItemMeta()) return false;
		ItemMeta meta = stack.getItemMeta();
		if(meta == null) return false;
		//TODO check item metadata
		return stack.getType() == itemConfig.getMaterial() && itemConfig.getDisplayName().equals(meta.getDisplayName());
	}

	public void addItem(final @NotNull Player player)
	{
		if(player.hasPermission(Permissions.USE))
		{
			MinepacksPlayerData minepacksPlayer = plugin.getMinepacksPlayer(player);
			addItem(minepacksPlayer, player);
		}
	}

	public void addItem(final @NotNull MinepacksPlayerData minepacksPlayer, final @NotNull Player player)
	{
		ItemConfig itemConfig = minepacksPlayer.getBackpackStyle();
		if(itemConfig == null) return; // Null = disabled
		boolean empty = false; // Stores if there is an empty inventory slot available
		for(ItemStack itemStack : player.getInventory())
		{
			if(itemStack == null || itemStack.getType() == Material.AIR) empty = true; // Empty inventory slot found
			else if(isItemShortcut(itemStack, itemConfig))
			{ //TODO update item
				if(itemStack.getAmount() > 1) itemStack.setAmount(1);
				return;
			}
		}
		if(empty) // There is an empty inventory slot available that the item can be added to
		{
			if(preferredSlotId >= 0 && preferredSlotId < 36)
			{
				ItemStack stack = player.getInventory().getItem(preferredSlotId);
				if(stack == null || stack.getType() == Material.AIR)
				{
					player.getInventory().setItem(preferredSlotId, itemConfig.make(1));
					return;
				}
			}
			player.getInventory().addItem(itemConfig.make(1));
		}
	}

	//region Add backpack item
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(MinepacksPlayerJoinEvent event)
	{
		if(plugin.isDisabled(event.getBukkitPlayer()) != WorldBlacklistMode.None || !event.getBukkitPlayer().hasPermission(Permissions.USE)) return;
		Bukkit.getScheduler().runTaskLater(plugin, () -> addItem((MinepacksPlayerData) event.getPlayer(), event.getBukkitPlayer()), 2L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onSpawn(PlayerRespawnEvent event)
	{
		if(plugin.isDisabled(event.getPlayer()) != WorldBlacklistMode.None) return;
		Bukkit.getScheduler().runTaskLater(plugin, () -> addItem(event.getPlayer()), 2L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldChange(PlayerChangedWorldEvent event)
	{
		if(plugin.isDisabled(event.getPlayer()) != WorldBlacklistMode.None) return;
		Bukkit.getScheduler().runTaskLater(plugin, () -> addItem(event.getPlayer()), 2L);
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
		if(isItemShortcut(event.getPlayer(), event.getItem()))
		{
			if(allowRightClickOnContainers && event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				return; //TODO testing
				//noinspection ConstantConditions
				//if(containerMaterials.contains(event.getClickedBlock().getType())) return;
			}
			event.getPlayer().performCommand(openCommand);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onArmorStandManipulation(PlayerArmorStandManipulateEvent event)
	{
		if(isItemShortcut(event.getPlayer(), event.getPlayerItem()))
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
			item = InventoryUtils.getItemInHand(player);
		}
		if(isItemShortcut(event.getPlayer(), item))
		{
			event.getPlayer().performCommand(openCommand);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(isItemShortcut(event.getPlayer(), event.getItemInHand()))
		{
			event.getPlayer().performCommand(openCommand);
			event.setCancelled(true);
		}
	}
	//endregion

	//region Handle inventory actions
	private void handleDragAndDrop(final @NotNull InventoryClickEvent event)
	{
		final Player player = (Player) event.getWhoClicked();
		if(plugin.isDisabled(player) != WorldBlacklistMode.None || !player.hasPermission(Permissions.USE) || !plugin.isPlayerGameModeAllowed(player)) return;
		Backpack backpack = plugin.getBackpackLoadedOnly(player);
		if(backpack != null)
		{
			final ItemStack stack = event.getCursor();
			if(stack != null && stack.getAmount() > 0)
			{
				if(plugin.getItemFilter() == null || !plugin.getItemFilter().isItemBlocked(stack))
				{
					if(event.getClick() == ClickType.RIGHT)
					{ // right click should place only one
						ItemStack place = stack.clone();
						place.setAmount(1);
						ItemStack full = backpack.addItem(place);
						if(full == null)
						{
							stack.setAmount(stack.getAmount() - 1);
							event.setCursor(stack);
							if(dragAndDropSound != null) player.playSound(player.getEyeLocation(), dragAndDropSound, 1, 0);
						}
					}
					else
					{
						ItemStack full = backpack.addItem(stack);
						if(full == null)
						{
							stack.setAmount(0);
							if(dragAndDropSound != null) player.playSound(player.getEyeLocation(), dragAndDropSound, 1, 0);
						}
						else
						{
							if(dragAndDropSound != null && stack.getAmount() != full.getAmount()) player.playSound(player.getEyeLocation(), dragAndDropSound, 1, 0);
							stack.setAmount(full.getAmount());
						}
						event.setCursor(stack);
					}
					event.setCancelled(true);
				}
				else
				{
					plugin.getItemFilter().messageNotAllowedInBackpack.send(player, plugin.getItemFilter().itemNameResolver.getName(stack));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemClick(InventoryClickEvent event)
	{
		if(event.getWhoClicked() instanceof Player)
		{
			final Player player = (Player) event.getWhoClicked();
			MinepacksPlayerData minepacksPlayer = plugin.getMinepacksPlayer(player);
			ItemConfig itemConfig = minepacksPlayer.getBackpackStyle();
			if(itemConfig == null) return;
			if(isItemShortcut(event.getCurrentItem(), itemConfig))
			{
				if(event.getAction() == InventoryAction.SWAP_WITH_CURSOR)
				{
					handleDragAndDrop(event);
				}
				else if(event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT)
				{
					player.performCommand(openCommand);
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
				if(isItemShortcut(item, itemConfig))
				{
					event.setCancelled(true);
					messageDoNotRemoveItem.send(player);
				}
			}
			else if((event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP) && event.getClick().name().equals("SWAP_OFFHAND"))
			{
				if(isItemShortcut(player.getInventory().getItemInOffHand(), itemConfig))
				{
					event.setCancelled(true);
					messageDoNotRemoveItem.send(player);
				}
			}
			else if(isItemShortcut(event.getCursor(), itemConfig))
			{
				if(!player.getInventory().equals(InventoryUtils.getClickedInventory(event)))
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
			MinepacksPlayerData minepacksPlayer = plugin.getMinepacksPlayer((Player) event.getWhoClicked());
			ItemConfig itemConfig = minepacksPlayer.getBackpackStyle();
			if(itemConfig == null) return;
			if(isItemShortcut(event.getCursor(), itemConfig) || isItemShortcut(event.getOldCursor(), itemConfig))
			{
				event.setCancelled(true);
				messageDoNotRemoveItem.send(event.getWhoClicked());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onDropItem(PlayerDropItemEvent event)
	{
		if(isItemShortcut(event.getPlayer(), event.getItemDrop().getItemStack()))
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
		MinepacksPlayerData minepacksPlayer = plugin.getMinepacksPlayer(event.getEntity());
		ItemConfig itemConfig = minepacksPlayer.getBackpackStyle();
		if(itemConfig == null) return;
		//region prevent drop
		Iterator<ItemStack> itemStackIterator = event.getDrops().iterator();
		while(itemStackIterator.hasNext())
		{
			if(isItemShortcut(itemStackIterator.next(), itemConfig))
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
				if(isItemShortcut(itemStack, itemConfig))
				{
					itemStack.setAmount(0);
					itemStack.setType(Material.AIR);
				}
			}
		}
	}
}