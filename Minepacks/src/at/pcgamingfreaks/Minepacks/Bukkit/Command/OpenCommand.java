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

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.Placeholder.Processors.GameModePlaceholderProcessor;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Calendar.TimeSpan;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.Message.MessageClickEvent;
import at.pcgamingfreaks.Message.Placeholder.Processors.FloatPlaceholderProcessor;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OpenCommand extends MinepacksCommand
{
	private final Message messageCooldown, messageWrongGameMode;
	private final String descriptionOpenOthers, helpParam;
	private final Minepacks plugin;

	public OpenCommand(Minepacks plugin)
	{
		super(plugin, "open", plugin.getLanguage().getTranslated("Commands.Description.Backpack"), Permissions.USE, false, plugin.getLanguage().getCommandAliases("Open"));
		this.plugin = plugin;

		messageCooldown       = plugin.getLanguage().getMessage("Ingame.Open.Cooldown").placeholder("TimeLeft", new FloatPlaceholderProcessor(1)).placeholder("TimeSpanLeft");
		messageWrongGameMode  = plugin.getLanguage().getMessage("Ingame.Open.WrongGameMode").staticPlaceholder("AllowedGameModes", new GameModePlaceholderProcessor(), plugin.getConfiguration().getAllowedGameModes()).placeholder("CurrentGameMode", new GameModePlaceholderProcessor());
		descriptionOpenOthers = plugin.getLanguage().getTranslated("Commands.Description.OpenOthers");
		helpParam = "<" + plugin.getLanguage().get("Commands.PlayerNameVariable") + ">";
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String main, @NotNull String s1, @NotNull String[] args)
	{
		if (!(sender instanceof Player))
		{
			handleOpenFromConsole(sender, args);
			return;
		}
		Player player = (Player) sender;
		if(args.length == 0)
		{
			executeSelf(player);
		}
		else
		{
			executeOther(player, args[0]);
		}
	}

	void executeSelf(Player player)
	{
		if(getMinepacksPlugin().isPlayerGameModeAllowed(player))
		{
			if(plugin.getCooldownManager() != null && !player.hasPermission(Permissions.NO_COOLDOWN))
			{
				long cd = plugin.getCooldownManager().getRemainingCooldown(player);
				if(cd > 0)
				{
					TimeSpan ts = TimeSpan.fromMilliseconds(cd);
					messageCooldown.send(player, cd / 1000f, ts.toString());
					return;
				}
				plugin.getCooldownManager().setCooldown(player);
			}
			plugin.openBackpack(player, player, true);
		}
		else
		{
			messageWrongGameMode.send(player, player.getGameMode());
		}
	}

	void executeOther(Player player, String name)
	{
		if(player.hasPermission(Permissions.OTHERS))
		{
			OfflinePlayer target = Bukkit.getPlayer(name);
			if(target == null)
			{
				//noinspection deprecation
				target = Bukkit.getOfflinePlayer(name);
			}
			plugin.openBackpack(player, target, player.hasPermission(Permissions.OTHERS_EDIT));
		}
		else
		{
			plugin.messageNoPermission.send(player);
		}
	}

	void handleOpenFromConsole(final @NotNull CommandSender sender, final @NotNull String[] names)
	{
		int opened = 0;
		List<String> notOnline = new ArrayList<>(names.length);
		for(String name : names)
		{
			Player target = Bukkit.getPlayer(name);
			if (target == null)
			{
				notOnline.add(name);
			}
			else if (target.hasPermission(Permissions.USE))
			{
				opened++;
				plugin.openBackpack(target, target, true);
			}
		}
		sender.sendMessage("Opened backpack of " + opened + " players." + (notOnline.isEmpty() ? "" : " Not online: " + String.join(", ", notOnline)));
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length > 0 && (!(commandSender instanceof Player) || commandSender.hasPermission(Permissions.OTHERS)))
		{
			return Utils.getPlayerNamesStartingWith(args[args.length - 1], commandSender);
		}
		return null;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new ArrayList<>(2);
		help.add(new HelpData(getTranslatedName(), null, getDescription(), MessageClickEvent.ClickEventAction.RUN_COMMAND));
		if(requester.hasPermission(Permissions.OTHERS))
		{
			help.add(new HelpData(getTranslatedName(), helpParam, descriptionOpenOthers));
		}
		return help;
	}
}