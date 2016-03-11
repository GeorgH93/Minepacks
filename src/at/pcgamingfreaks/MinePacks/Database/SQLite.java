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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite extends SQL
{
	public SQLite(MinePacks mp)
	{
		super(mp);
	}

	@Override
	protected void loadSettings()
	{
		// Set table and field names to fixed values to prevent users from destroying old databases.
		Field_PlayerID = "player_id";
		Field_Name = "name";
		Field_UUID = "uuid";
		Field_BPOwner = "owner";
		Field_BPITS = "itemstacks";
		Field_BPVersion = "version";
		Field_BPLastUpdate = "lastupdate";
		Table_Players = "backpack_players";
		Table_Backpacks = "backpacks";
		// Set fixed settings
		useUUIDSeparators = false;
		UpdatePlayer = true;
	}

	@Override
	protected HikariConfig getPoolConfig()
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		HikariConfig poolConfig = new HikariConfig();
		poolConfig.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "backpack.db");
		poolConfig.setConnectionTestQuery("SELECT 1;");
		return poolConfig;
	}

	@Override
	protected void updateQuerysForDialect()
	{
		if(maxAge > 0)
		{
			Query_InsertBP = Query_InsertBP.replaceAll("\\) VALUES \\(\\?,\\?,\\?", "{FieldBPLastUpdate}) VALUES (?,?,?,DATE('now')");
		}
		Query_DeleteOldBackpacks = "DELETE FROM `{TableBackpacks}` WHERE `{FieldBPLastUpdate}` < DATE('now', '-{VarMaxAge} days')";
		Query_UpdateBP = Query_UpdateBP.replaceAll("\\{NOW\\}", "DATE('now')");
		if(useUUIDs)
		{
			Query_UpdatePlayerAdd = "INSERT OR IGNORE INTO `{TablePlayers}` (`{FieldName}`,`{FieldUUID}`) VALUES (?,?);";
		}
		else
		{
			Query_UpdatePlayerAdd = Query_UpdatePlayerAdd.replaceAll("INSERT IGNORE INTO", "INSERT OR IGNORE INTO");
		}
	}

	@Override
	protected void checkDB()
	{
		try(Connection connection = getConnection(); Statement stmt = connection.createStatement())
		{
			stmt.execute("CREATE TABLE IF NOT EXISTS `backpack_players` (`player_id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` CHAR(16) NOT NULL" + ((useUUIDs) ? " , `uuid` CHAR(32)" : "") + " UNIQUE);");
			if(useUUIDs)
			{
				try
				{
					stmt.execute("ALTER TABLE `backpack_players` ADD COLUMN `uuid` CHAR(32);");
				}
				catch(SQLException ignored) {}
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS `backpacks` (`owner` INT UNSIGNED PRIMARY KEY, `itemstacks` BLOB, `version` INT DEFAULT 0);");
			try
			{
				stmt.execute("ALTER TABLE `backpacks` ADD COLUMN `version` INT DEFAULT 0;");
			}
			catch(SQLException ignored) {}
			if(maxAge > 0)
			{
				try
				{
					ResultSet rs = stmt.executeQuery("SELECT DATE('now');");
					rs.next();
					stmt.execute("ALTER TABLE `backpacks` ADD COLUMN `lastupdate` DATE DEFAULT '" + rs.getString(1) + "';");
				}
				catch(SQLException ignored)
				{
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void updatePlayer(final Player player)
	{
		if(useUUIDs)
		{
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run()
				{
					runStatement(Query_UpdatePlayerAdd, player.getName(), getPlayerFormattedUUID(player));
					runStatement("UPDATE `" + Table_Players + "` SET `" + Field_Name + "`=? WHERE `" + Field_UUID + "`=?;", player.getName(), getPlayerFormattedUUID(player));
				}
			});
		}
		else
		{
			runStatementAsync(Query_UpdatePlayerAdd, player.getName());
		}
	}
}