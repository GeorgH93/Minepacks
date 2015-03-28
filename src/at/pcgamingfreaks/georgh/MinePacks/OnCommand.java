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

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class OnCommand implements CommandExecutor 
{
	private MinePacks plugin;
	
	public String Message_NotFromConsole, Message_NoPermission, Message_IvalidBackpack, Message_BackpackCleaned;
	
	public OnCommand(MinePacks mp) 
	{
		plugin = mp;
		Message_NotFromConsole = ChatColor.translateAlternateColorCodes('&', plugin.lang.Get("Console.NotFromConsole"));
		Message_NoPermission = ChatColor.translateAlternateColorCodes('&', ChatColor.RED + plugin.lang.Get("Ingame.NoPermission"));
		Message_IvalidBackpack = ChatColor.translateAlternateColorCodes('&', ChatColor.RED + plugin.lang.Get("Ingame.IvalidBackpack"));
		Message_BackpackCleaned = ChatColor.translateAlternateColorCodes('&', ChatColor.DARK_GREEN + plugin.lang.Get("Ingame.BackpackCleaned"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) 
	{
		Player player = null;
		if (sender instanceof Player) 
		{
			player = (Player) sender;
	    }
		else
		{
			sender.sendMessage(Message_NotFromConsole);
			return true;
		}
		
		if(args.length == 0)
		{
			// Open player backpack
			if(player.hasPermission("backpack"))
			{
				Backpack bp = plugin.DB.getBackpack(player, false);
				if(bp == null)
				{
					player.sendMessage(Message_IvalidBackpack);
					return true;
				}
				int size = plugin.getBackpackPermSize(player);
				if(size != bp.getSize())
				{
					List<ItemStack> items = bp.setSize(size);
					for(ItemStack i : items)
					{
						if (i != null)
					    {
					        player.getWorld().dropItemNaturally(player.getLocation(), i);
					    }
					}
				}
				plugin.OpenBackpack(player, bp, true);
			}
			else
			{
				player.sendMessage(Message_NoPermission);
			}
		}
		else
		{
			// Subcommands
			switch(args[0].toLowerCase())
			{
				case "help": // Shows the help for the plugin
				case "hilfe":
				case "?":
					if(player.hasPermission("backpack"))
					{
						player.sendMessage(ChatColor.GOLD + "Minepacks Help:");
						player.sendMessage(ChatColor.AQUA + "/backpack" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Backpack"));
						if(player.hasPermission("backpack.clean"))
						{
							player.sendMessage(ChatColor.AQUA + "/backpack clean" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.Clean"));
						}
						if(player.hasPermission("backpack.clean.other"))
						{
							player.sendMessage(ChatColor.AQUA + "/backpack clean <playername>" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.CleanOther"));
						}
						if(player.hasPermission("backpack.other"))
						{
							player.sendMessage(ChatColor.AQUA + "/backpack <playername>" + ChatColor.WHITE + " - " + plugin.lang.Get("Description.View"));
						}
					}
					else
					{
						player.sendMessage(Message_NoPermission);
					}
					break;
				case "empty": // Removes all items from the backpack
				case "clean":
				case "clear":
					if(player.hasPermission("backpack.clean"))
					{
						OfflinePlayer OP = player;
						if(player.hasPermission("backpack.clean.other") && args.length == 2)
						{
							OP = Bukkit.getOfflinePlayer(args[1]);
						}
						Backpack BP = plugin.DB.getBackpack(OP, false);
						BP.getBackpack().clear();
						player.sendMessage(Message_BackpackCleaned);
					}
					else
					{
						player.sendMessage(Message_NoPermission);
					}
					break;
				default: // Shows the backpack of an other player
					if(player.hasPermission("backpack.others"))
					{
						plugin.OpenBackpack(player, Bukkit.getOfflinePlayer(args[0]), player.hasPermission("backpack.others.edit"));
					}
					else
					{
						player.sendMessage(Message_NoPermission);
					}
					break;
			}
		}
		return true;
	}
}