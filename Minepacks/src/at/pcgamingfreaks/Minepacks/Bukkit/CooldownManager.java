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

package at.pcgamingfreaks.Minepacks.Bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager extends CancellableRunnable implements Listener
{
	private final Minepacks plugin;
	private final Map<UUID, Long> cooldowns = new HashMap<>();
	private final long cooldown;
	private final boolean syncCooldown, addOnJoin, clearOnLeave;

	public CooldownManager(Minepacks plugin)
	{
		this.plugin = plugin;

		cooldown = plugin.getConfiguration().getCommandCooldown();
		syncCooldown = plugin.getConfiguration().isCommandCooldownSyncEnabled();
		addOnJoin = plugin.getConfiguration().isCommandCooldownAddOnJoinEnabled();
		clearOnLeave = plugin.getConfiguration().isCommandCooldownClearOnLeaveEnabled();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		schedule();
	}

	public void close()
	{
		cancel();
		HandlerList.unregisterAll(this);
	}

	public void setCooldown(@NotNull Player player)
	{
		final long cooldownTime = System.currentTimeMillis() + cooldown;
		if(syncCooldown)
		{
			plugin.getDatabase().syncCooldown(player, cooldownTime);
		}
		cooldowns.put(player.getUniqueId(), cooldownTime);
	}

	@SuppressWarnings("unused")
	public boolean isInCooldown(@NotNull Player player)
	{
		return cooldowns.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis();
	}

	public long getRemainingCooldown(@NotNull Player player)
	{
		long cd = cooldowns.getOrDefault(player.getUniqueId(), 0L);
		if(cd > System.currentTimeMillis())
		{
			return cd - System.currentTimeMillis();
		}
		return 0;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	{
		if(syncCooldown)
		{
			final UUID uuid = event.getPlayer().getUniqueId();
			cooldowns.put(uuid, System.currentTimeMillis() + cooldown); // Temporary cooldown till the data is loaded from the database
			plugin.getDatabase().getCooldown(event.getPlayer(), dbCooldownTime -> cooldowns.put(uuid, dbCooldownTime));
		}
		else if(addOnJoin)
		{
			setCooldown(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeaveEvent(PlayerQuitEvent event)
	{
		if(clearOnLeave) cooldowns.remove(event.getPlayer().getUniqueId());
	}

	@Override
	public void run()
	{
		cooldowns.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
	}

	@Override
	public void schedule() {
		task = getScheduler().runTimer(this::run, plugin.getConfiguration().getCommandCooldownCleanupInterval(), plugin.getConfiguration().getCommandCooldownCleanupInterval());
	}
}
