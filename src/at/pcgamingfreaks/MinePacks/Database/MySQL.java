/*
 *   Copyright (C) 2014-2018 GeorgH93
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

package at.pcgamingfreaks.MinePacks.Database;

import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.MinePacks.MinePacks;

import com.zaxxer.hikari.HikariConfig;

import java.sql.*;

public class MySQL extends SQL
{
	public MySQL(MinePacks mp)
	{
		super(mp); // Load Settings
	}

	@Override
	protected HikariConfig getPoolConfig()
	{
		HikariConfig poolConfig = new HikariConfig();
		poolConfig.setJdbcUrl("jdbc:mysql://" + plugin.config.getMySQLHost() + "/" + plugin.config.getMySQLDatabase() + "?allowMultiQueries=true&autoReconnect=true" + plugin.config.getMySQLProperties());
		poolConfig.setUsername(plugin.config.getMySQLUser());
		poolConfig.setPassword(plugin.config.getMySQLPassword());
		poolConfig.setMinimumIdle(1);
		poolConfig.setMaximumPoolSize(plugin.config.getMySQLMaxConnections());
		return poolConfig;
	}

	@Override
	protected void updateQuerysForDialect()
	{
		queryDeleteOldBackpacks = "DELETE FROM `{TableBackpacks}` WHERE `{FieldBPLastUpdate}` + INTERVAL {VarMaxAge} day < NOW()";
		queryUpdateBP = queryUpdateBP.replaceAll("\\{NOW}", "NOW()");
	}

	@Override
	protected void checkDB()
	{
		try(Connection connection = getConnection())
		{
			if(useUUIDs)
			{
				if(useUUIDSeparators)
				{
					DBTools.updateDB(connection, "CREATE TABLE `" + tablePlayers + "` (\n`" + fieldPlayerID + "` INT UNSIGNED NOT NULL AUTO_INCREMENT,\n`" + fieldName + "` VARCHAR(16) NOT NULL,\n`"
							+ fieldUUID + "` CHAR(36) DEFAULT NULL,\nPRIMARY KEY (`" + fieldPlayerID + "`),\nUNIQUE INDEX `" + fieldUUID + "_UNIQUE` (`" + fieldUUID + "`)\n);");
				}
				else
				{
					// Check if table exists
					validateUUIDColumn(connection);
					DBTools.updateDB(connection, "CREATE TABLE `" + tablePlayers + "` (\n`" + fieldPlayerID + "` INT UNSIGNED NOT NULL AUTO_INCREMENT,\n`" + fieldName + "` VARCHAR(16) NOT NULL,\n`"
							+ fieldUUID + "` CHAR(32) DEFAULT NULL,\nPRIMARY KEY (`" + fieldPlayerID + "`),\nUNIQUE INDEX `" + fieldUUID + "_UNIQUE` (`" + fieldUUID + "`)\n);");
				}
			}
			else
			{
				DBTools.updateDB(connection, "CREATE TABLE `" + tablePlayers + "` (\n`" + fieldPlayerID + "` INT UNSIGNED NOT NULL AUTO_INCREMENT,\n`" + fieldName + "` CHAR(16) NOT NULL,\n"
						+ "PRIMARY KEY (`" + fieldPlayerID + "`),\nUNIQUE INDEX `" + fieldName + "_UNIQUE` (`" + fieldName + "`)\n);");
			}
			DBTools.updateDB(connection, "CREATE TABLE `" + tableBackpacks + "` (\n`" + fieldBPOwner + "` INT UNSIGNED NOT NULL,\n`" + fieldBPITS + "` BLOB,\n`"
					+ fieldBPVersion + "` INT DEFAULT 0,\n" + ((maxAge > 0) ? "`" + fieldBPLastUpdate + "` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" : "") + "PRIMARY KEY (`" + fieldBPOwner + "`)\n);");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void validateUUIDColumn(Connection connection)
	{
		try(Statement statement = connection.createStatement())
		{
			try(ResultSet rs = statement.executeQuery("SELECT count(*) FROM information_schema.tables WHERE table_schema = '" + plugin.config.getMySQLDatabase() + "' AND table_name = '" + tablePlayers + "';"))
			{
				rs.next();
				if(rs.getInt(1) < 1) return; // Table doesn't exist
			}
			try(ResultSet rs = statement.executeQuery("SHOW COLUMNS FROM `" + tablePlayers + "` LIKE '" + fieldUUID + "';"))
			{
				if(!rs.next()) return; // UUID column doesn't exist yet
				if(rs.getString("Type").equalsIgnoreCase("char(32)")) return; // Column is already only 32 chars long, there is no need to shorten any uuids
			}
			String query = "SELECT `{FieldPlayerID}`,`{FieldUUID}` FROM `{TablePlayers}` WHERE `{FieldUUID}` LIKE '%-%';".replaceAll("\\{TablePlayers}", tablePlayers).replaceAll("\\{FieldUUID}", fieldUUID).replaceAll("\\{FieldPlayerID}", fieldPlayerID);
			try(ResultSet rs = statement.executeQuery(query); PreparedStatement fixStatement = connection.prepareStatement(queryFixUUIDs))
			{
				while(rs.next())
				{
					fixStatement.setString(1, rs.getString(fieldUUID).replaceAll("-", ""));
					fixStatement.setInt(2, rs.getInt(fieldPlayerID));
					fixStatement.addBatch();
				}
				fixStatement.executeBatch();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
