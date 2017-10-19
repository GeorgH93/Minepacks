/*
 *   Copyright (C) 2014-2017 GeorgH93
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

package at.pcgamingfreaks.MinePacks;

import at.pcgamingfreaks.MinePacks.Database.Database;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
	private long joinCooldown;
	private String message_OwnBPClose, message_PlayerBPClose;
	
	public EventListener(MinePacks mp)
	{
		plugin = mp;
		drop_on_death = plugin.config.getDropOnDeath();
		message_OwnBPClose = plugin.lang.getTranslated("Ingame.OwnBackPackClose");
		message_PlayerBPClose = plugin.lang.getTranslated("Ingame.PlayerBackPackClose");
		showCloseMessageOther = message_PlayerBPClose != null && plugin.config.getShowCloseMessage();
		showCloseMessageOwn = message_OwnBPClose != null && plugin.config.getShowCloseMessage();
		joinCooldown = plugin.config.getCommandCooldownAfterJoin();
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
				public void onFail() {}
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
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
	public void onPlayerLoginEvent(PlayerJoinEvent event)
	{
		plugin.DB.updatePlayerAndLoadBackpack(event.getPlayer());
		if(joinCooldown > 0 && !event.getPlayer().hasPermission("backpack.noCooldown"))
		{
			plugin.cooldowns.put(event.getPlayer(), System.currentTimeMillis() + joinCooldown);
		}
	}
	
	@EventHandler
	public void onPlayerLeaveEvent(PlayerQuitEvent event)
	{
		Backpack backpack = plugin.DB.getBackpack(event.getPlayer());
		if(backpack != null)
		{
			backpack.save();
			if(!backpack.isOpen()) plugin.DB.unloadBackpack(backpack);
		}
		if(plugin.cooldowns.containsKey(event.getPlayer()))
		{
			plugin.cooldowns.remove(event.getPlayer());
		}
	}
}