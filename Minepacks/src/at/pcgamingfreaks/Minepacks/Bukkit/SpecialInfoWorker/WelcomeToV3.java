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
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Message.MessageFormat;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WelcomeToV3 extends SpecialInfoBase
{
	private List<Message> messages = new ArrayList<>();

	public WelcomeToV3(final Minepacks plugin)
	{
		super(plugin, Permissions.RELOAD);
		messages.add(new MessageBuilder().append("      ", MessageColor.AQUA, MessageFormat.UNDERLINE)
				.append("[", MessageColor.DARK_GRAY).append("Minepacks v3." + plugin.getVersion().getMinor(), MessageColor.YELLOW).append("]", MessageColor.DARK_GRAY)
				.append("      ", MessageColor.AQUA, MessageFormat.UNDERLINE).getMessage());
		messages.add(new MessageBuilder("Minepacks has been updated to " + plugin.getVersion() + "!").appendNewLine()
				.append("This update brings a lot of changes, so make sure to check the config file and update your language file.")
				.getMessage());
	}

	@Override
	protected void sendMessage(Player player)
	{
		messages.forEach(message -> message.send(player));
	}
}