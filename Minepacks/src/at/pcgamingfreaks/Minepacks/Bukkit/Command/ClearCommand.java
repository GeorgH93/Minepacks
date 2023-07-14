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

package at.pcgamingfreaks.Minepacks.Bukkit.Command;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import at.pcgamingfreaks.Minepacks.Bukkit.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClearCommand extends MinepacksCommand
{
	private final Message messageCleared, messageClearedOther, messageClearedBy;
	private final String helpParam, descriptionCleanOthers;

	public ClearCommand(Minepacks plugin)
	{
		super(plugin, "clear", plugin.getLanguage().getTranslated("Commands.Description.Clean"), Permissions.CLEAN, plugin.getLanguage().getCommandAliases("Clean"));
		descriptionCleanOthers = plugin.getLanguage().getTranslated("Commands.Description.CleanOthers");
		helpParam = "<" + plugin.getLanguage().get("Commands.PlayerNameVariable") + ">";
		messageCleared = plugin.getLanguage().getMessage("Ingame.Clean.BackpackCleaned");
		messageClearedBy = plugin.getLanguage().getMessage("Ingame.Clean.BackpackCleanedBy").placeholders(Placeholders.PLAYER_NAME);
		messageClearedOther = plugin.getLanguage().getMessage("Ingame.Clean.BackpackCleanedOther").placeholders(Placeholders.PLAYER_NAME);
	}

	@Override
	public void execute(final @NotNull CommandSender commandSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		OfflinePlayer target = null;
		if(commandSender instanceof Player && args.length < 2)
		{
			Player player = (Player) commandSender;
			target = (args.length == 1 && player.hasPermission(Permissions.CLEAN_OTHER)) ? Bukkit.getOfflinePlayer(args[0]) : player;
		}
		else if(args.length == 1) target = Bukkit.getOfflinePlayer(args[0]);
		if(target != null)
		{
			getMinepacksPlugin().getBackpack(target, new Callback<Backpack>()
			{
				@Override
				public void onResult(Backpack backpack)
				{
					if(backpack != null)
					{
						backpack.clear();
						if(commandSender instanceof Player && ((Player) commandSender).getUniqueId().equals(backpack.getOwnerId()))
						{
							messageCleared.send(commandSender);
						}
						else
						{
							Player owner = backpack.getOwnerPlayer();
							if(owner != null)
							{
								messageClearedOther.send(commandSender, owner);
								messageClearedBy.send(owner, commandSender);
							}
							else
							{
								messageClearedOther.send(commandSender, backpack.getOwner());
							}
						}
					}
					else
					{
						((Minepacks) getMinepacksPlugin()).messageInvalidBackpack.send(commandSender);
					}
				}

				@Override
				public void onFail()
				{
					((Minepacks) getMinepacksPlugin()).messageInvalidBackpack.send(commandSender);
				}
			});
		}
		else
		{
			showHelp(commandSender, mainCommandAlias);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length > 0 && (!(commandSender instanceof Player) || commandSender.hasPermission(Permissions.CLEAN_OTHER)))
		{
			return Utils.getPlayerNamesStartingWith(args[args.length - 1], commandSender);
		}
		return null;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = super.getHelp(requester);
		if(!(requester instanceof Player) || requester.hasPermission(Permissions.CLEAN_OTHER))
		{
			//noinspection ConstantConditions
			help.add(new HelpData(getTranslatedName(), helpParam, descriptionCleanOthers));
		}
		return help;
	}
}