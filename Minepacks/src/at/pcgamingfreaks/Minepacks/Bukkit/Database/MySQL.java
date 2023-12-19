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

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Database.ConnectionProvider.MySQLConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQL extends SQL
{
	public MySQL(@NotNull Minepacks plugin, @Nullable ConnectionProvider connectionProvider)
	{
		super(plugin, (connectionProvider == null) ? new MySQLConnectionProvider(plugin.getLogger(), plugin.getDescription().getName(), plugin.getConfiguration()) : connectionProvider);
	}

	@Override
	protected void updateQueriesForDialect()
	{
		queryDeleteOldBackpacks = "DELETE FROM {TableBackpacks} WHERE {FieldBPLastUpdate} + INTERVAL {VarMaxAge} day < NOW()";
		queryUpdateBp = queryUpdateBp.replace("{NOW}", "NOW()");
	}

	@Override
	protected void checkDB()
	{
		try(Connection connection = getConnection())
		{
			DBTools.updateDB(connection, replacePlaceholders("CREATE TABLE IF NOT EXISTS {TablePlayers} (\n{FieldPlayerID} INT UNSIGNED NOT NULL AUTO_INCREMENT,\n{FieldName} VARCHAR(16) NOT NULL,\n" +
					                                                 "{FieldUUID} CHAR(" +  ((useUUIDSeparators) ? "36" : "32") + ") DEFAULT NULL," + "\nPRIMARY KEY ({FieldPlayerID}),\n" +
					                                                 "UNIQUE INDEX {FieldUUID}_UNIQUE ({FieldUUID})\n);"));
			DBTools.updateDB(connection, replacePlaceholders("CREATE TABLE IF NOT EXISTS {TableBackpacks} (\n{FieldBPOwner} INT UNSIGNED NOT NULL,\n{FieldBPITS} MEDIUMBLOB,\n{FieldBPVersion} INT DEFAULT 0,\n" +
					                                                 "{FieldBPLastUpdate} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
					                                                 "PRIMARY KEY ({FieldBPOwner}),\nCONSTRAINT fk_{TableBackpacks}_{TablePlayers}_{FieldBPOwner} FOREIGN KEY ({FieldBPOwner}) " +
					                                                 "REFERENCES {TablePlayers} ({FieldPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE\n);"));
			if(syncCooldown)
			{
				DBTools.updateDB(connection, replacePlaceholders("CREATE TABLE IF NOT EXISTS {TableCooldowns} (\n{FieldCDPlayer} INT UNSIGNED NOT NULL,\n{FieldCDTime} DATETIME NOT NULL,\nPRIMARY KEY ({FieldCDPlayer}),\n" +
						                                                 "CONSTRAINT fk_{TableCooldowns}_{TablePlayers}_{FieldCDPlayer} FOREIGN KEY ({FieldCDPlayer}) " +
						                                                 "REFERENCES {TablePlayers} ({FieldPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE\n);"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}