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

import at.pcgamingfreaks.georgh.MinePacks.Backpack;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MinePacks.MinePacks;
import org.bukkit.inventory.ItemStack;

public class MySQL extends SQL
{
	public MySQL(MinePacks mp)
	{
		super(mp); // Load Settings

		// Fire DB request every 10 minutes to keep database connection alive
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){ @Override public void run() { try { getConnection().createStatement().execute("SELECT 1"); }
		catch(Exception ignored) {} }}, 600*20, 600*20);
	}

	@Override
	protected void updateQuerysForDialect()
	{
		Query_DeleteOldBackpacks = "DELETE FROM `{TableBackpacks}` WHERE `{FieldBPLastUpdate}` + INTERVAL {VarMaxAge} day < NOW()";
		Query_UpdateBP = Query_UpdateBP.replaceAll("\\{NOW\\}", "NOW()");
	}

	@Override
	protected Connection getConnection()
	{
		try
		{
			if(conn == null || conn.isClosed())
			{
				conn = DriverManager.getConnection("jdbc:mysql://" + plugin.config.getMySQLHost() + "/" + plugin.config.getMySQLDatabase() + "?autoReconnect=true&timeBetweenEvictionRunsMillis=300000&testWhileIdle=true", plugin.config.getMySQLUser(), plugin.config.getMySQLPassword());
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return conn;
	}

	protected Connection getNewConnection()
	{
		try
		{
			return DriverManager.getConnection("jdbc:mysql://" + plugin.config.getMySQLHost() + "/" + plugin.config.getMySQLDatabase() + "?autoReconnect=true&timeBetweenEvictionRunsMillis=300000&testWhileIdle=true", plugin.config.getMySQLUser(), plugin.config.getMySQLPassword());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void checkDB()
	{
		try
		{
			Statement stmt = getConnection().createStatement();
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
			stmt.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	// Plugin Functions
	@Override
	public void updatePlayer(final Player player)
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
					Connection con = getNewConnection();
					PreparedStatement ps = con.prepareStatement(Query_UpdatePlayerAdd);
					ps.setString(1, player.getName());
					if(useUUIDs)
					{
						String uuid = getPlayerFormattedUUID(player);
						ps.setString(2, uuid);
						ps.setString(3, player.getName());
					}
					ps.execute();
					ps.close();
					con.close();
				}
				catch(SQLException e)
				{
					plugin.log.info("Failed to add/update user: " + player.getName());
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void loadBackpack(final OfflinePlayer player, final Callback<Backpack> callback)
	{
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run()
			{
				try
				{
					Connection conn = getNewConnection();
					PreparedStatement ps = getConnection().prepareStatement(Query_GetBP);
					ps.setString(1, getPlayerNameOrUUID(player));
					ResultSet rs = ps.executeQuery();
					final int bpID, version;
					final byte[] data;
					if(rs.next())
					{
						bpID = rs.getInt(1);
						version = rs.getInt(3);
						data = rs.getBytes(2);
					}
					else
					{
						bpID = -1;
						version = 0;
						data = null;
					}
					plugin.getServer().getScheduler().runTask(plugin, new Runnable()
					{
						@Override
						public void run()
						{
							ItemStack[] its = (data != null) ? itsSerializer.deserialize(data, version) : null;
							callback.onResult((its != null) ? new Backpack(player, its, bpID) : null);
						}
					});
					rs.close();
					ps.close();
					conn.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void saveBackpack(final Backpack backpack)
	{
		final byte[] data = itsSerializer.serialize(backpack.getInventory());
		final int id = backpack.getOwnerID();
		final String nameOrUUID = getPlayerNameOrUUID(backpack.getOwner()), name = backpack.getOwner().getName();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run()
			{
				try
				{
					Connection conn = getNewConnection();
					PreparedStatement ps; // Statement Variable
					// Building the mysql statement
					if(id <= 0)
					{
						final int newID;
						ps = conn.prepareStatement(Query_GetPlayerID);
						ps.setString(1, nameOrUUID);
						ResultSet rs = ps.executeQuery();
						if(rs.next())
						{
							newID = rs.getInt(1);
							plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
								@Override
								public void run()
								{
									backpack.setOwnerID(newID);
								}
							});
						}
						else
						{
							newID = -1;
						}
						rs.close();
						ps.close();
						if(newID <= 0)
						{
							plugin.log.warning("Failed saving backpack for: " + name + " (Unable to get players ID from database)");
							conn.close();
							return;
						}
						ps = conn.prepareStatement(Query_InsertBP);
						ps.setInt(1, newID);
						ps.setBytes(2, data);
						ps.setInt(3, itsSerializer.getUsedSerializer());
					}
					else
					{
						ps = conn.prepareStatement(Query_UpdateBP);
						ps.setBytes(1, data);
						ps.setInt(2, itsSerializer.getUsedSerializer());
						ps.setInt(3, id);
					}
					ps.execute();
					ps.close();
					conn.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}