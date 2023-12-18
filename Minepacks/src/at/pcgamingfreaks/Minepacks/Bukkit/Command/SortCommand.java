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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Command;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.InventoryCompressor;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SortCommand extends MinepacksCommand
{
	private final Message messageSorted;

	public SortCommand(Minepacks plugin)
	{
		super(plugin, "sort", plugin.getLanguage().getTranslated("Commands.Description.Sort"), Permissions.SORT, true, plugin.getLanguage().getCommandAliases("Sort"));
		messageSorted = plugin.getLanguage().getMessage("Ingame.Sort.Sorted");
	}

	@Override
	public void execute(final @NotNull CommandSender commandSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		final Player player = (Player) commandSender;
		getMinepacksPlugin().getBackpack(player, backpack -> {
			InventoryCompressor compressor = new InventoryCompressor(backpack.getInventory().getContents());
			if(!compressor.sort().isEmpty())
			{
				plugin.getLogger().warning("Failed to sort backpack!"); //this should not happen
				return;
			}
			backpack.getInventory().setContents(compressor.getTargetStacks());
			backpack.setChanged();
			messageSorted.send(player);
		});
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		return null;
	}
}