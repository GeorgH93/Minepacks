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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Migration;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.*;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Database;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.DatabaseType;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Reflection;

import org.bukkit.event.HandlerList;

public class MigrationManager
{
	private final Minepacks plugin;

	public MigrationManager(final Minepacks plugin)
	{
		this.plugin = plugin;
	}

	public void migrateDB(final String targetDatabaseType, final MigrationCallback callback)
	{
		final Migration migration = getMigrationPerformer(targetDatabaseType);
		if(migration == null)
		{
			callback.onResult(new MigrationResult("There is no need to migrate the database.", MigrationResult.MigrationResultType.NOT_NEEDED));
			return;
		}
		final Database db = plugin.getDatabase();

		//region Disable the plugin except for the database
		try
		{
			plugin.getLogger().info("Unloading plugin for migration");
			Reflection.setValue(plugin, "database", null); // Hack to prevent the unload of the database
			//noinspection ConstantConditions
			Reflection.getMethod(Minepacks.class, "unload").invoke(plugin); // Unload plugin
			HandlerList.unregisterAll(db); // Disable events for database
			Reflection.setValue(plugin, "database", db);
		}
		catch(Exception e)
		{
			plugin.getLogger().warning(ConsoleColor.RED + "Failed to unload plugin! Please restart your server!" + ConsoleColor.RESET);
			e.printStackTrace();
			callback.onResult(new MigrationResult("Failed to unload plugin! Please restart your server!", MigrationResult.MigrationResultType.ERROR));
			return;
		}
		//endregion
		//region Migrate data
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			MigrationResult migrationResult = null;
			try
			{
				plugin.getLogger().info("Start migrating data to new database");
				migrationResult = migration.migrate();
				plugin.getConfiguration().setDatabaseType(targetDatabaseType);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				plugin.getLogger().warning(ConsoleColor.RED + "There was a problem migrating from " + db.getClass().getName() + " to " + targetDatabaseType + ConsoleColor.RESET);
				callback.onResult(new MigrationResult("There was a problem migrating from " + db.getClass().getName() + " to " + targetDatabaseType + ". Please check the console for details.", MigrationResult.MigrationResultType.ERROR));
			}

			//region Start the plugin again
			final MigrationResult migrationResultFinal = migrationResult;
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				db.close();
				// No need to reload the config
				try
				{
					plugin.getLogger().info("Migration is done, loading the plugin again.");
					//noinspection ConstantConditions
					Reflection.getMethod(Minepacks.class, "load").invoke(plugin); // load the plugin again
					plugin.getLogger().info(ConsoleColor.GREEN + "Plugin loaded successful and is ready to use again." + ConsoleColor.RESET);
					if(migrationResultFinal != null) callback.onResult(migrationResultFinal);
				}
				catch(Exception e)
				{
					plugin.getLogger().warning(ConsoleColor.RED + "Failed to start plugin again!" + ConsoleColor.RESET);
					e.printStackTrace();
				}
			});
			//endregion
		});
		//endregion
	}

	public Migration getMigrationPerformer(String targetDatabaseType)
	{
		try
		{
			DatabaseType targetDbType = DatabaseType.fromName(targetDatabaseType);
			ConnectionProvider cp = targetDbType == DatabaseType.SHARED ? Database.getGlobalConnectionProvider(plugin.getLogger()) : null;
			boolean global = cp != null;
			if(cp != null) targetDbType = DatabaseType.fromName(cp.getDatabaseType());
			DatabaseBackend oldBackend = plugin.getDatabase().getBackend();
			switch(targetDbType)
			{
				case FILES:
					if(!(oldBackend instanceof SQL)) return null;
					return new SQLtoFilesMigration(plugin, (SQL) plugin.getDatabase().getBackend());
				case MYSQL:
					if(oldBackend instanceof MySQL) return null;
					if(oldBackend instanceof SQL) return new SQLtoSQLMigration(plugin, (SQL) oldBackend, DatabaseType.MYSQL, global);
					else return new FilesToSQLMigration(plugin, (Files) oldBackend, DatabaseType.MYSQL, global);
				case SQLITE:
					if(oldBackend instanceof SQLite) return null;
					if(oldBackend instanceof SQL) return new SQLtoSQLMigration(plugin, (SQL) oldBackend, DatabaseType.SQLITE, global);
					else return new FilesToSQLMigration(plugin, (Files) oldBackend, DatabaseType.SQLITE, global);
				default: plugin.getLogger().warning(String.format(Database.MESSAGE_UNKNOWN_DB_TYPE,  targetDatabaseType)); return null;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}