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

import at.pcgamingfreaks.Minepacks.Bukkit.Database.SQL;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.SQLite;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("ConstantConditions")
public class SQLtoSQLMigration extends ToSQLMigration
{
	private final @Language("SQL") String queryInsertUsers, queryInsertBackpacks;

	protected SQLtoSQLMigration(@NotNull Minepacks plugin, @NotNull SQL oldDb, @NotNull String dbType, boolean global) throws Exception
	{
		super(plugin, oldDb, dbType, global);

		queryInsertUsers = replacePlaceholders(newDb, "INSERT INTO {TablePlayers} ({FieldPlayerID},{FieldName},{FieldUUID}) VALUES (?,?,?);");
		queryInsertBackpacks = replacePlaceholders(newDb, "INSERT INTO {TableBackpacks} ({FieldBPOwner},{FieldBPITS},{FieldBPVersion},{FieldBPLastUpdate}) VALUES (?,?,?,?);");
	}

	@Override
	public @Nullable MigrationResult migrate() throws Exception
	{
		try(Connection readConnection = ((SQL) oldDb).getConnection(); Connection writeConnection = newDb.getConnection(); Statement readStatement = readConnection.createStatement())
		{
			int users = migrate("users", writeConnection, readStatement, "SELECT * FROM {TablePlayers};", queryInsertUsers);
			int backpacks = migrate("backpacks", writeConnection, readStatement, "SELECT * FROM {TableBackpacks};", queryInsertBackpacks);
			return new MigrationResult("Migrated " + users + " users and " + backpacks + " backpacks from " + oldDb.getClass().getSimpleName() + " to " + newDb.getClass().getSimpleName() + ".", MigrationResult.MigrationResultType.SUCCESS);
		}
		finally
		{
			newDb.close();
		}
	}

	private int migrate(@NotNull String type, @NotNull Connection writeConnection, @NotNull Statement readStatement, @Language("SQL") String readQuery, @Language("SQL") String insertQuery) throws Exception
	{
		int count = 0;
		byte mode = (byte) ((type.equals("users")) ? 0 : 1);
		plugin.getLogger().info("Migrate " + type + " ...");
		try(ResultSet resultSet = readStatement.executeQuery(replacePlaceholders((SQL) oldDb, readQuery));
		    PreparedStatement preparedStatement = writeConnection.prepareStatement(replacePlaceholders(newDb, insertQuery)))
		{
			while(resultSet.next())
			{
				switch(mode)
				{
					case 0: migrateUser(resultSet, preparedStatement); break;
					case 1: migrateBackpack(resultSet, preparedStatement); break;
				}
				preparedStatement.addBatch();
				count++;
			}
			preparedStatement.executeBatch();
		}
		plugin.getLogger().info("Migrated " + count + " " + type + ".");
		return count;
	}

	private void migrateUser(@NotNull ResultSet usersResultSet, @NotNull PreparedStatement preparedStatement) throws Exception
	{
		int userId = usersResultSet.getInt((String) FIELD_PLAYER_ID.get(oldDb));
		preparedStatement.setInt(1, userId);
		preparedStatement.setString(2, usersResultSet.getString((String) FIELD_PLAYER_NAME.get(oldDb)));
		preparedStatement.setString(3, usersResultSet.getString((String) FIELD_PLAYER_UUID.get(oldDb)));
	}

	private void migrateBackpack(@NotNull ResultSet backpacksResultSet, @NotNull PreparedStatement preparedStatement) throws Exception
	{
		preparedStatement.setInt(1, backpacksResultSet.getInt((String) FIELD_BP_OWNER.get(oldDb)));
		preparedStatement.setBytes(2, backpacksResultSet.getBytes((String) FIELD_BP_ITS.get(oldDb)));
		preparedStatement.setInt(3, backpacksResultSet.getInt((String) FIELD_BP_VERSION.get(oldDb)));
		final DateFormat sqliteDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		if(oldDb instanceof SQLite)
		{
			preparedStatement.setTimestamp(4, new Timestamp(sqliteDateFormat.parse(backpacksResultSet.getString((String) FIELD_BP_LAST_UPDATE.get(oldDb))).getTime()));
		}
		else
		{
			preparedStatement.setString(4, sqliteDateFormat.format(new Date(backpacksResultSet.getTimestamp((String) FIELD_BP_LAST_UPDATE.get(oldDb)).getTime())));
		}
	}
}