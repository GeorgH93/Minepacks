/*
 *   Copyright (C) 2016 GeorgH93
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

import at.pcgamingfreaks.Version;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MinepacksPlugin
{
	/**
	 * Let a given player open the backpack of an other player.
	 *
	 * @param opener   The player who opens the backpack.
	 * @param owner    The owner of the backpack that should be opened.
	 * @param editable Defines if the player who has opened the backpack can change the items inside.
	 */
	void openBackpack(@NotNull final Player opener, @NotNull final OfflinePlayer owner, final boolean editable);

	/**
	 * Let a given player open a given {@link at.pcgamingfreaks.Minepacks.Bukkit.Backpack}.
	 *
	 * @param opener   The player who opens the backpack.
	 * @param backpack The backpack to be opened. null will result in an error message for the player.
	 * @param editable Defines if the player who has opened the backpack can change the items inside.
	 */
	void openBackpack(@NotNull final Player opener, @Nullable final at.pcgamingfreaks.Minepacks.Bukkit.Backpack backpack, boolean editable);

	/**
	 * Retrieves the backpack for a given player.
	 * This method runs sync! If the requested backpack is not in the cache it will block you server! Use with extreme care!
	 *
	 * @param owner The player who's backpack should be retrieved.
	 * @return The backpack of the given player.
	 */
	@Nullable at.pcgamingfreaks.Minepacks.Bukkit.Backpack getBackpack(@NotNull final OfflinePlayer owner);

	/**
	 * Retrieves the backpack for a given player.
	 * This method only returns a backpack if it is in the cache.
	 *
	 * @param owner The player who's backpack should be retrieved.
	 * @return The backpack of the given player. null if the backpack is in the cache.
	 */
	@Nullable at.pcgamingfreaks.Minepacks.Bukkit.Backpack getBackpackCachedOnly(@NotNull final OfflinePlayer owner);

	/**
	 * Retrieves the backpack for a given player.
	 * This method runs async! The result will be delivered with a callback.
	 *
	 * @param owner The player who's backpack should be retrieved.
	 * @param callback The callback delivering the result of the request.
	 */
	void getBackpack(@NotNull final OfflinePlayer owner, @NotNull final Callback<at.pcgamingfreaks.Minepacks.Bukkit.Backpack> callback);

	/**
	 * Gets the currently running {@link Version} of the plugin.
	 *
	 * @return The currently running version of the plugin.
	 */
	Version getVersion();
}