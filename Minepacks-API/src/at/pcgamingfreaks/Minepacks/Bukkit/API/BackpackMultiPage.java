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

package at.pcgamingfreaks.Minepacks.Bukkit.API;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public interface BackpackMultiPage extends Backpack
{
	/**
	 * Gets the backpack page.
	 *
	 * @param page The page that should be retrieved.
	 * @return The backpack page.
	 * @throws IndexOutOfBoundsException if page < 0 or page >= getPageCount()
	 */
	@NotNull Backpack getPage(int page) throws IndexOutOfBoundsException;

	/**
	 * Gets all the pages of the backpack.
	 *
	 * @return Collection of backpack pages.
	 */
	Collection<? extends Backpack> getPages();

	/**
	 * Gets the amount of pages this multi page backpack has.
	 *
	 * @return The amount of pages owned by this multi page backpack.
	 */
	int getPageCount();



	/**
	 * Let a given player open this backpack.
	 *
	 * @param player   The player who opens the backpack.
	 * @param editable Defines if the player who has opened the backpack can change the items inside.
	 * @param page     The page that should be opened.
	 */
	void open(@NotNull Player player, boolean editable, int page);

	/**
	 * @param stack The item stack that should be added to the backpack.
	 * @param page The page on which the item should be added.
	 * @return null if the entire item stack has been added. An item stack containing the items that did not fit into the backpack.
	 */
	@Nullable ItemStack addItem(ItemStack stack, int page);

	/**
	 * @param itemStacks The item that should be added to the backpack.
	 * @param page The page on which the item should be added.
	 * @return A HashMap containing items that didn't fit. The key is the number of the added item.
	 */
	@NotNull Map<Integer, ItemStack> addItems(int page, ItemStack... itemStacks);
}