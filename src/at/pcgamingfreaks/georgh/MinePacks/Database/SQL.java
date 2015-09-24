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

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import at.pcgamingfreaks.UUIDConverter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import at.pcgamingfreaks.georgh.MinePacks.Backpack;
import at.pcgamingfreaks.georgh.MinePacks.MinePacks;

public abstract class SQL extends Database
{
	protected Connection conn = null;
	
	protected String Table_Players, Table_Backpacks; // Table Names
	protected String Field_Name, Field_PlayerID, Field_UUID, Field_BPOwner, Field_BPITS, Field_BPVersion, Field_BPLastUpdate; // Table Fields
	protected String Query_UpdatePlayerAdd, Query_GetPlayerID, Query_InsertBP, Query_UpdateBP, Query_GetBP, Query_DeleteOldBackpacks, Query_GetUnsetOrInvalidUUIDs, Query_FixUUIDs; // DB Querys
	protected boolean UpdatePlayer;
	
	public SQL(MinePacks mp)
	{
		super(mp);

		loadSettings();
		buildQuerys();
		checkDB();
		if(useUUIDs && UpdatePlayer)
		{
			checkUUIDs(); // Check if there are user accounts without UUID
		}

		// Delete old backpacks
		if(maxAge > 0)
		{
			try
			{
				getConnection().createStatement().execute(Query_DeleteOldBackpacks);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	protected void loadSettings()
	{
		// Load table and field names
		Table_Players		= plugin.config.getUserTable();
		Table_Backpacks		= plugin.config.getBackpackTable();
		Field_PlayerID		= plugin.config.getDBFields("User.Player_ID");
		Field_Name			= plugin.config.getDBFields("User.Name");
		Field_UUID			= plugin.config.getDBFields("User.UUID");
		Field_BPOwner		= plugin.config.getDBFields("Backpack.Owner_ID");
		Field_BPITS			= plugin.config.getDBFields("Backpack.ItemStacks");
		Field_BPVersion		= plugin.config.getDBFields("Backpack.Version");
		Field_BPLastUpdate	= plugin.config.getDBFields("Backpack.LastUpdate");
		UpdatePlayer		= plugin.config.getUpdatePlayer();
	}
	
	public void close()
	{
		try
		{
			conn.close();
		}
		catch(Exception ignored) { }
	}

	protected void checkUUIDs()
	{
		class UpdateData // Helper class for fixing UUIDs
		{
			int id;
			String name, uuid;
			public UpdateData(String name, String uuid, int id) { this.id = id; this.name = name; this.uuid = uuid; }
		}
		try
		{
			List<UpdateData> converter = new ArrayList<>();
			PreparedStatement ps = getConnection().prepareStatement(Query_FixUUIDs);
			ResultSet res = getConnection().createStatement().executeQuery(Query_GetUnsetOrInvalidUUIDs);
			while(res.next())
			{
				if(res.isFirst())
				{
					plugin.log.info(plugin.lang.get("Console.UpdateUUIDs"));
				}
				converter.add(new UpdateData(res.getString(2), res.getString(3), res.getInt(1)));
			}
			res.close();
			if(converter.size() > 0)
			{
				for (UpdateData data : converter)
				{
					if(data.uuid == null)
					{
						ps.setString(1, UUIDConverter.getUUIDFromName(data.name, true, useUUIDSeparators, false));
					}
					else
					{
						ps.setString(1, (useUUIDSeparators) ? data.uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5") : data.uuid.replaceAll("-", ""));
					}
					ps.setInt(2, data.id);
				}
				plugin.log.info(String.format(plugin.lang.get("Console.UpdatedUUIDs"), converter.size()));
			}
			ps.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	protected abstract Connection getConnection();

	protected abstract void checkDB();
	
	protected final void buildQuerys()
	{
		// Build the SQL querys with placeholders for the table and field names
		Query_GetBP = "SELECT `{FieldBPOwner}`,`{FieldBPITS}`,`{FieldBPVersion}` FROM `{TableBackpacks}` INNER JOIN `{TablePlayers}` ON `{TableBackpacks}`.`{FieldBPOwner}`=`{TablePlayers}`.`{FieldPlayerID}` WHERE ";
		if(useUUIDs)
		{
			Query_UpdatePlayerAdd = "INSERT INTO `{TablePlayers}` (`{FieldName}`,`{FieldUUID}`) VALUES (?,?) ON DUPLICATE KEY UPDATE `{FieldName}`=?;";
			Query_GetPlayerID = "SELECT `{FieldPlayerID}` FROM `{TablePlayers}` WHERE `{FieldUUID}`=?;";
			Query_GetBP += "`{FieldUUID}`=?;";
		}
		else
		{
			Query_UpdatePlayerAdd = "INSERT IGNORE INTO `{TablePlayers}` (`{FieldName}`) VALUES (?);";
			Query_GetPlayerID = "SELECT `{FieldPlayerID}` FROM `{TablePlayers}` WHERE `{FieldName}`=?;";
			Query_GetBP += "`{FieldName}`=?;";
		}
		Query_InsertBP = "INSERT INTO `{TableBackpacks}` (`{FieldBPOwner}`,`{FieldBPITS}`,`{FieldBPVersion}`) VALUES (?,?,?);";
		Query_UpdateBP = "UPDATE `{TableBackpacks}` SET `{FieldBPITS}`=?,`{FieldBPVersion}`=?";
		if(maxAge > 0)
		{
			Query_UpdateBP += ",`{FieldBPLastUpdate}`={NOW}";
		}
		Query_UpdateBP += " WHERE `{FieldBPOwner}`=?;";
		Query_DeleteOldBackpacks = "DELETE FROM `{TableBackpacks}` WHERE `{FieldBPLastUpdate}` < DATE('now', '-{VarMaxAge} days')";
		if(useUUIDSeparators)
		{
			Query_GetUnsetOrInvalidUUIDs = "SELECT `{FieldPlayerID}`,`{FieldName}`,`{FieldUUID}` FROM `{TablePlayers}` WHERE `{FieldUUID}` IS NULL OR `{FieldUUID}` NOT LIKE '%-%-%-%-%';";
		}
		else
		{
			Query_GetUnsetOrInvalidUUIDs = "SELECT `{FieldPlayerID}`,`{FieldName}`,`{FieldUUID}` FROM `{TablePlayers}` WHERE `{FieldUUID}` IS NULL OR `{FieldUUID}` LIKE '%-%';";
		}
		Query_FixUUIDs = "UPDATE `{TablePlayers}` SET `{FieldUUID}`=? WHERE `{FieldPlayerID}`=?;";

		updateQuerysForDialect();

		// Replace the table and filed names with the names from the config
		Query_UpdatePlayerAdd = Query_UpdatePlayerAdd.replaceAll("\\{TablePlayers\\}", Table_Players).replaceAll("\\{FieldName\\}", Field_Name).replaceAll("\\{FieldUUID\\}", Field_UUID).replaceAll("\\{FieldPlayerID\\}", Field_PlayerID);
		Query_GetPlayerID = Query_GetPlayerID.replaceAll("\\{TablePlayers\\}", Table_Players).replaceAll("\\{FieldName\\}", Field_Name).replaceAll("\\{FieldUUID\\}", Field_UUID).replaceAll("\\{FieldPlayerID\\}", Field_PlayerID);
		Query_GetBP = Query_GetBP.replaceAll("\\{FieldBPOwner\\}", Field_BPOwner).replaceAll("\\{FieldBPITS\\}",Field_BPITS).replaceAll("\\{FieldBPVersion\\}", Field_BPVersion).replaceAll("\\{TableBackpacks\\}", Table_Backpacks).replaceAll("\\{TablePlayers\\}", Table_Players).replaceAll("\\{FieldPlayerID\\}", Field_PlayerID).replaceAll("\\{FieldName\\}", Field_Name).replaceAll("\\{FieldUUID\\}", Field_UUID);
		Query_InsertBP = Query_InsertBP.replaceAll("\\{TableBackpacks\\}", Table_Backpacks).replaceAll("\\{FieldBPOwner\\}", Field_BPOwner).replaceAll("\\{FieldBPITS\\}",Field_BPITS).replaceAll("\\{FieldBPVersion\\}", Field_BPVersion).replaceAll("\\{FieldBPLastUpdate\\}", Field_BPLastUpdate);
		Query_UpdateBP = Query_UpdateBP.replaceAll("\\{TableBackpacks\\}", Table_Backpacks).replaceAll("\\{FieldBPOwner\\}", Field_BPOwner).replaceAll("\\{FieldBPITS\\}",Field_BPITS).replaceAll("\\{FieldBPVersion\\}", Field_BPVersion).replaceAll("\\{FieldBPLastUpdate\\}", Field_BPLastUpdate);
		Query_DeleteOldBackpacks = Query_DeleteOldBackpacks.replaceAll("\\{TableBackpacks\\}", Table_Backpacks).replaceAll("\\{FieldBPLastUpdate\\}", Field_BPLastUpdate).replaceAll("\\{VarMaxAge\\}", maxAge + "");
		Query_GetUnsetOrInvalidUUIDs = Query_GetUnsetOrInvalidUUIDs.replaceAll("\\{TablePlayers\\}", Table_Players).replaceAll("\\{FieldName\\}", Field_Name).replaceAll("\\{FieldUUID\\}", Field_UUID).replaceAll("\\{FieldPlayerID\\}", Field_PlayerID);
		Query_FixUUIDs = Query_FixUUIDs.replaceAll("\\{TablePlayers\\}", Table_Players).replaceAll("\\{FieldUUID\\}", Field_UUID).replaceAll("\\{FieldPlayerID\\}", Field_PlayerID);
	}

	protected abstract void updateQuerysForDialect();

	// Plugin Functions
	public void updatePlayer(final Player player)
	{
		try
		{
			PreparedStatement ps = getConnection().prepareStatement(Query_UpdatePlayerAdd);
			ps.setString(1, player.getName());
			if(useUUIDs)
			{
				String uuid = getPlayerFormattedUUID(player);
				ps.setString(2, uuid);
				ps.setString(3, player.getName());
			}
			ps.execute();
			ps.close();
		}
		catch (SQLException e)
	    {
	        plugin.log.info("Failed to add/update user: " + player.getName());
	        e.printStackTrace();
	    }
	}

	public int getPlayerID(OfflinePlayer player)
	{
		int id = -1;
		try
		{
			PreparedStatement ps = getConnection().prepareStatement(Query_GetPlayerID);
			ps.setString(1, getPlayerNameOrUUID(player));
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				id = rs.getInt(1);
			}
			rs.close();
			ps.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return id;
	}

	public void saveBackpack(Backpack backpack)
	{
		try
		{
			PreparedStatement ps; // Statement Variable
			// Building the mysql statement
			if(backpack.getOwnerID() <= 0)
			{
				backpack.setOwnerID(getPlayerID(backpack.getOwner()));
				if(backpack.getOwnerID() <= 0)
				{
					plugin.log.warning("Failed saving backpack for: " + backpack.getOwner().getName() + " (Unable to get players ID from database)");
					return;
				}
				ps = getConnection().prepareStatement(Query_InsertBP);
				ps.setInt(1, backpack.getOwnerID());
				ps.setBytes(2, itsSerializer.serialize(backpack.getInventory()));
				ps.setInt(3, itsSerializer.getUsedSerializer());
				ps.execute();
				ps.close();
				return;
			}
			else
			{
				ps = getConnection().prepareStatement(Query_UpdateBP);
				ps.setBytes(1, itsSerializer.serialize(backpack.getInventory()));
				ps.setInt(2, itsSerializer.getUsedSerializer());
				ps.setInt(3, backpack.getOwnerID());
			}
			ps.execute();
			ps.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Backpack loadBackpack(OfflinePlayer player)
	{
		try
		{
			PreparedStatement ps = getConnection().prepareStatement(Query_GetBP);
			ps.setString(1, getPlayerNameOrUUID(player));
			ResultSet rs = ps.executeQuery();
			if(!rs.next())
			{
				return null;
			}
			int bpID = rs.getInt(1);
			ItemStack[] its = itsSerializer.deserialize(rs.getBytes(2), rs.getInt(3));
			rs.close();
			ps.close();
			return new Backpack(player, its, bpID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}