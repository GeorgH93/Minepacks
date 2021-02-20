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

package at.pcgamingfreaks.Minepacks.Bukkit.API;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player in the Minepacks plugin.
 */
public interface MinepacksPlayer
{
	/**
	 * Gets the name of the player.
	 *
	 * @return The name of the player.
	 */
	@NotNull String getName();

	/**
	 * Gets the {@link UUID} of the player.
	 *
	 * @return The {@link UUID} of the player.
	 */
	@NotNull UUID getUUID();

	/**
	 * Gets the {@link OfflinePlayer} object of the player.
	 *
	 * @return The {@link OfflinePlayer} object of the player.
	 */
	@NotNull OfflinePlayer getPlayer();

	/**
	 * Gets the {@link Player} object of the player.
	 *
	 * @return The {@link Player} object of the player. Null if the player is offline.
	 */
	@Nullable Player getPlayerOnline();

	/**
	 * Gets the display name of the player.
	 * If the player is offline Gray + Name will be used instead.
	 *
	 * @return The display name of the player.
	 */
	@NotNull String getDisplayName();

	/**
	 * Checks if the player has a given permission.
	 *
	 * @param permission The permission that should be checked.
	 * @return True if the player does not have the permission. False if the player does not have the permission or is offline.
	 */
	boolean hasPermission(final @NotNull String permission);

	/**
	 * Checks if the player is online.
	 *
	 * @return True if the player is online. False if not.
	 */
	boolean isOnline();

	/**
	 * Sets the style of the players backpack shortcut.
	 *
	 * @param style The style of the backpack. "default" to use the configured default style. "none" to disable the backpack shortcut for the player.
	 */
	void setBackpackStyle(final @NotNull String style);

	/**
	 * Gets the backpack shortcut item for the player.
	 *
	 * @return The backpack shortcut item for the player. null if the shortcut item is disabled for the player.
	 */
	@Nullable ItemStack getBackpackItem();

	/**
	 * Gets the {@link Backpack} of the player.
	 *
	 * @return The players {@link Backpack}.
	 */
	@Nullable Backpack getBackpack();


	/**
	 * Checks whether the player has been loaded.
	 * While the player is not loaded data might not be available or valid.
	 *
	 * @return True if the player has been loaded. False if not.
	 */
	boolean isLoaded();

	void notifyOnLoad(Callback<MinepacksPlayer> callback);

	/**
	 * Checks whether the backpack of the player has been loaded.
	 * While the backpack is not loaded the backpack won't be available.
	 * Loading the backpack will be delayed when using a BungeeCord setup.
	 *
	 * @return True if the players backpack has been loaded. False if not.
	 */
	boolean isBackpackLoaded();

	void getBackpack(Callback<Backpack> callback);

	void setCooldown(long cooldown);

	long getCooldown();
}