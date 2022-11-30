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

package at.pcgamingfreaks.Minepacks.Bukkit;

import at.pcgamingfreaks.Minepacks.Bukkit.API.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.ItemFilter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ItemsCollector extends BukkitRunnable
{
	private final Minepacks plugin;
	private final double radius;
	private final BukkitTask task;
	private final ItemFilter itemFilter;

	/**
	 * Is the feature enabled?
	 */
	private final boolean isToggleable;

	/**
	 * Default on join?
	 */
	private final boolean enabledOnJoin;

	/**
	 * List of players that toggled the feature.
	 */
	private final Set<UUID> toggleList;

	public ItemsCollector(Minepacks plugin)
	{
		this.plugin = plugin;
		this.radius = plugin.getConfiguration().getFullInvRadius();

		this.isToggleable = plugin.getConfiguration().isToggleAllowed();
		this.enabledOnJoin = plugin.getConfiguration().isEnabledOnJoin();
		this.toggleList = new HashSet<>();

		task = runTaskTimer(plugin, plugin.getConfiguration().getFullInvCheckInterval(), plugin.getConfiguration().getFullInvCheckInterval());
		itemFilter = plugin.getItemFilter();
	}

	@Override
	public void run()
	{
		for(Player player : Bukkit.getServer().getOnlinePlayers())
		{
			if(plugin.isDisabled(player) != WorldBlacklistMode.None) return;

			// Check toggle
			if (!isToggleable || !isPickupEnabled(player.getUniqueId())) return;

			// No permission ot use the backpack.
			if (!player.hasPermission(Permissions.USE)) return;

			// If a player has either of these permissions, pickup is allowed.
			if (!player.hasPermission(Permissions.FULL_PICKUP) && !player.hasPermission(Permissions.PICKUP_TOGGLE));

			// Inventory is full
			if (player.getInventory().firstEmpty() != -1) return;

			// Only check loaded backpacks (loading them would take too much time for a repeating task, the backpack will be loaded async soon enough)
			Backpack backpack = (Backpack) plugin.getBackpackCachedOnly(player);
			if(backpack == null)
			{
				continue;
			}
			List<Entity> entities = player.getNearbyEntities(radius, radius, radius);
			for(Entity entity : entities)
			{
				if(entity instanceof Item) {
					Item item = (Item) entity;
					if (!item.isDead() && item.getPickupDelay() <= 0) {
						Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.getItemStack());
						if (!leftover.isEmpty()) {
							ItemStack itemStack = leftover.get(0);
							if (itemStack == null || itemStack.getAmount() == 0 || (itemFilter != null && itemFilter.isItemBlocked(itemStack)))
								continue;
							leftover = backpack.addItems(itemStack);
						}
						if (!leftover.isEmpty()) {
							item.setItemStack(leftover.get(0));
						} else {
							item.remove();
						}
					}
				}
			}
		}
	}

	public void close()
	{
		task.cancel();
	}

	/**
	 * Toggles the automatic collection for the player.
	 * @param uuid The players UUID
	 * @return The new state. True = collection enabled.
	 */
	public boolean toggleState(UUID uuid) {
		boolean removed = toggleList.remove(uuid);
		if (!removed) {
			toggleList.add(uuid);
		}
		return isPickupEnabled(uuid);
	}

	/**
	 * The item pickup state for a certain player.
	 * @param uuid The player uuid
	 * @return true if enabled
	 */
	public boolean isPickupEnabled(UUID uuid) {
		return enabledOnJoin ^ toggleList.contains(uuid);
	}

	/**
	 * If this feature is enabled or not.
	 * @return
	 */
	public boolean isToggleable() {
		return isToggleable;
	}
}