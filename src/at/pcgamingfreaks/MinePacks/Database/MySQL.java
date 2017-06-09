/*
 *   Copyright (C) 2014-2017 GeorgH93
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

import at.pcgamingfreaks.MinePacks.MinePacks;

import com.zaxxer.hikari.HikariConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
		queryUpdateBP = queryUpdateBP.replaceAll("\\{NOW\\}", "NOW()");
	}

	@Override
	protected void checkDB()
	{
		try(Connection connection = getConnection(); Statement stmt = connection.createStatement())
		{
			ResultSet res;
			if(useUUIDs)
			{
				stmt.execute("CREATE TABLE IF NOT EXISTS `" + tablePlayers + "` (`" + fieldPlayerID + "` INT UNSIGNED NOT NULL AUTO_INCREMENT,`" + fieldName + "` CHAR(16) NOT NULL,`" + fieldUUID + "` CHAR(36) UNIQUE, PRIMARY KEY (`" + fieldPlayerID + "`));");
				res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tablePlayers + "' AND COLUMN_NAME = '" + fieldUUID + "';");
				if(!res.next())
				{
					stmt.execute("ALTER TABLE `" + tablePlayers + "` ADD COLUMN `" + fieldUUID + "` CHAR(36) UNIQUE;");
				}
				res.close();
				res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tablePlayers + "' AND COLUMN_NAME = '" + fieldName + "' AND COLUMN_KEY='UNI';");
				if(res.next())
				{
					stmt.execute("ALTER TABLE `" + tablePlayers + "` DROP INDEX `" + fieldName + "_UNIQUE`;");
				}
				res.close();
				if(useUUIDSeparators)
				{
					res = stmt.executeQuery("SELECT CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tablePlayers + "' AND COLUMN_NAME = '" + fieldUUID + "';");
					if(res.next() && res.getInt(1) < 36)
					{
						stmt.execute("ALTER TABLE `" + tablePlayers + "` MODIFY `" + fieldUUID + "` CHAR(36) UNIQUE;");
					}
					res.close();
				}
			}
			else
			{
				stmt.execute("CREATE TABLE IF NOT EXISTS `" + tablePlayers + "` (`" + fieldPlayerID + "` INT UNSIGNED NOT NULL AUTO_INCREMENT,`" + fieldName + "` CHAR(16) NOT NULL, PRIMARY KEY (`" + fieldPlayerID + "`));");
				res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tablePlayers + "' AND COLUMN_NAME = '" + fieldName + "' AND COLUMN_KEY='UNI';");
				if(!res.next())
				{
					stmt.execute("ALTER TABLE `" + tablePlayers + "` ADD UNIQUE INDEX `" + fieldName + "_UNIQUE` (`" + fieldName + "` ASC);");
				}
				res.close();
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + tableBackpacks + "` (`" + fieldBPOwner + "` INT UNSIGNED NOT NULL, `" + fieldBPITS + "` BLOB, `"
					+ fieldBPVersion + "` INT DEFAULT 0, " + ((maxAge > 0) ? "`" + fieldBPLastUpdate + "` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " : "") + "PRIMARY KEY (`" + fieldBPOwner + "`));");
			res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tableBackpacks + "' AND COLUMN_NAME = '" + fieldBPVersion + "';");
			if(!res.next())
			{
				stmt.execute("ALTER TABLE `" + tableBackpacks + "` ADD COLUMN `" + fieldBPVersion + "` INT DEFAULT 0;");
			}
			if(maxAge > 0)
			{
				res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tableBackpacks + "' AND COLUMN_NAME = '" + fieldBPLastUpdate + "';");
				if(!res.next())
				{
					stmt.execute("ALTER TABLE `" + tableBackpacks + "` ADD COLUMN `" + fieldBPLastUpdate + "` TIMESTAMP DEFAULT CURRENT_TIMESTAMP;");
				}
				res.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}