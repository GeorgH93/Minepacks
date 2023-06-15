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

import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VersionCommand extends MinepacksCommand
{
	private final String minepacksVersion;

	public VersionCommand(Minepacks plugin)
	{
		super(plugin, "version", plugin.getLanguage().getTranslated("Commands.Description.Version"), Permissions.VERSION, plugin.getLanguage().getCommandAliases("Version"));
		minepacksVersion = plugin.getDescription().getName() + ": " + plugin.getDescription().getVersion();
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		sender.sendMessage("##### Start Minepacks version info #####");
		sender.sendMessage(minepacksVersion);
		/*if_not[STANDALONE]*/
		sender.sendMessage("PCGF PluginLib: " + at.pcgamingfreaks.PluginLib.Bukkit.PluginLib.getInstance().getVersion());
		/*end[STANDALONE]*/
		sender.sendMessage("Server: " +  plugin.getServer().getVersion());
		sender.sendMessage("Java: " + System.getProperty("java.version"));
		sender.sendMessage("#####  End Minepacks version info  #####");
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return null;
	}
}