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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend;

import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Database.ConnectionProvider.MySQLConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQL extends SQL
{
	public MySQL(final @NotNull Minepacks plugin, final @Nullable ConnectionProvider connectionProvider) throws SQLException
	{
		super(plugin, (connectionProvider == null) ? new MySQLConnectionProvider(plugin.getLogger(), plugin.getDescription().getName(), plugin.getConfiguration()) : connectionProvider);
	}

	@Override
	protected void updateQueriesForDialect()
	{
		queryDeleteOldBackpacks = "DELETE FROM {TableBackpacks} WHERE {FieldBPLastUpdate} + INTERVAL {VarMaxAge} day < NOW()";
		queryUpdateBp = queryUpdateBp.replaceAll("\\{NOW}", "NOW()");

		// Prevent auto id incrementing if no insert was performed
		queryInsertPlayer = "INSERT IGNORE INTO {TablePlayers} ({FieldName},{FieldUUID}) SELECT ?,? FROM (SELECT 1) AS `tmp` WHERE NOT EXISTS (SELECT {FieldUUID} FROM {TablePlayers} WHERE {FieldUUID}=?);";
	}

	void mkTable(final @NotNull Connection connection, final @NotNull @Language("SQL") String createStatement) throws SQLException
	{
		DBTools.updateDB(connection, replacePlaceholders(createStatement));
	}

	@Override
	protected void checkDB()
	{
		try(Connection connection = getConnection())
		{
			mkTable(connection, "CREATE TABLE IF NOT EXISTS {TablePlayers} (\n{FieldPlayerID} INT UNSIGNED NOT NULL AUTO_INCREMENT,\n{FieldName} VARCHAR(16) NOT NULL,\n{FieldUUID} CHAR(" +  ((useUUIDSeparators) ? "36" : "32") + ") DEFAULT NULL," +
								"\nPRIMARY KEY ({FieldPlayerID}),\nUNIQUE INDEX {FieldUUID}_UNIQUE ({FieldUUID})\n) ENGINE=InnoDB;");
			mkTable(connection, "CREATE TABLE IF NOT EXISTS {TableBackpacks} (\n{FieldBPOwner} INT UNSIGNED NOT NULL,\n{FieldBPITS} BLOB,\n{FieldBPVersion} INT NOT NULL DEFAULT 0,\n" +
                                "{FieldBPLastUpdate} TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\nPRIMARY KEY ({FieldBPOwner}),\n" +
								"CONSTRAINT fk_{TableBackpacks}_{TablePlayers}_{FieldBPOwner} FOREIGN KEY ({FieldBPOwner}) REFERENCES {TablePlayers} ({FieldPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE\n) ENGINE=InnoDB;");
			if(syncCooldown)
			{
				mkTable(connection, "CREATE TABLE IF NOT EXISTS {TableCooldowns} (\n{FieldCDPlayer} INT UNSIGNED NOT NULL,\n{FieldCDTime} DATETIME NOT NULL,\nPRIMARY KEY ({FieldCDPlayer}),\n" +
									"CONSTRAINT fk_{TableCooldowns}_{TablePlayers}_{FieldCDPlayer} FOREIGN KEY ({FieldCDPlayer}) REFERENCES {TablePlayers} ({FieldPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE\n) ENGINE=InnoDB;");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}