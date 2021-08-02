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

import at.pcgamingfreaks.Bukkit.Debug.EventToStringUtil;
import at.pcgamingfreaks.Bukkit.GUI.GuiButton;
import at.pcgamingfreaks.Bukkit.Util.InventoryUtils;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backpack.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class BackpackControls implements Listener
{
	private final Minepacks plugin;

	public BackpackControls(final @NotNull Minepacks plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event)
	{
		final Inventory clickedInventory = InventoryUtils.getClickedInventory(event);
		if(clickedInventory == null || !(clickedInventory.getHolder() instanceof Backpack)) return; // If the click was not in the backpack inventory it can not be on a button
		EventToStringUtil.logEvent(plugin.getLogger(), event);
		Backpack backpack = (Backpack) clickedInventory.getHolder();
		GuiButton button = backpack.getButton(event.getRawSlot());
		if (button == null) return;
		event.setCancelled(true);
		if (!(event.getWhoClicked() instanceof Player)) return;
		button.onClick((Player) event.getWhoClicked(), event.getClick(), event.getCursor());
	}
}