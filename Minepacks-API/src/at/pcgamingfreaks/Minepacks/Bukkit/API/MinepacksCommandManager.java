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

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface MinepacksCommandManager
{
	/**
	 * Registers a new sub-command for /backpack.
	 * This function is only available if the plugin is not running in standalone mode!
	 *
	 * @param command The command that should be registered.
	 */
	void registerSubCommand(@NotNull MinepacksCommand command);

	/**
	 * Unregisters a sub-command for /backpack.
	 * This function is only available if the plugin is not running in standalone mode!
	 *
	 * @param command The command that should be unregistered.
	 */
	void unRegisterSubCommand(@NotNull MinepacksCommand command);
}