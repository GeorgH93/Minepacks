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
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UpdateCommand extends MinepacksCommand
{
	private final Message messageCheckingForUpdates, messageUpdated, messageNoUpdate, messageUpdateFail, messageUpdateAvailable;

	public UpdateCommand(final @NotNull Minepacks plugin)
	{
		super(plugin, "update", plugin.getLanguage().getTranslated("Commands.Description.Update"), Permissions.UPDATE, plugin.getLanguage().getCommandAliases("Update"));

		messageCheckingForUpdates   = plugin.getLanguage().getMessage("Ingame.Update.CheckingForUpdates");
		messageUpdated              = plugin.getLanguage().getMessage("Ingame.Update.Updated");
		messageNoUpdate             = plugin.getLanguage().getMessage("Ingame.Update.NoUpdate");
		messageUpdateFail           = plugin.getLanguage().getMessage("Ingame.Update.UpdateFail");
		messageUpdateAvailable      = plugin.getLanguage().getMessage("Ingame.Update.UpdateAvailable");
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		messageCheckingForUpdates.send(sender);
		/*if_not[STANDALONE]*/
		((at.pcgamingfreaks.PluginLib.Bukkit.PluginLib) at.pcgamingfreaks.PluginLib.Bukkit.PluginLib.getInstance()).getUpdater().update(); // Make the PluginLib check for updates too
		/*end[STANDALONE]*/
		((Minepacks) plugin).update(result -> {
			switch(result)
			{
				case SUCCESS: messageUpdated.send(sender); break;
				case NO_UPDATE: messageNoUpdate.send(sender); break;
				case UPDATE_AVAILABLE: messageUpdateAvailable.send(sender); break;
				default: messageUpdateFail.send(sender); break;
			}
		});
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		return null;
	}
}