/*
 *   Copyright (C) 2022 GeorgH93
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
import at.pcgamingfreaks.Minepacks.Bukkit.ItemsCollector;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PickupCommand extends MinepacksCommand
{
    private final Minepacks plugin;
    private final Message toggleOn, toggleOff;

    public PickupCommand(Minepacks plugin)
    {
        super(plugin, "pickup", plugin.getLanguage().getTranslated("Commands.Description.Pickup"), Permissions.PICKUP_TOGGLE, true, plugin.getLanguage().getCommandAliases("Pickup"));

        this.plugin = plugin;
        toggleOn    = plugin.getLanguage().getMessage("Ingame.Pickup.ToggleOn");
        toggleOff   = plugin.getLanguage().getMessage("Ingame.Pickup.ToggleOff");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String s1, @NotNull String[] args)
    {
        Player player = (Player) sender;
        ItemsCollector collector = plugin.getItemsCollector();

        if (collector == null) return;

        if (collector.toggleState(player.getUniqueId()))
		{
            toggleOn.send(player);
        }
		else
        {
	        toggleOff.send(player);
        }
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String s1, @NotNull String[] strings)
    {
        return null;
    }

	@Override
	public boolean canUse(@NotNull CommandSender sender)
	{
		return super.canUse(sender) && sender.hasPermission(Permissions.FULL_PICKUP) && sender.hasPermission(Permissions.USE);
	}
}
