/*
 *   Copyright (C) 2022 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit;

import at.pcgamingfreaks.Bukkit.Message.Placeholder.Processors.PlayerDisplayNamePlaceholderProcessor;
import at.pcgamingfreaks.Bukkit.Message.Placeholder.Processors.PlayerNamePlaceholderProcessor;
import at.pcgamingfreaks.Message.Placeholder.Placeholder;

import org.jetbrains.annotations.NotNull;

public class Placeholders
{
	private Placeholders(){}

	public static final @NotNull String[] PAGE_OPTIONS = { "CurrentPage", "MaxPage", "MainCommand", "SubCommand", "PrevPage", "NextPage" };
	public static final @NotNull Placeholder[] PLAYER_NAME = mkPlayerName("");

	public static @NotNull Placeholder[] mkPlayerName(final @NotNull String prefix)
	{
		return mkPlayerName(prefix, "", false);
	}

	public static @NotNull Placeholder[] mkPlayerName(final @NotNull String prefix, final @NotNull String suffix)
	{
		return mkPlayerName(prefix, suffix, false);
	}

	public static @NotNull Placeholder[] mkPlayerName(final @NotNull String prefix, final @NotNull String suffix, final boolean regex)
	{
		return new Placeholder[]{
				new Placeholder(prefix + "Name" + suffix, PlayerNamePlaceholderProcessor.INSTANCE, regex),
				new Placeholder(prefix + "DisplayName" + suffix, PlayerDisplayNamePlaceholderProcessor.INSTANCE, regex)
		};
	}

	public static @NotNull Placeholder[] mkPlayerNameRegex(final @NotNull String prefix)
	{
		return mkPlayerName(prefix, "", true);
	}

	public static @NotNull Placeholder[] mkPlayerNameRegex(final @NotNull String prefix, final @NotNull String suffix)
	{
		return mkPlayerName(prefix, suffix, true);
	}
}