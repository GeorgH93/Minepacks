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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import at.pcgamingfreaks.georgh.MinePacks.MinePacks;

public class MySQL extends SQL
{
	public MySQL(MinePacks mp)
	{
		super(mp); // Load Settings

		BuildQuerys(); // Build Querys
		CheckDB(); // Check Database
		if(UseUUIDs && UpdatePlayer)
		{
			CheckUUIDs(); // Check if there are user accounts without UUID
		}
	}
	
	protected void CheckUUIDs()
	{
		try
		{
			List<String> converter = new ArrayList<String>();
			Statement stmt = GetConnection().createStatement();
			ResultSet res = stmt.executeQuery("SELECT `" + Field_PlayerID + "`,`" + Field_Name + "` FROM `" + Table_Players + "` WHERE `" + Field_UUID + "` IS NULL");
			while(res.next())
			{
				if(res.isFirst())
				{
					plugin.log.info(plugin.lang.Get("Console.UpdateUUIDs"));
				}
				converter.add("UPDATE `" + Table_Players + "` SET `" + Field_UUID + "`='" + UUIDConverter.getUUIDFromName(res.getString(2), true, UseUUIDSeparators) + "' WHERE `" + Field_PlayerID + "`='" + res.getInt(1) + "'");
			}
			if(converter.size() > 0)
			{
				for (String string : converter)
				{
					stmt.execute(string);
				}
				plugin.log.info(String.format(plugin.lang.Get("Console.UpdatedUUIDs"), converter.size()));
			}
			res.close();
			res = null;
			if(UseUUIDSeparators)
			{
				res = stmt.executeQuery("SELECT `" + Field_PlayerID + "`,`" + Field_UUID + "` FROM `" + Table_Players + "` WHERE `" + Field_UUID + "` NOT LIKE '%-%'");
				while(res.next())
				{
					if(res.isFirst())
					{
						plugin.log.info(plugin.lang.Get("Console.UpdateUUIDs"));
					}
					converter.add("UPDATE `" + Table_Players + "` SET `" + Field_UUID + "`='" + res.getString(2).replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5") + "' WHERE `" + Field_PlayerID + "`='" + res.getInt(1) + "'");
				}
				if(converter.size() > 0)
				{
					for (String string : converter)
					{
						stmt.execute(string);
					}
					plugin.log.info(String.format(plugin.lang.Get("Console.UpdatedUUIDs"), converter.size()));
				}
			}
			else
			{
				res = stmt.executeQuery("SELECT `" + Field_PlayerID + "`,`" + Field_UUID + "` FROM `" + Table_Players + "` WHERE `" + Field_UUID + "` LIKE '%-%'");
				while(res.next())
				{
					if(res.isFirst())
					{
						plugin.log.info(plugin.lang.Get("Console.UpdateUUIDs"));
					}
					converter.add("UPDATE `" + Table_Players + "` SET `" + Field_UUID + "`='" + res.getString(2).replaceAll("-", "") + "' WHERE `" + Field_PlayerID + "`='" + res.getInt(1) + "'");
				}
				if(converter.size() > 0)
				{
					for (String string : converter)
					{
						stmt.execute(string);
					}
					plugin.log.info(String.format(plugin.lang.Get("Console.UpdatedUUIDs"), converter.size()));
				}
			}
			res.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	protected Connection GetConnection()
	{
		try
		{
			if(conn == null || conn.isClosed())
			{
				conn = DriverManager.getConnection("jdbc:mysql://" + plugin.config.GetMySQLHost() + "/" + plugin.config.GetMySQLDatabase(), plugin.config.GetMySQLUser(), plugin.config.GetMySQLPassword());
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
			ResultSet res;
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Players + "` (`" + Field_PlayerID + "` INT UNSIGNED NOT NULL AUTO_INCREMENT,`" + Field_Name + "` CHAR(16) NOT NULL UNIQUE" + ((UseUUIDs) ? ",`" + Field_UUID + "` CHAR(36) UNIQUE" : "") + ", PRIMARY KEY (`" + Field_PlayerID + "`));");
			if(UseUUIDs)
			{
				res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + Table_Players + "' AND COLUMN_NAME = '" + Field_UUID + "';");
				if(!res.next())
				{
					stmt.execute("ALTER TABLE `" + Table_Players + "` ADD COLUMN `" + Field_UUID + "` CHAR(36) UNIQUE;");
				}
				res.close();
			}
			if(UseUUIDSeparators)
			{
				res = stmt.executeQuery("SELECT CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + Table_Players + "' AND COLUMN_NAME = '" + Field_UUID + "';");
				if(res.next() && res.getInt(1) < 36)
				{
					stmt.execute("ALTER TABLE `" + Table_Players + "` ADD MODIFY `" + Field_UUID + "` CHAR(36) UNIQUE;");
				}
				res.close();
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Backpacks + "` (`" + Field_BPOwner + "` INT UNSIGNED NOT NULL, `" + Field_BPITS + "` BLOB, `" + Field_BPVersion + "` INT DEFAULT 0, PRIMARY KEY (`" + Field_BPOwner + "`));");
			res = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + Table_Backpacks + "' AND COLUMN_NAME = '" + Field_BPVersion + "';");
			if(!res.next())
			{
				stmt.execute("ALTER TABLE `" + Table_Backpacks + "` ADD COLUMN `" + Field_BPVersion + "` INT DEFAULT 0;");
			}
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	// Plugin Functions
	public void UpdatePlayer(final Player player)
	{
		if(!UpdatePlayer)
		{
			return;
		}
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
		{
			@Override
			public void run()
		    {
				try
				{
					PreparedStatement ps;
					Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.config.GetMySQLHost() + "/" + plugin.config.GetMySQLDatabase(), plugin.config.GetMySQLUser(), plugin.config.GetMySQLPassword());;
					ps = con.prepareStatement(Query_UpdatePlayerGet);
					if(UseUUIDs)
					{
						if(UseUUIDSeparators)
						{
							ps.setString(1, player.getUniqueId().toString());
						}
						else
						{
							ps.setString(1, player.getUniqueId().toString().replace("-", ""));
						}
					}
					else
					{
						ps.setString(1, player.getName());
					}
					ResultSet rs = ps.executeQuery();
					if(rs.next())
					{
						rs.close();
						ps.close();
						if(!UseUUIDs)
						{
							con.close();
							return;
						}
						ps = con.prepareStatement(Query_UpdatePlayerUUID);
						ps.setString(1, player.getName());
						if(UseUUIDSeparators)
						{
							ps.setString(2, player.getUniqueId().toString());
						}
						else
						{
							ps.setString(2, player.getUniqueId().toString().replace("-", ""));
						}
					}
					else
					{
						rs.close();
						ps.close();
						ps = con.prepareStatement(Query_UpdatePlayerAdd);
						ps.setString(1, player.getName());
						if(UseUUIDs)
						{
							if(UseUUIDSeparators)
							{
								ps.setString(2, player.getUniqueId().toString());
							}
							else
							{
								ps.setString(2, player.getUniqueId().toString().replace("-", ""));
							}
						}
					}
					ps.execute();
					ps.close();
					con.close();
				}
				catch (SQLException e)
			    {
			        plugin.log.info("Failed to add user: " + player.getName());
			        e.printStackTrace();
			    }
		    }});
	}
}