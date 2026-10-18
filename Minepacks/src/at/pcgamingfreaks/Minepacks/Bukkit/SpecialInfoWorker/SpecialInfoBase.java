/*
 *   Copyright (C) 2025 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.SpecialInfoWorker;

import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class SpecialInfoBase implements Listener
{
	private final JavaPlugin plugin;
	private final String permission;

	protected SpecialInfoBase(final JavaPlugin plugin, final String permission)
	{
		this.plugin = plugin;
		this.permission = permission;
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent event)
	{
		if(event.getPlayer().hasPermission(permission))
		{
			Minepacks.getScheduler().runLater(() ->
				Minepacks.getScheduler().runAtEntity(event.getPlayer(), task -> {
					if(event.getPlayer().isOnline())
					{
						sendMessage(event.getPlayer());
					}
				}), 3 * 20L); // Run with a 3 seconds delay
		}
	}

	protected abstract void sendMessage(final Player player);
}