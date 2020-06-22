/*
 *   Copyright (C) 2020 GeorgH93
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
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backpack.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BackupCommand extends MinepacksCommand
{
	private final Message messageCreated;
	private final String helpParam;

	public BackupCommand(final @NotNull Minepacks plugin)
	{
		super(plugin, "backup", plugin.getLanguage().getTranslated("Commands.Description.Backup"), Permissions.BACKUP, plugin.getLanguage().getCommandAliases("Backup"));
		helpParam = "<" + plugin.getLanguage().get("Commands.PlayerNameVariable") + ">";
		messageCreated = plugin.getLanguage().getMessage("Ingame.Backup.Created");
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		if(args.length == 1)
		{
			//noinspection deprecation
			getMinepacksPlugin().getBackpack(plugin.getServer().getOfflinePlayer(args[0]), backpack -> {
				((Backpack) backpack).backup();
				messageCreated.send(sender);
			}, false);
		}
		else
		{
			showHelp(sender, mainCommandAlias);
		}
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
		return help;
	}
}