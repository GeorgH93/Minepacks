/*
 *   Copyright (C) 2014-2015 GeorgH93
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

package at.pcgamingfreaks.georgh.MinePacks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener
{
	private MinePacks plugin;
	private boolean drop_on_death, showCloseMessageOwn, showCloseMessageOther;
	
	private String message_OwnBPClose, message_PlayerBPClose;
	
	public EventListener(MinePacks mp)
	{
		plugin = mp;
		drop_on_death = plugin.config.getDropOnDeath();
		message_OwnBPClose = plugin.lang.getTranslated("Ingame.OwnBackPackClose");
		message_PlayerBPClose = plugin.lang.getTranslated("Ingame.PlayerBackPackClose");
		showCloseMessageOther = message_PlayerBPClose != null && plugin.config.getShowCloseMessage();
		showCloseMessageOwn = message_OwnBPClose != null && plugin.config.getShowCloseMessage();
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		if (drop_on_death && !player.hasPermission("backpack.KeepOnDeath"))
		{
			Backpack backpack = plugin.DB.getBackpack(player, false);
			Inventory backpackInventory = backpack.getBackpack();
			for (ItemStack i : backpackInventory.getContents())
			{
			    if (i != null)
			    {
			        player.getWorld().dropItemNaturally(player.getLocation(), i);
			        backpackInventory.remove(i);
			    }
			}
			plugin.DB.saveBackpack(backpack);
		}
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent event)
	{
		if (event.getInventory() != null && event.getInventory().getTitle() != null && event.getPlayer() instanceof Player)
	    {
			Backpack backpack = plugin.DB.getBackpack(event.getInventory());
			if(backpack != null && !backpack.inUse())
			{
				Player closer = (Player)event.getPlayer();
				if(backpack.canEdit(closer))
				{
					plugin.DB.saveBackpack(backpack);
				}
				backpack.close(closer);
				if(event.getPlayer().getName().equals(backpack.getOwner().getName()))
				{
					if(showCloseMessageOwn)
					{
						closer.sendMessage(message_OwnBPClose);
					}
				}
				else
				{
					if(showCloseMessageOther)
					{
						closer.sendMessage(String.format(message_PlayerBPClose, backpack.getOwner().getName()));
					}
				}
			}
	    }
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event)
	{
		if (event.getInventory() != null && event.getInventory().getTitle() != null && event.getWhoClicked() instanceof Player)
	    {
			Backpack backpack = plugin.DB.getBackpack(event.getInventory());
			if(backpack != null && !backpack.canEdit((Player)event.getWhoClicked()))
			{
				event.setCancelled(true);
			}
	    }
	}
	
	@EventHandler
	public void PlayerLoginEvent(PlayerJoinEvent event) 
	{
		plugin.DB.updatePlayer(event.getPlayer());
	}
	
	@EventHandler
	public void PlayerLeaveEvent(PlayerQuitEvent event)
	{
		Backpack bp = plugin.DB.getBackpack(event.getPlayer(), true);
		if(bp != null && !bp.isOpen())
		{
			plugin.DB.UnloadBackpack(bp);
		}
		if(plugin.cooldowns.containsKey(event.getPlayer()))
		{
			plugin.cooldowns.remove(event.getPlayer());
		}
	}
}