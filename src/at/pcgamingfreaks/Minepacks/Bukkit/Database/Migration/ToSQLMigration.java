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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Migration;

import at.pcgamingfreaks.Minepacks.Bukkit.Database.*;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.PluginLib.Bukkit.PluginLib;
import at.pcgamingfreaks.PluginLib.Database.DatabaseConnectionPool;
import at.pcgamingfreaks.Reflection;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
	protected static final DateFormat SQLITE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	protected final SQL newDb;
	protected final boolean uuid;

	protected ToSQLMigration(@NotNull Minepacks plugin, @NotNull Database oldDb, @NotNull String dbType, boolean global) throws Exception
	{
		super(plugin, oldDb);
		if(global)
		{
			newDb = (SQL) Reflection.getConstructor((dbType.equals("mysql")) ? MySQLShared.class : SQLiteShared.class, Minepacks.class, DatabaseConnectionPool.class).newInstance(plugin, PluginLib.getInstance().getDatabaseConnectionPool());
		}
		else
		{
			newDb = (SQL) Reflection.getConstructor((dbType.equals("mysql")) ? MySQL.class : SQLite.class, Minepacks.class).newInstance(plugin);
		}
		uuid = plugin.getConfiguration().getUseUUIDs();
	}

	protected  @Language("SQL") String replacePlaceholders(SQL database, @Language("SQL") String query) throws Exception
	{
		return (String) METHOD_REPLACE_PLACEHOLDERS.invoke(database, query);
	}
}