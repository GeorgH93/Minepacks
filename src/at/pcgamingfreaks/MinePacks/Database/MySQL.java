/*
 *   Copyright (C) 2014-2016 GeorgH93
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
		poolConfig.setJdbcUrl("jdbc:mysql://" + plugin.config.getMySQLHost() + "/" + plugin.config.getMySQLDatabase() + "?allowMultiQueries=true&autoReconnect=true");
		poolConfig.setUsername(plugin.config.getMySQLUser());
		poolConfig.setPassword(plugin.config.getMySQLPassword());
		poolConfig.setMinimumIdle(1);
		poolConfig.setMaximumPoolSize(8);
		return poolConfig;
	}

	@Override
	protected void updateQuerysForDialect()
	{
		Query_DeleteOldBackpacks = "DELETE FROM `{TableBackpacks}` WHERE `{FieldBPLastUpdate}` + INTERVAL {VarMaxAge} day < NOW()";
		Query_UpdateBP = Query_UpdateBP.replaceAll("\\{NOW\\}", "NOW()");
	}

	@Override
	protected void checkDB()
	{
		try(Connection connection = getConnection(); Statement stmt = connection.createStatement())
		{
			ResultSet res;
			if(useUUIDs)
			{
				stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Players + "` (`" + Field_PlayerID + "` INT UNSIGNED NOT NULL AUTO_INCREMENT,`" + Field_Name + "` CHAR(16) NOT NULL,`" + Field_UUID + "` CHAR(36) UNIQUE, PRIMARY KEY (`" + Field_PlayerID + "`));");
				res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + Table_Players + "' AND COLUMN_NAME = '" + Field_UUID + "';");
				if(!res.next())
				{
					stmt.execute("ALTER TABLE `" + Table_Players + "` ADD COLUMN `" + Field_UUID + "` CHAR(36) UNIQUE;");
				}
				res.close();
				res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + Table_Players + "' AND COLUMN_NAME = '" + Field_Name + "' AND COLUMN_KEY='UNI';");
				if(res.next())
				{
					stmt.execute("ALTER TABLE `" + Table_Players + "` DROP INDEX `" + Field_Name + "_UNIQUE`;");
				}
				res.close();
				if(useUUIDSeparators)
				{
					res = stmt.executeQuery("SELECT CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + Table_Players + "' AND COLUMN_NAME = '" + Field_UUID + "';");
					if(res.next() && res.getInt(1) < 36)
					{
						stmt.execute("ALTER TABLE `" + Table_Players + "` MODIFY `" + Field_UUID + "` CHAR(36) UNIQUE;");
					}
					res.close();
				}
			}
			else
			{
				stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Players + "` (`" + Field_PlayerID + "` INT UNSIGNED NOT NULL AUTO_INCREMENT,`" + Field_Name + "` CHAR(16) NOT NULL, PRIMARY KEY (`" + Field_PlayerID + "`));");
				res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + Table_Players + "' AND COLUMN_NAME = '" + Field_Name + "' AND COLUMN_KEY='UNI';");
				if(!res.next())
				{
					stmt.execute("ALTER TABLE `" + Table_Players + "` ADD UNIQUE INDEX `" + Field_Name + "_UNIQUE` (`" + Field_Name + "` ASC);");
				}
				res.close();
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Backpacks + "` (`" + Field_BPOwner + "` INT UNSIGNED NOT NULL, `" + Field_BPITS + "` BLOB, `"
					+ Field_BPVersion + "` INT DEFAULT 0, " + ((maxAge > 0) ? "`" + Field_BPLastUpdate + "` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " : "") + "PRIMARY KEY (`" + Field_BPOwner + "`));");
			res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + Table_Backpacks + "' AND COLUMN_NAME = '" + Field_BPVersion + "';");
			if(!res.next())
			{
				stmt.execute("ALTER TABLE `" + Table_Backpacks + "` ADD COLUMN `" + Field_BPVersion + "` INT DEFAULT 0;");
			}
			if(maxAge > 0)
			{
				res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + Table_Backpacks + "' AND COLUMN_NAME = '" + Field_BPLastUpdate + "';");
				if(!res.next())
				{
					stmt.execute("ALTER TABLE `" + Table_Backpacks + "` ADD COLUMN `" + Field_BPLastUpdate + "` TIMESTAMP DEFAULT CURRENT_TIMESTAMP;");
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