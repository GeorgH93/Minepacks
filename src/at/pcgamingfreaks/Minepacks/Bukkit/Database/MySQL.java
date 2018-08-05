/*
 *   Copyright (C) 2016-2018 GeorgH93
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

import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import com.zaxxer.hikari.HikariConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQL extends SQL
{
	//TODO add cooldown sync table
	public MySQL(Minepacks plugin)
	{
		super(plugin); // Load Settings
	}

	@Override
	protected HikariConfig getPoolConfig()
	{
		HikariConfig poolConfig = new HikariConfig();
		poolConfig.setJdbcUrl("jdbc:mysql://" + plugin.getConfiguration().getSQLHost() + "/" + plugin.getConfiguration().getSQLDatabase() + "?allowMultiQueries=true&autoReconnect=true" + plugin.getConfiguration().getSQLProperties());
		poolConfig.setUsername(plugin.getConfiguration().getSQLUser());
		poolConfig.setPassword(plugin.getConfiguration().getSQLPassword());
		poolConfig.setMinimumIdle(1);
		poolConfig.setMaximumPoolSize(plugin.getConfiguration().getSQLMaxConnections());
		return poolConfig;
	}

	@Override
	protected void updateQuerysForDialect()
	{
		queryDeleteOldBackpacks = "DELETE FROM `{TableBackpacks}` WHERE `{FieldBPLastUpdate}` + INTERVAL {VarMaxAge} day < NOW()";
		queryUpdateBp = queryUpdateBp.replaceAll("\\{NOW}", "NOW()");
	}

	@Override
	protected void checkDB()
	{
		try(Connection connection = getConnection())
		{
			DBTools.updateDB(connection, replacePlaceholders("CREATE TABLE {TablePlayers} (\n{FieldPlayerID} INT UNSIGNED NOT NULL AUTO_INCREMENT,\n{FieldName} CHAR(16) NOT NULL,\n{FieldUUID} CHAR(36) DEFAULT NULL," +
					                                                 "\nPRIMARY KEY ({FieldPlayerID}),\nUNIQUE INDEX {FieldUUID}_UNIQUE ({FieldUUID})\n);"));
			DBTools.updateDB(connection, replacePlaceholders("CREATE TABLE {TableBackpacks} (\n{FieldBPOwner} INT UNSIGNED NOT NULL,\n{FieldBPITS} BLOB,\n{FieldBPVersion} INT DEFAULT 0,\n" +
					                                                 "{FieldBPLastUpdate} TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
					                                                 "PRIMARY KEY ({FieldBPOwner}),\nCONSTRAINT fk_{TableBackpacks}_{TablePlayers}_{FieldBPOwner} FOREIGN KEY ({FieldBPOwner}) " +
					                                                 "REFERENCES {TablePlayers} ({FieldPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE\n);"));
			DBTools.updateDB(connection, replacePlaceholders("CREATE TABLE {TableCooldowns} (\n{FieldCDPlayer} INT UNSIGNED,\n{FieldCDTime} LONG NOT NULL,\nPRIMARY KEY ({FieldCDPlayer}),\n" +
					                                                 "CONSTRAINT fk_{TableCooldowns}_{TablePlayers}_{FieldCDPlayer} FOREIGN KEY ({FieldCDPlayer}) " +
					                                                 "REFERENCES {TablePlayers} ({FieldPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE\n);"));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}