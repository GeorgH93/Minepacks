/*
 *   Copyright (C) 2016, 2017 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.API;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public interface Backpack extends InventoryHolder
{
	/**
	 * Gets the owner of the backpack.
	 *
	 * @return The owner of the backpack;
	 */
	@NotNull OfflinePlayer getOwner();

	/**
	 * Let a given player open this backpack.
	 *
	 * @param player   The player who opens the backpack.
	 * @param editable Defines if the player who has opened the backpack can change the items inside.
	 */
	void open(@NotNull Player player, boolean editable);

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
}