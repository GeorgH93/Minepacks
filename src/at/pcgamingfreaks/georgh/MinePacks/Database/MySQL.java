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

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import at.pcgamingfreaks.georgh.MinePacks.Backpack;
import at.pcgamingfreaks.georgh.MinePacks.MinePacks;

public class MySQL extends Database
{
	private Connection conn = null;
	
	private String Table_Players, Table_Backpacks; // Table Names
	
	private boolean UpdatePlayer;
	
	public MySQL(MinePacks mp)
	{
		super(mp);
		// Load Settings
		Table_Players = plugin.config.getUserTable();
		Table_Backpacks = plugin.config.getBackpackTable();
		UpdatePlayer = plugin.config.getUpdatePlayer();
		
		CheckDB(); // Check Database
		if(plugin.config.UseUUIDs())
		{
			CheckUUIDs(); // Check if there are user accounts without UUID
		}
	}
	
	private void CheckUUIDs()
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
					plugin.log.info(plugin.lang.Get("Console.UpdateUUIDs"));
				}
				converter.add("UPDATE `" + Table_Players + "` SET `uuid`='" + UUIDConverter.getUUIDFromName(res.getString(1), true) + "' WHERE `name`='" + res.getString(1).replace("\\", "\\\\").replace("'", "\\'") + "'");
			}
			if(converter.size() > 0)
			{
				for (String string : converter)
				{
					stmt.execute(string);
				}
				plugin.log.info(String.format(plugin.lang.Get("Console.UpdatedUUIDs"),converter.size()));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private Connection GetConnection()
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
	
	private void CheckDB()
	{
		try
		{
			Statement stmt = GetConnection().createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Players + "` (`player_id` INT UNSIGNED NOT NULL AUTO_INCREMENT, `name` CHAR(16) NOT NULL UNIQUE, PRIMARY KEY (`player_id`));");
			if(plugin.UseUUIDs)
			{
				try
				{
					stmt.execute("ALTER TABLE `" + Table_Players + "` ADD COLUMN `uuid` CHAR(32) UNIQUE;");
				}
				catch(SQLException e) { }
			}
			stmt.execute("CREATE TABLE IF NOT EXISTS `" + Table_Backpacks + "` (`owner` INT UNSIGNED NOT NULL, `itemstacks` BLOB, PRIMARY KEY (`owner`));");
			try
			{
				stmt.execute("ALTER TABLE `" + Table_Backpacks + "` ADD COLUMN `version` INT DEFAULT 0;");
			}
			catch(SQLException e) { }
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
					ps = con.prepareStatement("SELECT `player_id` FROM `" + Table_Players + "` WHERE " + ((plugin.UseUUIDs) ? "`uuid`" : "`name`") + "=?;");
					if(plugin.UseUUIDs)
					{
						ps.setString(1, player.getUniqueId().toString().replace("-", ""));
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
						if(!plugin.UseUUIDs)
						{
							con.close();
							return;
						}
						ps = con.prepareStatement("UPDATE `" + Table_Players + "` SET `name`=? WHERE `uuid`=?;");
						ps.setString(1, player.getName());
						ps.setString(2, player.getUniqueId().toString().replace("-", ""));
					}
					else
					{
						rs.close();
						ps.close();
						ps = con.prepareStatement("INSERT INTO `" + Table_Players + "` (`name`" + ((plugin.UseUUIDs) ? ",`uuid`" : "") + ") VALUES (?" + ((plugin.UseUUIDs) ? ",?" : "") + ");");
						ps.setString(1, player.getName());
						if(plugin.UseUUIDs)
						{
							ps.setString(2, player.getUniqueId().toString().replace("-", ""));
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

	public void SaveBackpack(Backpack backpack)
	{
		try
		{
			PreparedStatement ps = null; // Statement Variable
			// Building the mysql statement
			if(backpack.getID() <= 0)
			{
				ps = GetConnection().prepareStatement("SELECT `player_id` FROM `" + Table_Players + "` WHERE " + ((plugin.UseUUIDs) ? "`uuid`" : "`name`")+ "=?;");
				if(plugin.UseUUIDs)
				{
					ps.setString(1, backpack.getOwner().getUniqueId().toString().replace("-", ""));
				}
				else
				{
					ps.setString(1, backpack.getOwner().getName());
				}
				ResultSet rs = ps.executeQuery();
				if(rs.next())
			    {
			    	backpack.setID(rs.getInt(1));
			    }
			    else
			    {
			    	plugin.log.warning("Faild saving backpack for: " + backpack.getOwner().getName());
			    	return;
			    }
				rs.close();
				ps.close();
				ps = GetConnection().prepareStatement("INSERT INTO `" + Table_Backpacks + "` (`owner`, `itemstacks`, `version`) VALUES (?,?,?);", Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, backpack.getID());
				ps.setBytes(2, itsSerializer.Serialize(backpack.getBackpack()));
				ps.setInt(3, itsSerializer.getUsedVersion());
				ps.executeUpdate();
				rs = ps.getGeneratedKeys();
				backpack.setID(rs.getInt(1));
				ps.close();
				rs.close();
				return;
			}
			else
			{
				ps = GetConnection().prepareStatement("UPDATE `" + Table_Backpacks + "` SET `itemstacks`=?,`version`=? WHERE `owner`=?");
				ps.setBytes(1, itsSerializer.Serialize(backpack.getBackpack()));
				ps.setInt(2, itsSerializer.getUsedVersion());
				ps.setInt(3, backpack.getID());
			}
			ps.execute();
			ps.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Backpack LoadBackpack(OfflinePlayer player)
	{
		try
		{
			PreparedStatement ps = null; // Statement Variable
			ps = GetConnection().prepareStatement("SELECT `owner`,`itemstacks`,`version` FROM `" + Table_Backpacks + "` INNER JOIN `" + Table_Players + "` ON `owner`=`player_id` WHERE " + ((plugin.UseUUIDs) ? "`uuid`" : "`name`")+ "=?;");
			if(plugin.UseUUIDs)
			{
				ps.setString(1, player.getUniqueId().toString().replace("-", ""));
			}
			else
			{
				ps.setString(1, player.getName());
			}
			ResultSet rs = ps.executeQuery();
			if(!rs.next())
			{
				return null;
			}
			int bpid = rs.getInt(1);
			ItemStack[] its = itsSerializer.Deserialize(rs.getBytes(2), rs.getInt(3));
			rs.close();
			ps.close();
			return new Backpack(player, its, bpid);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}