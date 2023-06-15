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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BackupCommand extends MinepacksCommand
{
	private final Message messageCreated, messageNoBackpack;
	private final String helpParam, helpParamEveryone, descriptionEveryone;

	public BackupCommand(Minepacks plugin)
	{
		super(plugin, "backup", plugin.getLanguage().getTranslated("Commands.Description.Backup"), Permissions.BACKUP, plugin.getLanguage().getCommandAliases("Backup"));
		helpParam = "<" + plugin.getLanguage().get("Commands.PlayerNameVariable") + ">";
		helpParamEveryone = "!everyone!";
		descriptionEveryone = plugin.getLanguage().getTranslated("Commands.Description.BackupEveryone");
		messageCreated = plugin.getLanguage().getMessage("Ingame.Backup.Created");
		messageNoBackpack = plugin.getLanguage().getMessage("Ingame.Backup.NoBackpack");
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		if(args.length == 1)
		{
			if(args[0].equalsIgnoreCase("!everyone!"))
			{
				for(Player player : plugin.getServer().getOnlinePlayers())
				{
					backup(sender, player);
				}
			}
			else
			{
				//noinspection deprecation
				backup(sender, plugin.getServer().getOfflinePlayer(args[0]));
			}
		}
		else
		{
			showHelp(sender, mainCommandAlias);
		}
	}

	private void backup(final @NotNull CommandSender sender, final @NotNull OfflinePlayer offlinePlayer)
	{
		getMinepacksPlugin().getBackpack(offlinePlayer, new Callback<Backpack>() {
			@Override
			public void onResult(Backpack backpack)
			{
				((at.pcgamingfreaks.Minepacks.Bukkit.Backpack) backpack).backup();
				messageCreated.send(sender);
			}

			@Override
			public void onFail()
			{
				messageNoBackpack.send(sender);
			}
		}, false);
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		return Utils.getPlayerNamesStartingWith(args[args.length - 1], sender);
	}

	@Override
	public List<HelpData> getHelp(final @NotNull CommandSender requester)
	{
		List<HelpData> help = new ArrayList<>(1);
		help.add(new HelpData(getTranslatedName(), helpParam, getDescription()));
		help.add(new HelpData(getTranslatedName(), helpParamEveryone, descriptionEveryone));
		return help;
	}
}