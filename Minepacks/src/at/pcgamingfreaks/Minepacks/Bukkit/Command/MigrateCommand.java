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

import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Migration.MigrationManager;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class MigrateCommand extends MinepacksCommand
{
	public MigrateCommand(Minepacks plugin)
	{
		super(plugin, "migrate", plugin.getLanguage().getTranslated("Commands.Description.Migrate"), Permissions.MIGRATE, plugin.getLanguage().getCommandAliases("migrate"));
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		if(args.length >= 1)
		{
			switch(args[0].toLowerCase(Locale.ROOT))
			{
				case "db": case "database": case "storage": migrateDb(sender, args); break;
			}
		}
		else
		{
			sender.sendMessage("/" + mainCommandAlias + ' ' + alias + "database");
		}
	}

	private void migrateDb(final @NotNull CommandSender sender, final @NotNull String[] args)
	{
		if(args.length >= 2)
		{
			if(args.length >= 3 && args[2].equalsIgnoreCase("start"))
			{
				MigrationManager migrationManager = new MigrationManager((Minepacks) getMinepacksPlugin());
				migrationManager.migrateDB(args[1], result -> {
					sender.sendMessage("Minepacks database migration result: " + result.getType().name());
					sender.sendMessage(result.getMessage());
				});
			}
			else
			{
				sender.sendMessage("This process will convert your storage type from " + ((Minepacks) getMinepacksPlugin()).getDatabase().getClass().getName() + " to " + args[1]);
				sender.sendMessage("Your old data will not be deleted and you can switch back any time in the \"config.yml\" file.");
				if(args[1].equalsIgnoreCase("mysql"))
				{
					sender.sendMessage("Please make sure that you have set the config options \"Host\", \"Database\", \"User\" and \"Password\" to the correct values.");
				}
				sender.sendMessage("To start the migration please confirm with: /backpack migrate " + args[0] + ' ' + args[1] + " start");
			}
		}
		else
		{
			sender.sendMessage("/backpacks migrate " + args[0] + " <mysql/sqlite/files/shared>");
		}
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		return null;
	}

	@Override
	public List<HelpData> getHelp(final @NotNull CommandSender requester)
	{
		return null;
	}
}