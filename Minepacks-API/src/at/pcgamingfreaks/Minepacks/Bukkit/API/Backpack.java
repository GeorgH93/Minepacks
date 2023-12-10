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

package at.pcgamingfreaks.Minepacks.Bukkit.API;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public interface Backpack extends InventoryHolder
{
	/**
	 * Gets the owner of the backpack.
	 *
	 * @return The owner of the backpack;
	 * @deprecated Use the {@link Backpack#getOwnerId()} function instead.
	 */
	@Deprecated
	@NotNull OfflinePlayer getOwner();

	/**
	 * Gets the id of the player owning the backpack.
	 *
	 * @return The uuid of the owning player.
	 */
	UUID getOwnerId();

	@Nullable Player getOwnerPlayer();

	/**
	 * Let a given player open this backpack.
	 *
	 * @param player   The player who opens the backpack.
	 * @param editable Defines if the player who has opened the backpack can change the items inside.
	 */
	void open(@NotNull Player player, boolean editable);

	/**
	 * Let a given player open this backpack.
	 *
	 * @param player   The player who opens the backpack.
	 * @param editable Defines if the player who has opened the backpack can change the items inside.
	 * @param title    Custom title for the backpack (will be shown to the player who opened the backpack.
	 */
	void open(@NotNull Player player, boolean editable, @Nullable String title);

	/**
	 * Checks if the backpack is currently opened by a player.
	 *
	 * @return True if the backpack is open, false if not.
	 */
	boolean isOpen();

	/**
	 * Checks if a player can change the content of the backpack.
	 *
	 * @param player The player to be checked.
	 * @return True if he can change the content, false if not.
	 */
	boolean canEdit(@NotNull Player player);

	/**
	 * Gets the size of the backpack.
	 *
	 * @return The size of the backpack.
	 */
	int getSize();

	/**
	 * Checks if the backpack has changed since it was last saved.
	 *
	 * @return True if it has been changed, false if not.
	 */
	boolean hasChanged();

	/**
	 * Marks that the content of the backpack a changed. It will be saved when the next player closes the backpack or before it gets removed from the cache.
	 */
	void setChanged();

	/**
	 * Forces the backpack to be saved
	 */
	void save();

	/**
	 * Removes all items from the backpack.
	 */
	void clear();

	/**
	 * Drops the content of the backpack to the ground on a given location.
	 *
	 * @param location The location the content of the backpack should be dropped to.
	 */
	void drop(Location location);

	/**
	 * @param stack The item stack that should be added to the backpack.
	 * @return null if the entire item stack has been added. An item stack containing the items that did not fit into the backpack.
	 */
	default @Nullable ItemStack addItem(ItemStack stack)
	{
		Map<Integer, ItemStack> left = addItems(stack);
		if(left.isEmpty()) return null;
		return left.get(0);
	}

	/**
	 * @param itemStacks The item that should be added to the backpack.
	 * @return A HashMap containing items that didn't fit. The key is the number of the added item
	 */
	default @NotNull Map<Integer, ItemStack> addItems(ItemStack... itemStacks)
	{
		setChanged();
		return getInventory().addItem(itemStacks);
	}

	static boolean isBackpack(@Nullable Inventory inventory)
	{
		return inventory instanceof Backpack;
	}
}