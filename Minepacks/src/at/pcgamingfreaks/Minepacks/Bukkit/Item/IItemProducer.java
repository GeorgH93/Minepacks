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

package at.pcgamingfreaks.Minepacks.Bukkit.Item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IItemProducer
{
	/**
	 * Produces the item according to the item config. But with a different amount.
	 *
	 * @param config The config that defines the item.
	 * @return The produced item stack.
	 */
	default @NotNull ItemStack make(final @NotNull ItemConfig config)
	{
		return make(config, config.getAmount());
	}

	/**
	 * Produces the item according to the item config. But with a different amount.
	 *
	 * @param config The config that defines the item.
	 * @param amount How many items should be produced.
	 * @return The produced item stack.
	 */
	@NotNull ItemStack make(final @NotNull ItemConfig config, final int amount);
}