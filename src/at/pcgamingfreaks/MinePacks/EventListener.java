/*
 *   Copyright (C) 2014-2016 GeorgH93
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

package at.pcgamingfreaks.Minepacks;

import at.pcgamingfreaks.Minepacks.Database.Database;
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
	private Minepacks plugin;
	private boolean drop_on_death, showCloseMessageOwn, showCloseMessageOther;
	
	private String messageOwnBPClose, messagePlayerBPClose;
	
	public EventListener(Minepacks mp)
	{
		plugin = mp;
		drop_on_death = plugin.config.getDropOnDeath();
		messageOwnBPClose = plugin.lang.getTranslated("Ingame.OwnBackPackClose");
		messagePlayerBPClose = plugin.lang.getTranslated("Ingame.PlayerBackPackClose");
		showCloseMessageOther = messagePlayerBPClose != null && plugin.config.getShowCloseMessage();
		showCloseMessageOwn = messageOwnBPClose != null && plugin.config.getShowCloseMessage();
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		final Player player = event.getEntity();
		if (drop_on_death && !player.hasPermission("backpack.KeepOnDeath"))
		{
			plugin.DB.getBackpack(player, new Database.Callback<Backpack>()
			{
				@Override
				public void onResult(Backpack backpack)
				{
					Inventory backpackInventory = backpack.getInventory();
					for(ItemStack i : backpackInventory.getContents())
					{
						if(i != null)
						{
							player.getWorld().dropItemNaturally(player.getLocation(), i);
							backpackInventory.remove(i);
							backpack.setChanged();
						}
					}
					backpack.save(); // We have to save it now!
				}

				@Override
				public void onFail()
				{

				}
			});
		}
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent event)
	{
		if (event.getInventory() != null && event.getInventory().getHolder() instanceof Backpack && event.getPlayer() instanceof Player)
	    {
			Backpack backpack = (Backpack)event.getInventory().getHolder();
			if(!backpack.inUse())
			{
				Player closer = (Player)event.getPlayer();
				if(backpack.canEdit(closer))
				{
					backpack.save();
				}
				backpack.close(closer);
				if(event.getPlayer().getName().equals(backpack.getOwner().getName()))
				{
					if(showCloseMessageOwn)
					{
						closer.sendMessage(messageOwnBPClose);
					}
				}
				else
				{
					if(showCloseMessageOther)
					{
						closer.sendMessage(String.format(messagePlayerBPClose, backpack.getOwner().getName()));
					}
				}
			}
	    }
	}
	
	@EventHandler
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
	
	@EventHandler
	public void onPlayerLoginEvent(PlayerJoinEvent event)
	{
		plugin.DB.updatePlayerAndLoadBackpack(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeaveEvent(PlayerQuitEvent event)
	{
		Backpack backpack = plugin.DB.getBackpack(event.getPlayer());
		if(backpack != null && !backpack.isOpen())
		{
			backpack.save();
			plugin.DB.unloadBackpack(backpack);
		}
		if(plugin.cooldowns.containsKey(event.getPlayer()))
		{
			plugin.cooldowns.remove(event.getPlayer());
		}
	}
}