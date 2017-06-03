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

package at.pcgamingfreaks.MinePacks;

import at.pcgamingfreaks.MinePacks.Database.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class OnCommand implements CommandExecutor 
{
	private final MinePacks plugin;
	private final String messageNotFromConsole, messageNoPermission, messageBackpackCleaned, messageCooldown, messageWrongGameMode;
	private final long cooldown;
	private final Collection<GameMode> gameModes;
	
	public OnCommand(MinePacks mp) 
	{
		plugin = mp;
		messageNotFromConsole = plugin.lang.getTranslated("Console.NotFromConsole");
		messageNoPermission = ChatColor.RED + plugin.lang.getTranslated("Ingame.NoPermission");
		messageBackpackCleaned = ChatColor.DARK_GREEN + plugin.lang.getTranslated("Ingame.BackpackCleaned");
		messageCooldown = ChatColor.DARK_GREEN + plugin.lang.getTranslated("Ingame.Cooldown");
		messageWrongGameMode = ChatColor.RED + plugin.lang.getTranslated("Ingame.WrongGameMode");
		cooldown = plugin.config.getCommandCooldown();
		gameModes = plugin.config.getAllowedGameModes();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) 
	{
		final Player player;
		if (sender instanceof Player) 
		{
			player = (Player) sender;
	    }
		else
		{
			sender.sendMessage(messageNotFromConsole);
			return true;
		}
		if(args.length == 0)
		{
			// Open player backpack
			if(player.hasPermission("backpack") || player.hasPermission("backpack.use"))
			{
				if(gameModes.contains(player.getGameMode()) || player.hasPermission("backpack.ignoreGameMode"))
				{
					if(!player.hasPermission("backpack.noCooldown"))
					{
						if(plugin.cooldowns.containsKey(player) && (System.currentTimeMillis() - plugin.cooldowns.get(player)) < 0)
						{
							sender.sendMessage(messageCooldown);
							return true;
						}
						if(cooldown > 0) plugin.cooldowns.put(player, System.currentTimeMillis() + cooldown);
					}
					plugin.openBackpack(player, player, true);
				}
				else
				{
					player.sendMessage(messageWrongGameMode);
				}
			}
			else
			{
				player.sendMessage(messageNoPermission);
			}
		}
		else
		{
			// Sub-commands
			switch(args[0].toLowerCase())
			{
				case "help": // Shows the help for the plugin
				case "hilfe":
				case "?":
					if(player.hasPermission("backpack") || player.hasPermission("backpack.use"))
					{
						player.sendMessage(ChatColor.GOLD + "Minepacks Help:");
						player.sendMessage(ChatColor.AQUA + "/backpack" + ChatColor.WHITE + " - " + plugin.lang.getTranslated("Description.Backpack"));
						if(player.hasPermission("backpack.clean"))
						{
							player.sendMessage(ChatColor.AQUA + "/backpack clean" + ChatColor.WHITE + " - " + plugin.lang.getTranslated("Description.Clean"));
						}
						if(player.hasPermission("backpack.clean.other"))
						{
							player.sendMessage(ChatColor.AQUA + "/backpack clean <playername>" + ChatColor.WHITE + " - " + plugin.lang.getTranslated("Description.CleanOther"));
						}
						if(player.hasPermission("backpack.other"))
						{
							player.sendMessage(ChatColor.AQUA + "/backpack <playername>" + ChatColor.WHITE + " - " + plugin.lang.getTranslated("Description.View"));
						}
					}
					else
					{
						player.sendMessage(messageNoPermission);
					}
					break;
				case "empty": // Removes all items from the backpack
				case "clean":
				case "clear":
					if(player.hasPermission("backpack.clean"))
					{
						final OfflinePlayer OP = (args.length == 2 && player.hasPermission("backpack.clean.other")) ? Bukkit.getOfflinePlayer(args[1]) : player;
						plugin.DB.getBackpack(OP, new Database.Callback<Backpack>()
						{
							@Override
							public void onResult(Backpack backpack)
							{
								if(backpack != null)
								{
									backpack.getInventory().clear();
									backpack.save();
									player.sendMessage(messageBackpackCleaned);
								}
								else
								{
									player.sendMessage(plugin.messageInvalidBackpack);
								}
							}

							@Override
							public void onFail()
							{
								player.sendMessage(plugin.messageInvalidBackpack);
							}
						});
					}
					else
					{
						player.sendMessage(messageNoPermission);
					}
					break;
				default: // Shows the backpack of an other player
					if(player.hasPermission("backpack.others"))
					{
						plugin.openBackpack(player, Bukkit.getOfflinePlayer(args[0]), player.hasPermission("backpack.others.edit"));
					}
					else
					{
						player.sendMessage(messageNoPermission);
					}
					break;
			}
		}
		return true;
	}
}