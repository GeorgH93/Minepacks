/*
 *   Copyright (C) 2023 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Placeholder.Replacer;

import at.pcgamingfreaks.Bukkit.Placeholder.PlaceholderName;
import at.pcgamingfreaks.Bukkit.Placeholder.PlaceholderReplacerBase;
import at.pcgamingfreaks.Minepacks.Bukkit.ItemsCollector;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@PlaceholderName(aliases = "IsAutoPickupEnabled")
public class AutoPickupEnabled extends PlaceholderReplacerBase
{
	private final Minepacks plugin;

	public AutoPickupEnabled(Minepacks mp)
	{
		this.plugin = mp;
	}

	@Override
	public @Nullable String replace(OfflinePlayer player)
	{
		ItemsCollector collector = plugin.getItemsCollector();
		if (collector != null)
		{
			if (player instanceof Player)
			{
				return collector.canUseAutoPickup((Player) player) ? "true" : "false";
			}
			return "offline";
		}
		return "disabled";
	}
}
