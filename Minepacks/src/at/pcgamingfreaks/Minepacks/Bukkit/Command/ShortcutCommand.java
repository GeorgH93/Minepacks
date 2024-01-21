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

import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.ItemShortcut;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShortcutCommand extends MinepacksCommand
{
	private final ItemShortcut itemShortcut;

	public ShortcutCommand(Minepacks plugin, final @NotNull ItemShortcut itemShortcut)
	{
		super(plugin, "shortcut", ""/*plugin.getLanguage().getTranslated("Commands.Description.Shortcut")*/, Permissions.USE, true, plugin.getLanguage().getCommandAliases("Shortcut"));
		this.itemShortcut = itemShortcut;
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if (args.length == 1 && sender.hasPermission(Permissions.OTHERS))
		{
			Player p = Bukkit.getPlayer(args[0]);
			if (p != null && p.hasPermission(Permissions.USE)) itemShortcut.addItem(p);
		}
		else
		{
			itemShortcut.addItem((Player) sender);
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return null;
	}
	@Override
	public @Nullable List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		return null;
	}
}
