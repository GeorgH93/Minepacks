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
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Enums.ShrinkApproach;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.InventoryCompressor;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import at.pcgamingfreaks.StringUtils;
import at.pcgamingfreaks.Utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SortCommand extends MinepacksCommand
{
	private final Message messageSorted;
	private final ShrinkApproach defaultSortMethod;
	private final List<String> sortMethods;

	public SortCommand(final @NotNull Minepacks plugin)
	{
		super(plugin, "sort", plugin.getLanguage().getTranslated("Commands.Description.Sort"), Permissions.SORT, true, plugin.getLanguage().getCommandAliases("Sort"));
		defaultSortMethod = ShrinkApproach.SORT;
		sortMethods = Arrays.stream(ShrinkApproach.values()).map(Enum::name).collect(Collectors.toList());
		messageSorted = plugin.getLanguage().getMessage("Ingame.Sort.Sorted");
	}

	@Override
	public void execute(final @NotNull CommandSender commandSender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		final ShrinkApproach sortMethod = Utils.getEnum(args.length > 0 ? args[0] : "", defaultSortMethod);
		final Player player = (Player) commandSender;
		getMinepacksPlugin().getBackpack(player, backpack -> {
			InventoryCompressor compressor = new InventoryCompressor(backpack.getInventory().getContents());
			List<ItemStack> result = new ArrayList<>(0);
			switch(sortMethod)
			{
				case FAST: result = compressor.fast(); break;
				case COMPRESS: result = compressor.compress(); break;
				case SORT: result = compressor.sort(); break;
			}
			if(!result.isEmpty())
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
	public List<String> tabComplete(final @NotNull CommandSender commandSender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		if(args.length == 0) return null;
		return StringUtils.startsWithIgnoreCase(sortMethods, args[0]);
	}
}