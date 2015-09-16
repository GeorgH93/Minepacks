/*
 *   Copyright (C) 2014-2015 GeorgH93
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

package at.pcgamingfreaks.georgh.MinePacks.Database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import at.pcgamingfreaks.UUIDConverter;
import at.pcgamingfreaks.georgh.MinePacks.MinePacks;

public class SQLite extends SQL
{
	public SQLite(MinePacks mp)
	{
		super(mp); // Load Settings
		
		Field_PlayerID = "player_id";
		Field_Name = "name";
		Field_UUID = "uuid";
		Field_BPOwner = "owner";
		Field_BPITS = "itemstacks";
		Field_BPVersion = "version";
		Field_BPLastUpdate = "lastupdate";
		Table_Players = "backpack_players";
		Table_Backpacks = "backpacks";
		
		UseUUIDSeparators = false;
		UpdatePlayer = true;
		
		BuildQuerys(); // Build Query's
		CheckDB(); // Check Database
		if(UseUUIDs && UpdatePlayer)
		{
			CheckUUIDs(); // Check if there are user accounts without UUID
		}
		
		if(maxAge > 0)
		{
			try
			{
				GetConnection().createStatement().execute("DELETE FROM `" + Table_Backpacks + "` WHERE `" + Field_BPLastUpdate + "` < DATE('now', '-" + maxAge + " days')");
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	protected Connection GetConnection()
	{
		try
		{
			if(conn == null || conn.isClosed())
			{
				try
				{
					Class.forName("org.sqlite.JDBC");
					conn = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "backpack.db");
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return conn;
	}
	
	protected void CheckDB()
	{
		try
		{
			Statement stmt = GetConnection().createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Players + "` (`player_id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` CHAR(16) NOT NULL UNIQUE" + ((UseUUIDs) ? ", `uuid` CHAR(32) UNIQUE" : "") +");");
			if(UseUUIDs)
			{
				try
				{
					stmt.execute("ALTER TABLE `" + Table_Players + "` ADD COLUMN `uuid` CHAR(32);");
				}
				catch(SQLException e) { }
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Backpacks + "` (`owner` INT UNSIGNED PRIMARY KEY, `itemstacks` BLOB, `version` INT DEFAULT 0);");
			try
			{
				stmt.execute("ALTER TABLE `" + Table_Backpacks + "` ADD COLUMN `version` INT DEFAULT 0;");
			}
			catch(SQLException e) { }
			if(maxAge > 0)
			{
				try
				{
					ResultSet rs = stmt.executeQuery("SELECT DATE('now');");
					rs.next();
					stmt.execute("ALTER TABLE `" + Table_Backpacks + "` ADD COLUMN `lastupdate` DATE DEFAULT '" + rs.getString(1) + "';");
				}
				catch(SQLException e) { }
			}
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	protected void CheckUUIDs()
	{
		try
		{
			List<String> converter = new ArrayList<String>();
			Statement stmt = GetConnection().createStatement();
			ResultSet res = stmt.executeQuery("SELECT `name` FROM `" + Table_Players + "` WHERE `uuid` IS NULL");
			while(res.next())
			{
				if(res.isFirst())
				{
					plugin.log.info(plugin.lang.get("Console.UpdateUUIDs"));
				}
				converter.add("UPDATE `" + Table_Players + "` SET `uuid`='" + UUIDConverter.getUUIDFromName(res.getString(1), plugin.getServer().getOnlineMode()) + "' WHERE `name`='" + res.getString(1).replace("\\", "\\\\").replace("'", "\\'") + "'");
			}
			if(converter.size() > 0)
			{
				for (String string : converter)
				{
					stmt.execute(string);
				}
				plugin.log.info(String.format(plugin.lang.get("Console.UpdatedUUIDs"),converter.size()));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	protected void AddDateFieldToQuery()
	{
		Query_InsertBP = ") VALUES (";
		Query_UpdateBP = ",`" + Field_BPLastUpdate + "`=DATE('now')";
	}
}