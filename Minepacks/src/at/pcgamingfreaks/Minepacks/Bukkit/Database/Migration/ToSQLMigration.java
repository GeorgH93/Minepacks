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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Migration;

import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Database;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.MySQL;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.SQL;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.SQLite;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Reflection;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("ConstantConditions")
public abstract class ToSQLMigration extends Migration
{
	protected static final Method METHOD_REPLACE_PLACEHOLDERS = Reflection.getMethod(SQL.class, "replacePlaceholders", String.class);
	protected static final Field FIELD_PLAYER_ID   = Reflection.getField(SQL.class, "fieldPlayerID");
	protected static final Field FIELD_PLAYER_UUID = Reflection.getField(SQL.class, "fieldPlayerUUID");
	protected static final Field FIELD_PLAYER_NAME = Reflection.getField(SQL.class, "fieldPlayerName");
	protected static final Field FIELD_BP_OWNER       = Reflection.getField(SQL.class, "fieldBpOwner");
	protected static final Field FIELD_BP_ITS         = Reflection.getField(SQL.class, "fieldBpIts");
	protected static final Field FIELD_BP_VERSION     = Reflection.getField(SQL.class, "fieldBpVersion");
	protected static final Field FIELD_BP_LAST_UPDATE = Reflection.getField(SQL.class, "fieldBpLastUpdate");

	protected final SQL newDb;

	protected ToSQLMigration(@NotNull Minepacks plugin, @NotNull Database oldDb, @NotNull String dbType, boolean global)
	{
		super(plugin, oldDb);
		/*if[STANDALONE]
		ConnectionProvider connectionProvider = null;
		else[STANDALONE]*/
		ConnectionProvider connectionProvider = (global) ? at.pcgamingfreaks.PluginLib.Bukkit.PluginLib.getInstance().getDatabaseConnectionPool().getConnectionProvider() : null;
		/*end[STANDALONE]*/
		switch(dbType)
		{
			case "mysql": newDb = new MySQL(plugin, connectionProvider); break;
			case "sqlite":
				final File dbFile = new File(SQLite.getDbFile(plugin));
				if(dbFile.exists() && !dbFile.renameTo(new File(SQLite.getDbFile(plugin) + ".old_" + System.currentTimeMillis())))
				{
					plugin.getLogger().warning("Failed to rename old database file.");
				}
				newDb = new SQLite(plugin, connectionProvider);
				break;
			default: newDb = null;
		}
	}

	protected  @Language("SQL") String replacePlaceholders(SQL database, @Language("SQL") String query) throws Exception
	{
		return (String) METHOD_REPLACE_PLACEHOLDERS.invoke(database, query);
	}
}