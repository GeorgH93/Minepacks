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

package at.pcgamingfreaks.Minepacks.Bukkit.Listener;

import at.pcgamingfreaks.Minepacks.Bukkit.API.Events.MinepacksPlayerJoinEvent;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlayer;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CooldownHandler implements Listener
{
	private final long cooldown;
	private final boolean addOnJoin;

	public CooldownHandler(final @NotNull Minepacks plugin)
	{
		cooldown  = plugin.getConfiguration().getCommandCooldown();
		addOnJoin = plugin.getConfiguration().isCommandCooldownAddOnJoinEnabled();
	}

	public long getRemainingCooldown(final @NotNull MinepacksPlayer player)
	{
		long cd = player.getCooldown() + cooldown;
		if(cd > System.currentTimeMillis())
		{
			return cd - System.currentTimeMillis();
		}
		return 0;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinEvent(final MinepacksPlayerJoinEvent event)
	{
		if(addOnJoin)
		{
			event.getPlayer().setCooldown(System.currentTimeMillis());
		}
	}
}