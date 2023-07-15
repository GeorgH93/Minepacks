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

package at.pcgamingfreaks.Minepacks.Bukkit.Listener;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Placeholders;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BackpackEventListener extends MinepacksListener
{
	private final Message messageOwnBackpackClose, messageOtherBackpackClose;
	private final Sound closeSound;
	
	public BackpackEventListener(Minepacks plugin)
	{
		super(plugin);
		messageOwnBackpackClose = plugin.getLanguage().getMessage("Ingame.OwnBackpackClose");
		messageOtherBackpackClose = plugin.getLanguage().getMessage("Ingame.PlayerBackpackClose").placeholders(Placeholders.mkPlayerName("Owner"));
		closeSound = plugin.getConfiguration().getCloseSound();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onClose(InventoryCloseEvent event)
	{
		if (event.getInventory() != null && event.getInventory().getHolder() instanceof Backpack && event.getPlayer() instanceof Player)
		{
			Backpack backpack = (Backpack)event.getInventory().getHolder();
			Player closer = (Player)event.getPlayer();
			if(backpack.canEdit(closer))
			{
				backpack.save();
			}
			backpack.close(closer);
			if(event.getPlayer().getUniqueId().equals(backpack.getOwnerId()))
			{
				messageOwnBackpackClose.send(closer);
			}
			else
			{
				messageOtherBackpackClose.send(closer, backpack.getOwner());
			}
			if(closeSound != null)
			{
				closer.playSound(closer.getLocation(), closeSound, 1, 0);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onClick(InventoryClickEvent event)
	{
		if (event.getInventory() != null && event.getInventory().getHolder() instanceof Backpack && event.getWhoClicked() instanceof Player)
		{
			Backpack backpack = (Backpack) event.getInventory().getHolder();
			if(!backpack.canEdit((Player)event.getWhoClicked()))
			{
				event.setCancelled(true);
			}
		    else
			{
				backpack.setChanged();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeaveEvent(PlayerQuitEvent event)
	{
		Backpack backpack = plugin.getDatabase().getBackpack(event.getPlayer());
		if(backpack != null) backpack.save();
	}
}