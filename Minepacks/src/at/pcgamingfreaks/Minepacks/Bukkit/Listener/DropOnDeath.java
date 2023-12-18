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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Listener;

import at.pcgamingfreaks.Minepacks.Bukkit.API.Events.BackpackDropOnDeathEvent;
import at.pcgamingfreaks.Minepacks.Bukkit.API.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DropOnDeath extends MinepacksListener
{
	private final boolean honorKeepOnDeath;

	public DropOnDeath(Minepacks plugin)
	{
		super(plugin);
		honorKeepOnDeath = plugin.getConfiguration().getHonorKeepInventoryOnDeath();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDeath(PlayerDeathEvent event)
	{
		if(honorKeepOnDeath && event.getKeepInventory()) return;
		final Player player = event.getEntity();
		if(plugin.isDisabled(player) != WorldBlacklistMode.None) return;
		if (!player.hasPermission(Permissions.KEEP_ON_DEATH))
		{
			final Location location = player.getLocation();
			plugin.getBackpack(player, backpack -> {
				BackpackDropOnDeathEvent event1 = new BackpackDropOnDeathEvent(player, backpack);
				plugin.getServer().getPluginManager().callEvent(event1);
				if(!event1.isCancelled())
				{
					backpack.drop(location);
				}
			});
		}
	}
}