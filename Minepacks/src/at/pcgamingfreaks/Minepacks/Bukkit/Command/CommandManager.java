/*
 *   Copyright (C) 2024 GeorgH93
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

import at.pcgamingfreaks.Bukkit.Command.CommandExecutorWithSubCommandsGeneric;
import at.pcgamingfreaks.Bukkit.Command.RegisterablePluginCommand;
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommandManager;
import at.pcgamingfreaks.Minepacks.Bukkit.API.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Reflection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class CommandManager extends CommandExecutorWithSubCommandsGeneric<MinepacksCommand> /*if_not[STANDALONE]*/ implements MinepacksCommandManager /*end[STANDALONE]*/
{
	private final Minepacks plugin;
	private final RegisterablePluginCommand backpackCommand;
	private final Message helpFormat;

	public CommandManager(@NotNull Minepacks plugin)
	{
		this.plugin = plugin;
		// Registering the backpack command with the translated aliases
		backpackCommand = new RegisterablePluginCommand(plugin, "backpack", plugin.getLanguage().getCommandAliases("Backpack"));
		backpackCommand.registerCommand();
		backpackCommand.setExecutor(this);
		backpackCommand.setTabCompleter(this);

		//TODO handle click action placeholder
		helpFormat = plugin.getLanguage().getMessage("Commands.HelpFormat").placeholder("MainCommand").placeholder("SubCommand").placeholder("Parameters").placeholder("Description");//.placeholder("suggest_command", "%5\\$s");

		// Setting the help format for the marry commands as well as the no permissions and not from console message
		try
		{
			// Show help function
			Reflection.setStaticField(MinepacksCommand.class, "minepacksPlugin", plugin); // Plugin instance
			Reflection.setStaticField(MinepacksCommand.class, "minepacksCommandManager", this); // Command manager instance
			Reflection.setStaticField(MinepacksCommand.class, "showHelp", this.getClass().getDeclaredMethod("sendHelp", CommandSender.class, String.class, Collection.class));
			Reflection.setStaticField(MinepacksCommand.class, "messageNoPermission", plugin.messageNoPermission); // No permission message
			Reflection.setStaticField(MinepacksCommand.class, "messageNotFromConsole", plugin.messageNotFromConsole); // Not from console message
		}
		catch(Exception e)
		{
			plugin.getLogger().log(Level.SEVERE, e, () -> ConsoleColor.RED + "Unable to set the help format. Default format will be used.\nMore details:" + ConsoleColor.RESET);
		}

		// Init backpack commands
		defaultSubCommand = new OpenCommand(plugin);
		registerSubCommand(defaultSubCommand);
		if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_8)) registerSubCommand(new SortCommand(plugin));
		registerSubCommand(new ClearCommand(plugin));
		registerSubCommand(new ReloadCommand(plugin));
		registerSubCommand(new UpdateCommand(plugin));
		registerSubCommand(new BackupCommand(plugin));
		registerSubCommand(new RestoreCommand(plugin));
		registerSubCommand(new MigrateCommand(plugin));
		registerSubCommand(new VersionCommand(plugin));
		if (plugin.getConfiguration().isFullInvToggleAllowed()) registerSubCommand(new PickupCommand(plugin));
		registerSubCommand(new DebugCommand(plugin));
		registerSubCommand(new HelpCommand(plugin, commands, this));
	}

	@Override
	public void close()
	{
		backpackCommand.unregisterCommand();
		super.close();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args)
	{
		if(sender instanceof Player)
		{
			WorldBlacklistMode disabled = plugin.isDisabled((Player) sender);
			if(disabled != WorldBlacklistMode.None)
			{
				switch(disabled)
				{
					case Message: plugin.messageWorldDisabled.send(sender); break;
					case MissingPermission: plugin.messageNoPermission.send(sender); break;
					case NoPlugin: return false;
				}
				return true;
			}
		}
		else if(args.length == 0) // If the command was executed in the console without parameters
		{
			args = new String[]{"help"}; // Show help
		}
		return super.onCommand(sender, command, alias, args);
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args)
	{
		if(sender instanceof Player)
		{
			WorldBlacklistMode disabled = plugin.isDisabled((Player) sender);
			if(disabled != WorldBlacklistMode.None)
			{
				if (disabled == WorldBlacklistMode.Message) plugin.messageWorldDisabled.send(sender);
				else if (disabled == WorldBlacklistMode.MissingPermission) plugin.messageNoPermission.send(sender);
				return null;
			}
		}
		return super.onTabComplete(sender, command, alias, args);
	}

	public void sendHelp(CommandSender target, String mainCommandAlias, Collection<HelpData> data)
	{
		for(HelpData d : data)
		{
			helpFormat.send(target, mainCommandAlias, d.getTranslatedSubCommand(), d.getParameter(), d.getDescription(), d.getClickAction().name().toLowerCase(Locale.ROOT));
		}
	}
}