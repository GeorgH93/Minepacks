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

package at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI;

import at.pcgamingfreaks.Bukkit.Message.Message;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Backpack extends at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack
{
	@Override
	@NotNull MinepacksPlayer getOwner();

	/**
	 * Let a given player open this backpack.
	 *
	 * @param player   The player who opens the backpack.
	 * @param editable Defines if the player who has opened the backpack can change the items inside.
	 * @param title    Custom title for the backpack (will be shown to the player who opened the backpack.
	 */
	void open(@NotNull Player player, boolean editable, @Nullable Message title);
}