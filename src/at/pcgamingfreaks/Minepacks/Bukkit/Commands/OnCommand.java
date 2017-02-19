/*
 *   Copyright (C) 2017 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Commands;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

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
	private final Minepacks plugin;
	private final Message messageNotFromConsole, messageBackpackCleaned, messageCooldown, messageWrongGameMode;
	private final long cooldown;
	private final boolean syncCooldown;
	private final Collection<GameMode> gameModes;
	private final String allowedGameModes;
	
	public OnCommand(Minepacks mp)
	{
		plugin = mp;
		messageNotFromConsole  = plugin.lang.getMessage("NotFromConsole");
		messageBackpackCleaned = plugin.lang.getMessage("Ingame.BackpackCleaned");
		messageCooldown        = plugin.lang.getMessage("Ingame.Cooldown").replaceAll("\\{TimeLeft}", "%1\\$.1f");
		messageWrongGameMode   = plugin.lang.getMessage("Ingame.WrongGameMode").replaceAll("\\{CurrentGameMode}", "%1\\$s").replaceAll("\\{AllowedGameModes}", "%1\\$s");
		cooldown = plugin.config.getCommandCooldown();
		syncCooldown = plugin.config.isCommandCooldownSyncEnabled();
		gameModes = plugin.config.getAllowedGameModes();
		StringBuilder allowedGameModesBuilder = new StringBuilder("");
		for(GameMode gameMode : gameModes)
		{
			if(allowedGameModesBuilder.length() > 1)
			{
				allowedGameModesBuilder.append(", ");
			}
			allowedGameModesBuilder.append(gameMode.name().toLowerCase());
		}
		allowedGameModes = allowedGameModesBuilder.toString();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) 
	{
		if (!(sender instanceof Player))
		{
			messageNotFromConsole.send(sender);
			return true;
		}
		final Player player = (Player) sender;
		if(args.length == 0)
		{
			// Open player backpack
			if(player.hasPermission("backpack.use") || player.hasPermission("backpack"))
			{
				if(gameModes.contains(player.getGameMode()) || player.hasPermission("backpack.ignoreGameMode"))
				{
					if(cooldown > 0 && !player.hasPermission("backpack.noCooldown"))
					{
						if(plugin.cooldowns.containsKey(player.getUniqueId()))
						{
							long cd = plugin.cooldowns.get(player.getUniqueId());
							if(cd < System.currentTimeMillis())
							{
								cd = cd - System.currentTimeMillis();
								messageCooldown.send(sender, cd / 1000f);
								return true;
							}
						}
						final long cooldownTime = System.currentTimeMillis() + cooldown;
						if(syncCooldown)
						{
							plugin.getDb().syncCooldown(player, cooldownTime);
						}
						plugin.cooldowns.put(player.getUniqueId(), cooldownTime);
					}
					plugin.openBackpack(player, player, true);
				}
				else
				{
					messageWrongGameMode.send(player, player.getGameMode().name().toLowerCase(), allowedGameModes);
 				}
			}
			else
			{
				plugin.messageNoPermission.send(player);
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
					if(player.hasPermission("backpack.use") || player.hasPermission("backpack"))
					{
						player.sendMessage(ChatColor.GOLD + "Minepacks Help:");
						player.sendMessage(ChatColor.AQUA + "/backpack" + ChatColor.WHITE + " - " + plugin.lang.getTranslated("Commands.Description.Backpack"));
						if(player.hasPermission("backpack.clean"))
						{
							player.sendMessage(ChatColor.AQUA + "/backpack clean" + ChatColor.WHITE + " - " + plugin.lang.getTranslated("Commands.Description.Clean"));
						}
						if(player.hasPermission("backpack.clean.other"))
						{
							player.sendMessage(ChatColor.AQUA + "/backpack clean <playername>" + ChatColor.WHITE + " - " + plugin.lang.getTranslated("Commands.Description.CleanOther"));
						}
						if(player.hasPermission("backpack.other"))
						{
							player.sendMessage(ChatColor.AQUA + "/backpack <playername>" + ChatColor.WHITE + " - " + plugin.lang.getTranslated("Commands.Description.View"));
						}
					}
					else
					{
						plugin.messageNoPermission.send(player);
					}
					break;
				case "empty": // Removes all items from the backpack
				case "clean":
				case "clear":
					if(player.hasPermission("backpack.clean"))
					{
						final OfflinePlayer OP = (args.length == 2 && player.hasPermission("backpack.clean.other")) ? Bukkit.getOfflinePlayer(args[1]) : player;
						plugin.getBackpack(OP, new Callback<Backpack>()
						{
							@Override
							public void onResult(Backpack backpack)
							{
								if(backpack != null)
								{
									backpack.clear();
									messageBackpackCleaned.send(player);
								}
								else
								{
									plugin.messageInvalidBackpack.send(player);
								}
							}

							@Override
							public void onFail()
							{
								plugin.messageInvalidBackpack.send(player);
							}
						});
					}
					else
					{
						plugin.messageNoPermission.send(player);
					}
					break;
				default: // Shows the backpack of an other player
					if(player.hasPermission("backpack.others"))
					{
						plugin.openBackpack(player, Bukkit.getOfflinePlayer(args[0]), player.hasPermission("backpack.others.edit"));
					}
					else
					{
						plugin.messageNoPermission.send(player);
					}
					break;
			}
		}
		return true;
	}
}