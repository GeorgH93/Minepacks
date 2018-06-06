/*
 *   Copyright (C) 2016-2018 GeorgH93
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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MinepacksPlugin
{
	/**
	 * Gets the instance of the minepacks plugin.
	 * WARNING use this function at your own risk! If the plugin is not installed the MinepacksPlugin class will be unknown!
	 *
	 * @return The instance of the minepacks plugin.
	 */
	static @Nullable MinepacksPlugin getInstance()
	{
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Minepacks");
		return (plugin instanceof MinepacksPlugin && plugin.isEnabled()) ? (MinepacksPlugin) plugin : null;
	}

	/**
	 * Gets the currently running {@link Version} of the plugin.
	 * Version 0.0 if plugin is not loaded or enabled.
	 *
	 * @return The currently running version of the plugin.
	 */
	static @NotNull Version getVersion()
	{
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Minepacks");
		if(plugin instanceof MinepacksPlugin && plugin.isEnabled())
		{
			return new Version(plugin.getDescription().getVersion());
		}
		return new Version("0.0");
	}

	/**
	 * Let a given player open the backpack of an other player.
	 *
	 * @param opener   The player who opens the backpack.
	 * @param owner    The owner of the backpack that should be opened.
	 * @param editable Defines if the player who has opened the backpack can change the items inside.
	 */
	void openBackpack(@NotNull final Player opener, @NotNull final OfflinePlayer owner, final boolean editable);

	/**
	 * Let a given player open a given {@link Backpack}.
	 *
	 * @param opener   The player who opens the backpack.
	 * @param backpack The backpack to be opened. null will result in an error message for the player.
	 * @param editable Defines if the player who has opened the backpack can change the items inside.
	 */
	void openBackpack(@NotNull final Player opener, @Nullable final Backpack backpack, boolean editable);

	/**
	 * Retrieves the backpack for a given player.
	 * This method only returns a backpack if it is in the cache.
	 *
	 * @param owner The player who's backpack should be retrieved.
	 * @return The backpack of the given player. null if the backpack is in the cache.
	 */
	@Nullable Backpack getBackpackCachedOnly(@NotNull final OfflinePlayer owner);

	/**
	 * Retrieves the backpack for a given player.
	 * This method runs async! The result will be delivered with a callback.
	 *
	 * @param owner The player who's backpack should be retrieved.
	 * @param callback The callback delivering the result of the request.
	 */
	void getBackpack(@NotNull final OfflinePlayer owner, @NotNull final Callback<at.pcgamingfreaks.Minepacks.Bukkit.Backpack> callback);

	/**
	 * Gets the command manager of the Minepacks plugin.
	 *
	 * @return The command manager instance.
	 */
	MinepacksCommandManager getCommandManager();

	/**
	 * Checks if the player is allowed to open a backpack based on is permissions and current game-mode.
	 *
	 * @param player The player to be checked.
	 * @return True if the player can use a backpack. False if not.
	 */
	boolean isPlayerGameModeAllowed(Player player);
}