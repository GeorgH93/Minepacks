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

package at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI;

import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin;

import org.jetbrains.annotations.NotNull;

public interface MinepacksPluginExtended extends MinepacksPlugin
{
	/**
	 * Gets the command manager of the Minepacks plugin.
	 *
	 * @return The command manager instance. null if the plugin is running in standalone mode
	 */
	@NotNull MinepacksCommandManager getCommandManager();
}