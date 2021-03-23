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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.SpecialInfoWorker;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.MessageBuilder;
import at.pcgamingfreaks.Message.MessageClickEvent;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NoUpgradesFromV1 extends SpecialInfoBase
{
	final Message message;

	public NoUpgradesFromV1(final JavaPlugin plugin)
	{
		super(plugin, Permissions.RELOAD);
		message = new MessageBuilder("Upgrading from Minepacks v1.x is not possible!", MessageColor.RED).appendNewLine().append("Please install ", MessageColor.RED)
				.append("v2.3.24").onClick(MessageClickEvent.ClickEventAction.OPEN_URL, "https://www.spigotmc.org/resources/minepacks-backpack-plugin-mc-1-7-1-16.19286/download?version=395380")
				.append(" first!", MessageColor.RED).appendNewLine().append("Or delete all your data (database + config)", MessageColor.RED).getMessage();
	}

	@Override
	protected void sendMessage(Player player)
	{
		message.send(player);
	}
}