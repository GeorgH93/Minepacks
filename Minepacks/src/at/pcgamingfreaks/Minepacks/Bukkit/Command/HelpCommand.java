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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Command;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HelpCommand extends MinepacksCommand
{
	private final Collection<MinepacksCommand> commands;
	private final Message messageHeader, messageFooter;
	private final CommandManager commandManager;

	public HelpCommand(Minepacks plugin, Collection<MinepacksCommand> commands, CommandManager commandManager)
	{
		super(plugin, "help", plugin.getLanguage().getTranslated("Commands.Description.Help"), plugin.getLanguage().getCommandAliases("Help"));
		this.commands = commands;
		this.commandManager = commandManager;

		messageHeader = plugin.getLanguage().getMessage("Ingame.Help.Header");
		messageFooter = plugin.getLanguage().getMessage("Ingame.Help.Footer");
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		messageHeader.send(sender);
		Collection<HelpData> help = new ArrayList<>(), temp;
		for(MinepacksCommand cmd : commands)
		{
			temp = cmd.doGetHelp(sender);
			if(temp != null) help.addAll(temp);
		}
		commandManager.sendHelp(sender, mainCommandAlias, help);
		messageFooter.send(sender);
		//TODO: pages
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return null;
	}
}