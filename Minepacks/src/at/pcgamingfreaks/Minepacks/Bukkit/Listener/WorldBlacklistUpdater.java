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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Listener;

import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldInitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class WorldBlacklistUpdater extends MinepacksListener
{
	public WorldBlacklistUpdater(final @NotNull Minepacks plugin)
	{
		super(plugin);
	}

	@EventHandler
	public void onWorldInit(final WorldInitEvent event)
	{
		String worldName = event.getWorld().getName().toLowerCase(Locale.ROOT);
		if(!plugin.getConfiguration().getWorldFilteredList().contains(worldName))
		{
			plugin.getWorldBlacklist().add(worldName);
		}
	}
}