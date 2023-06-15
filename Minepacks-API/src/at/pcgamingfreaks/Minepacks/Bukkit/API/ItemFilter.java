/*
 *   Copyright (C) 2019 GeorgH93
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

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface ItemFilter
{
	/**
	 * @param item The item that should be checked.
	 * @return True if the item is not allowed. False if the item is allowed.
	 */
	@Contract("null->false")
	boolean isItemBlocked(@Nullable ItemStack item);

	/**
	 * @param player The player that should receive the message that the item is not allowed.
	 * @param itemStack The item that is not allowed. Will be used for the name.
	 */
	void sendNotAllowedMessage(@NotNull Player player, @NotNull ItemStack itemStack);

	/**
	 * @param player The player that should receive the message if the item is not allowed.
	 * @param itemStack The item that should be checked.
	 * @return True if the item is not allowed. False if the item is allowed.
	 */
	default boolean checkIsBlockedAndShowMessage(@NotNull Player player, @Nullable ItemStack itemStack)
	{
		if(isItemBlocked(itemStack))
		{
			sendNotAllowedMessage(player, itemStack);
			return true;
		}
		return false;
	}
}