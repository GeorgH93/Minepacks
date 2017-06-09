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

import at.pcgamingfreaks.MinePacks.Backpack;
import at.pcgamingfreaks.MinePacks.MinePacks;
import at.pcgamingfreaks.UUIDConverter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.*;

public abstract class SQL extends Database
{
	private HikariDataSource dataSource;

	protected String tablePlayers, tableBackpacks; // Table Names
	protected String fieldName, fieldPlayerID, fieldUUID, fieldBPOwner, fieldBPITS, fieldBPVersion, fieldBPLastUpdate; // Table Fields
	protected String queryUpdatePlayerAdd, queryGetPlayerID, queryInsertBP, queryUpdateBP, queryGetBP, queryDeleteOldBackpacks, queryGetUnsetOrInvalidUUIDs, queryFixUUIDs; // DB Querys
	protected boolean updatePlayer;

	public SQL(MinePacks mp)
	{
		super(mp);

		HikariConfig poolConfig = getPoolConfig();
		poolConfig.setPoolName("Minepacks-Connection-Pool");
		poolConfig.addDataSourceProperty("cachePrepStmts", "true");
		dataSource = new HikariDataSource(poolConfig);

		loadSettings();
		buildQuerys();
		checkDB();
		if(useUUIDs && updatePlayer)
		{
			checkUUIDs(); // Check if there are user accounts without UUID
		}

		// Delete old backpacks
		if(maxAge > 0)
		{
			try
			{
				getConnection().createStatement().execute(queryDeleteOldBackpacks);
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	protected abstract HikariConfig getPoolConfig();

	protected void loadSettings()
	{
		// Load table and field names
		tablePlayers = plugin.config.getUserTable();
		tableBackpacks = plugin.config.getBackpackTable();
		fieldPlayerID = plugin.config.getDBFields("User.Player_ID");
		fieldName = plugin.config.getDBFields("User.Name");
		fieldUUID = plugin.config.getDBFields("User.UUID");
		fieldBPOwner = plugin.config.getDBFields("Backpack.Owner_ID");
		fieldBPITS = plugin.config.getDBFields("Backpack.ItemStacks");
		fieldBPVersion = plugin.config.getDBFields("Backpack.Version");
		fieldBPLastUpdate = plugin.config.getDBFields("Backpack.LastUpdate");
		updatePlayer = plugin.config.getUpdatePlayer();
	}

	@Override
	public void close()
	{
		dataSource.close();
	}

	protected void checkUUIDs()
	{
		class UpdateData // Helper class for fixing UUIDs
		{
			int id;
			String name, uuid;

			public UpdateData(String name, String uuid, int id)
			{
				this.id = id;
				this.name = name;
				this.uuid = uuid;
			}
		}
		try(Connection connection = getConnection())
		{
			Map<String, UpdateData> toConvert = new HashMap<>();
			List<UpdateData> toUpdate = new LinkedList<>();
			try(Statement stmt = connection.createStatement(); ResultSet res = stmt.executeQuery(queryGetUnsetOrInvalidUUIDs))
			{
				while(res.next())
				{
					if(res.isFirst())
					{
						plugin.log.info(plugin.lang.get("Console.UpdateUUIDs"));
					}
					String uuid = res.getString(fieldUUID);
					if(uuid == null)
					{
						toConvert.put(res.getString(fieldName).toLowerCase(), new UpdateData(res.getString(fieldName), null, res.getInt(fieldPlayerID)));
					}
					else
					{
						uuid = (useUUIDSeparators) ? uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5") : uuid.replaceAll("-", "");
						toUpdate.add(new UpdateData(res.getString(fieldName), uuid, res.getInt(fieldPlayerID)));
					}
				}
			}
			if(toConvert.size() > 0 || toUpdate.size() > 0)
			{
				if(toConvert.size() > 0)
				{
					Map<String, String> newUUIDs = UUIDConverter.getUUIDsFromNames(toConvert.keySet(), true, useUUIDSeparators);
					for(Map.Entry<String, String> entry : newUUIDs.entrySet())
					{
						UpdateData updateData = toConvert.get(entry.getKey().toLowerCase());
						updateData.uuid = entry.getValue();
						toUpdate.add(updateData);
					}
				}
				try(PreparedStatement ps = connection.prepareStatement(queryFixUUIDs))
				{
					for(UpdateData updateData : toUpdate)
					{
						ps.setString(1, updateData.uuid);
						ps.setInt(2, updateData.id);
						ps.addBatch();
						ps.executeBatch();
					}
				}
				plugin.log.info(String.format(plugin.lang.get("Console.UpdatedUUIDs"), toUpdate.size()));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	protected Connection getConnection() throws SQLException
	{
		return dataSource.getConnection();
	}

	protected abstract void checkDB();

	protected final void buildQuerys()
	{
		// Build the SQL querys with placeholders for the table and field names
		queryGetBP = "SELECT `{FieldBPOwner}`,`{FieldBPITS}`,`{FieldBPVersion}` FROM `{TableBackpacks}` INNER JOIN `{TablePlayers}` ON `{TableBackpacks}`.`{FieldBPOwner}`=`{TablePlayers}`.`{FieldPlayerID}` WHERE ";
		if(useUUIDs)
		{
			queryUpdatePlayerAdd = "INSERT INTO `{TablePlayers}` (`{FieldName}`,`{FieldUUID}`) VALUES (?,?) ON DUPLICATE KEY UPDATE `{FieldName}`=?;";
			queryGetPlayerID = "SELECT `{FieldPlayerID}` FROM `{TablePlayers}` WHERE `{FieldUUID}`=?;";
			queryGetBP += "`{FieldUUID}`=?;";
		}
		else
		{
			queryUpdatePlayerAdd = "INSERT IGNORE INTO `{TablePlayers}` (`{FieldName}`) VALUES (?);";
			queryGetPlayerID = "SELECT `{FieldPlayerID}` FROM `{TablePlayers}` WHERE `{FieldName}`=?;";
			queryGetBP += "`{FieldName}`=?;";
		}
		queryInsertBP = "INSERT INTO `{TableBackpacks}` (`{FieldBPOwner}`,`{FieldBPITS}`,`{FieldBPVersion}`) VALUES (?,?,?);";
		queryUpdateBP = "UPDATE `{TableBackpacks}` SET `{FieldBPITS}`=?,`{FieldBPVersion}`=?";
		if(maxAge > 0)
		{
			queryUpdateBP += ",`{FieldBPLastUpdate}`={NOW}";
		}
		queryUpdateBP += " WHERE `{FieldBPOwner}`=?;";
		queryDeleteOldBackpacks = "DELETE FROM `{TableBackpacks}` WHERE `{FieldBPLastUpdate}` < DATE('now', '-{VarMaxAge} days')";
		if(useUUIDSeparators)
		{
			queryGetUnsetOrInvalidUUIDs = "SELECT `{FieldPlayerID}`,`{FieldName}`,`{FieldUUID}` FROM `{TablePlayers}` WHERE `{FieldUUID}` IS NULL OR `{FieldUUID}` NOT LIKE '%-%-%-%-%';";
		}
		else
		{
			queryGetUnsetOrInvalidUUIDs = "SELECT `{FieldPlayerID}`,`{FieldName}`,`{FieldUUID}` FROM `{TablePlayers}` WHERE `{FieldUUID}` IS NULL OR `{FieldUUID}` LIKE '%-%';";
		}
		queryFixUUIDs = "UPDATE `{TablePlayers}` SET `{FieldUUID}`=? WHERE `{FieldPlayerID}`=?;";

		updateQuerysForDialect();

		// Replace the table and filed names with the names from the config
		queryUpdatePlayerAdd = queryUpdatePlayerAdd.replaceAll("\\{TablePlayers\\}", tablePlayers).replaceAll("\\{FieldName\\}", fieldName).replaceAll("\\{FieldUUID\\}", fieldUUID).replaceAll("\\{FieldPlayerID\\}", fieldPlayerID);
		queryGetPlayerID = queryGetPlayerID.replaceAll("\\{TablePlayers\\}", tablePlayers).replaceAll("\\{FieldName\\}", fieldName).replaceAll("\\{FieldUUID\\}", fieldUUID).replaceAll("\\{FieldPlayerID\\}", fieldPlayerID);
		queryGetBP = queryGetBP.replaceAll("\\{FieldBPOwner\\}", fieldBPOwner).replaceAll("\\{FieldBPITS\\}", fieldBPITS).replaceAll("\\{FieldBPVersion\\}", fieldBPVersion).replaceAll("\\{TableBackpacks\\}", tableBackpacks).replaceAll("\\{TablePlayers\\}", tablePlayers).replaceAll("\\{FieldPlayerID\\}", fieldPlayerID).replaceAll("\\{FieldName\\}", fieldName).replaceAll("\\{FieldUUID\\}", fieldUUID);
		queryInsertBP = queryInsertBP.replaceAll("\\{TableBackpacks\\}", tableBackpacks).replaceAll("\\{FieldBPOwner\\}", fieldBPOwner).replaceAll("\\{FieldBPITS\\}", fieldBPITS).replaceAll("\\{FieldBPVersion\\}", fieldBPVersion).replaceAll("\\{FieldBPLastUpdate\\}", fieldBPLastUpdate);
		queryUpdateBP = queryUpdateBP.replaceAll("\\{TableBackpacks\\}", tableBackpacks).replaceAll("\\{FieldBPOwner\\}", fieldBPOwner).replaceAll("\\{FieldBPITS\\}", fieldBPITS).replaceAll("\\{FieldBPVersion\\}", fieldBPVersion).replaceAll("\\{FieldBPLastUpdate\\}", fieldBPLastUpdate);
		queryDeleteOldBackpacks = queryDeleteOldBackpacks.replaceAll("\\{TableBackpacks\\}", tableBackpacks).replaceAll("\\{FieldBPLastUpdate\\}", fieldBPLastUpdate).replaceAll("\\{VarMaxAge\\}", maxAge + "");
		queryGetUnsetOrInvalidUUIDs = queryGetUnsetOrInvalidUUIDs.replaceAll("\\{TablePlayers\\}", tablePlayers).replaceAll("\\{FieldName\\}", fieldName).replaceAll("\\{FieldUUID\\}", fieldUUID).replaceAll("\\{FieldPlayerID\\}", fieldPlayerID);
		queryFixUUIDs = queryFixUUIDs.replaceAll("\\{TablePlayers\\}", tablePlayers).replaceAll("\\{FieldUUID\\}", fieldUUID).replaceAll("\\{FieldPlayerID\\}", fieldPlayerID);
	}

	protected abstract void updateQuerysForDialect();

	protected void runStatementAsync(final String query, final Object... args)
	{
		Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				runStatement(query, args);
			}
		});
	}

	protected void runStatement(final String query, final Object... args)
	{
		try(Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query))
		{
			for(int i = 0; args != null && i < args.length; i++)
			{
				preparedStatement.setObject(i + 1, args[i]);
			}
			preparedStatement.execute();
		}
		catch(SQLException e)
		{
			System.out.print("Query: " + query);
			e.printStackTrace();
		}
	}

	// Plugin Functions
	@Override
	public void updatePlayer(final Player player)
	{
		if(useUUIDs)
		{
			runStatementAsync(queryUpdatePlayerAdd, player.getName(), getPlayerFormattedUUID(player), player.getName());
		}
		else
		{
			runStatementAsync(queryUpdatePlayerAdd, player.getName());
		}
	}

	@Override
	public void saveBackpack(final Backpack backpack)
	{
		final byte[] data = itsSerializer.serialize(backpack.getInventory());
		final int id = backpack.getOwnerID(), usedSerializer = itsSerializer.getUsedSerializer();
		final String nameOrUUID = getPlayerNameOrUUID(backpack.getOwner()), name = backpack.getOwner().getName();

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				try(Connection connection = getConnection())
				{
					if(id <= 0)
					{
						final int newID;
						try(PreparedStatement ps = connection.prepareStatement(queryGetPlayerID))
						{
							ps.setString(1, nameOrUUID);
							try(ResultSet rs = ps.executeQuery())
							{
								if(rs.next())
								{
									newID = rs.getInt(1);
									plugin.getServer().getScheduler().runTask(plugin, new Runnable()
									{
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
							}
						}
						if(newID <= 0)
						{
							plugin.log.warning("Failed saving backpack for: " + name + " (Unable to get players ID from database)");
							return;
						}
						try(PreparedStatement ps = connection.prepareStatement(queryInsertBP))
						{
							ps.setInt(1, newID);
							ps.setBytes(2, data);
							ps.setInt(3, usedSerializer);
							ps.execute();
						}
					}
					else
					{
						try(PreparedStatement ps = connection.prepareStatement(queryUpdateBP))
						{
							ps.setBytes(1, data);
							ps.setInt(2, usedSerializer);
							ps.setInt(3, id);
							ps.execute();
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void loadBackpack(final OfflinePlayer player, final Callback<Backpack> callback)
	{
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				try(Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(queryGetBP))
				{
					ps.setString(1, getPlayerNameOrUUID(player));
					final int bpID, version;
					final byte[] data;
					try(ResultSet rs = ps.executeQuery())
					{
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
					}
					plugin.getServer().getScheduler().runTask(plugin, new Runnable()
					{
						@Override
						public void run()
						{
							ItemStack[] its = (data != null) ? itsSerializer.deserialize(data, version) : null;
							if(its != null)
							{
								callback.onResult(new Backpack(player, its, bpID));
							}
							else
							{
								callback.onFail();
							}
						}
					});
				}
				catch(Exception e)
				{
					e.printStackTrace();
					callback.onFail();
				}
			}
		});
	}

	@Override
	public Backpack loadBackpack(OfflinePlayer player) // The sync function shouldn't be called at all
	{
		try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryGetBP))
		{
			ps.setString(1, getPlayerNameOrUUID(player));
			try(ResultSet rs = ps.executeQuery())
			{
				if(rs.next())
				{
					return new Backpack(player, itsSerializer.deserialize(rs.getBytes(2), rs.getInt(3)), rs.getInt(1));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}