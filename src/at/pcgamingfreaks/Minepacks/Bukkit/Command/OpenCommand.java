/*
 *   Copyright (C) 2018 GeorgH93
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
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class OpenCommand extends MinepacksCommand
{
	private final Message messageCooldown, messageWrongGameMode;
	private final String allowedGameModes, descriptionOpenOthers, helpParam;
	private final long cooldown;
	private final boolean syncCooldown;
	private final Minepacks plugin;

	public OpenCommand(Minepacks plugin)
	{
		super(plugin, "open", plugin.lang.getTranslated("Commands.Description.Backpack"), "backpack.use", true, plugin.getLanguage().getCommandAliases("Open"));
		this.plugin = plugin;

		messageCooldown        = plugin.lang.getMessage("Ingame.Open.Cooldown").replaceAll("\\{TimeLeft}", "%1\\$.1f");
		messageWrongGameMode   = plugin.lang.getMessage("Ingame.Open.WrongGameMode").replaceAll("\\{CurrentGameMode}", "%1\\$s").replaceAll("\\{AllowedGameModes}", "%1\\$s");
		descriptionOpenOthers = plugin.lang.getTranslated("Commands.Description.OpenOthers");
		helpParam = "<" + plugin.getLanguage().get("Commands.PlayerNameVariable") + ">";

		cooldown = plugin.config.getCommandCooldown();
		syncCooldown = plugin.config.isCommandCooldownSyncEnabled();

		StringBuilder allowedGameModesBuilder = new StringBuilder();
		for(GameMode gameMode : plugin.getConfiguration().getAllowedGameModes())
		{
			if(allowedGameModesBuilder.length() > 1)
			{
				allowedGameModesBuilder.append(", ");
			}
			allowedGameModesBuilder.append(gameMode.name().toLowerCase());
		}
		allowedGameModes = allowedGameModesBuilder.toString(); //TODO translate
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String main, @NotNull String s1, @NotNull String[] args)
	{
		Player player = (Player) sender;
		if(args.length == 0)
		{
			if(getMinepacksPlugin().isPlayerGameModeAllowed(player))
			{
				if(cooldown > 0 && !player.hasPermission("backpack.noCooldown"))
				{
					if(plugin.cooldowns.containsKey(player.getUniqueId()))
					{
						long cd = plugin.cooldowns.get(player.getUniqueId());
						if(cd < System.currentTimeMillis())
						{
							cd = cd - System.currentTimeMillis();
							messageCooldown.send(sender, cd / 1000f);
							return;
						}
					}
					final long cooldownTime = System.currentTimeMillis() + cooldown;
					if(syncCooldown)
					{
						plugin.getDatabase().syncCooldown(player, cooldownTime);
					}
					plugin.cooldowns.put(player.getUniqueId(), cooldownTime);
				}
				plugin.openBackpack(player, player, true);
			}
			else
			{
				messageWrongGameMode.send(player, player.getGameMode().name().toLowerCase(), allowedGameModes);
			}
		}
		else
		{
			if(player.hasPermission("backpack.others"))
			{
				plugin.openBackpack(player, Bukkit.getOfflinePlayer(args[0]), player.hasPermission("backpack.others.edit"));
			}
			else
			{
				plugin.messageNoPermission.send(player);
			}
		}
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(args.length > 0 && (!(commandSender instanceof Player) || commandSender.hasPermission("backpack.open.other")))
		{
			String name, arg = args[args.length - 1].toLowerCase();
			List<String> names = new LinkedList<>();
			for(Player player : Bukkit.getOnlinePlayers())
			{
				name = player.getName().toLowerCase();
				if(!name.equalsIgnoreCase(commandSender.getName()) && name.startsWith(arg)) names.add(name);
			}
			return names;
		}
		return null;
	}

	@Override
	public List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		List<HelpData> help = new LinkedList<>();
		help.add(new HelpData("", null, getDescription()));
		if(requester.hasPermission("backpack.open.other"))
		{
			//noinspection ConstantConditions
			help.add(new HelpData("", helpParam, descriptionOpenOthers));
		}
		return help;
	}
}