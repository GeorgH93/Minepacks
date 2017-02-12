/*
 *   Copyright (C) 2016-2017 GeorgH93
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

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener extends MinepacksListener
{
	private final Message messageOwnBackpackClose, messageOtherBackpackClose;
	
	public EventListener(Minepacks plugin)
	{
		super(plugin);
		messageOwnBackpackClose = plugin.lang.getMessage("Ingame.OwnBackpackClose");
		messageOtherBackpackClose = plugin.lang.getMessage("Ingame.PlayerBackpackClose").replaceAll("\\{OwnerName\\}", "%1\\$s").replaceAll("\\{OwnerDisplayName\\}", "%2\\$s");
	}
	
	@EventHandler
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
			if(event.getPlayer().getUniqueId().equals(backpack.getOwner().getUniqueId()))
			{
				messageOwnBackpackClose.send(closer);
			}
			else
			{
				OfflinePlayer owner = backpack.getOwner();
				messageOtherBackpackClose.send(closer, owner.getName(), owner.isOnline() ? owner.getPlayer().getDisplayName() : ChatColor.GRAY + owner.getName());
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
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLeaveEvent(PlayerQuitEvent event)
	{
		if(plugin.cooldowns.containsKey(event.getPlayer().getUniqueId()))
		{
			plugin.cooldowns.remove(event.getPlayer().getUniqueId());
		}
	}
}