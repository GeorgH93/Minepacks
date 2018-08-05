/*
 *   Copyright (C) 2016-2018 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.UUIDConverter;
import at.pcgamingfreaks.Utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class SQL extends Database
{
	private HikariDataSource dataSource;

	protected String tablePlayers, tableBackpacks, tableCooldowns; // Table Names
	protected String fieldPlayerName, fieldPlayerID, fieldPlayerUUID, fieldBpOwner, fieldBpIts, fieldBpVersion, fieldBpLastUpdate, fieldCdPlayer, fieldCdTime; // Table Fields
	@Language("SQL") protected String queryUpdatePlayerAdd, queryGetPlayerID, queryInsertBp, queryUpdateBp, queryGetBP, queryDeleteOldBackpacks, queryGetUnsetOrInvalidUUIDs, queryFixUUIDs; // DB Querys
	@Language("SQL") protected String queryDeleteOldCooldowns, querySyncCooldown, queryGetCooldown; // DB Querys
	protected boolean updatePlayer, syncCooldown;

	public SQL(Minepacks plugin)
	{
		super(plugin);

		HikariConfig poolConfig = getPoolConfig();
		if(poolConfig != null)
		{
			poolConfig.setPoolName("Minepacks-Connection-Pool");
			poolConfig.addDataSourceProperty("cachePrepStmts", "true");
			dataSource = new HikariDataSource(poolConfig);
		}

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
			try(Connection connection = getConnection(); Statement statement = connection.createStatement())
			{
				statement.execute(queryDeleteOldBackpacks);
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		// Delete old cooldowns
		if(syncCooldown)
		{
			try(Connection connection = getConnection())
			{
				DBTools.runStatement(connection, queryDeleteOldCooldowns, System.currentTimeMillis());
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	protected abstract @Nullable HikariConfig getPoolConfig();

	protected void loadSettings()
	{
		// Load table and field names
		tablePlayers      = plugin.getConfiguration().getUserTable();
		tableBackpacks    = plugin.getConfiguration().getBackpackTable();
		tableCooldowns    = plugin.getConfiguration().getCooldownTable();
		fieldPlayerID     = plugin.getConfiguration().getDBFields("User.Player_ID", "id");
		fieldPlayerName   = plugin.getConfiguration().getDBFields("User.Name", "name");
		fieldPlayerUUID   = plugin.getConfiguration().getDBFields("User.UUID", "uuid");
		fieldBpOwner      = plugin.getConfiguration().getDBFields("Backpack.Owner_ID", "owner");
		fieldBpIts        = plugin.getConfiguration().getDBFields("Backpack.ItemStacks", "its");
		fieldBpVersion    = plugin.getConfiguration().getDBFields("Backpack.Version", "version");
		fieldBpLastUpdate = plugin.getConfiguration().getDBFields("Backpack.LastUpdate", "lastUpdate");
		fieldCdPlayer     = plugin.getConfiguration().getDBFields("Cooldown.Player_ID", "id");
		fieldCdTime       = plugin.getConfiguration().getDBFields("Cooldown.Time", "time");
		updatePlayer      = plugin.getConfiguration().getUpdatePlayer();
		syncCooldown      = plugin.getConfiguration().isCommandCooldownSyncEnabled();
	}

	@Override
	public void close()
	{
		super.close();
		Utils.blockThread(1); // Give the database some time to perform async operations
		dataSource.close();
	}

	protected void checkUUIDs()
	{
		class UpdateData // Helper class for fixing UUIDs
		{
			int id;
			String  uuid;

			public UpdateData(String uuid, int id)
			{
				this.id = id;
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
						plugin.getLogger().info(START_UUID_UPDATE);
					}
					String uuid = res.getString(fieldPlayerUUID);
					if(uuid == null)
					{
						toConvert.put(res.getString(fieldPlayerName).toLowerCase(), new UpdateData(null, res.getInt(fieldPlayerID)));
					}
					else
					{
						uuid = (useUUIDSeparators) ? uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5") : uuid.replaceAll("-", "");
						toUpdate.add(new UpdateData(uuid, res.getInt(fieldPlayerID)));
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
					}
					ps.executeBatch();
				}
				plugin.getLogger().info(String.format(UUIDS_UPDATED, toUpdate.size()));
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public Connection getConnection() throws SQLException
	{
		return dataSource.getConnection();
	}

	protected abstract void checkDB();

	protected final void buildQuerys()
	{
		// Build the SQL querys with placeholders for the table and field names
		queryGetBP = "SELECT {FieldBPOwner},{FieldBPITS},{FieldBPVersion} FROM {TableBackpacks} INNER JOIN {TablePlayers} ON {TableBackpacks}.{FieldBPOwner}={TablePlayers}.{FieldPlayerID} WHERE ";
		if(useUUIDs)
		{
			queryUpdatePlayerAdd = "INSERT INTO {TablePlayers} ({FieldName},{FieldUUID}) VALUES (?,?) ON DUPLICATE KEY UPDATE {FieldName}=?;";
			queryGetPlayerID = "SELECT {FieldPlayerID} FROM {TablePlayers} WHERE {FieldUUID}=?;";
			queryGetBP += "{FieldUUID}=?;";
			querySyncCooldown = "INSERT INTO {TableCooldowns} ({FieldCDPlayer},{FieldCDTime}) SELECT {FieldPlayerID},? FROM {TablePlayers} WHERE {FieldUUID}=?;";
			queryGetCooldown = "SELECT * FROM {TableCooldowns} WHERE {FieldCDPlayer} IN (SELECT {FieldPlayerID} FROM {TablePlayers} WHERE {FieldUUID}=?);";
		}
		else
		{
			queryUpdatePlayerAdd = "INSERT IGNORE INTO {TablePlayers} ({FieldName}) VALUES (?);";
			queryGetPlayerID = "SELECT {FieldPlayerID} FROM {TablePlayers} WHERE {FieldName}=?;";
			queryGetBP += "{FieldName}=?;";
			querySyncCooldown = "INSERT INTO {TableCooldowns} ({FieldCDPlayer},{FieldCDTime}) SELECT {FieldPlayerID},? FROM {TablePlayers} WHERE {FieldName}=?;";
			queryGetCooldown = "SELECT * FROM {TableCooldowns} WHERE {FieldCDPlayer} IN (SELECT {FieldPlayerID} FROM {TablePlayers} WHERE {FieldName}=?);";
		}
		queryInsertBp = "REPLACE INTO {TableBackpacks} ({FieldBPOwner},{FieldBPITS},{FieldBPVersion}) VALUES (?,?,?);";
		queryUpdateBp = "UPDATE {TableBackpacks} SET {FieldBPITS}=?,{FieldBPVersion}=?,{FieldBPLastUpdate}={NOW} WHERE {FieldBPOwner}=?;";
		queryDeleteOldBackpacks = "DELETE FROM {TableBackpacks} WHERE {FieldBPLastUpdate} < DATE('now', '-{VarMaxAge} days')";
		if(useUUIDSeparators)
		{
			queryGetUnsetOrInvalidUUIDs = "SELECT {FieldPlayerID},{FieldName},{FieldUUID} FROM {TablePlayers} WHERE {FieldUUID} IS NULL OR {FieldUUID} NOT LIKE '%-%-%-%-%';";
		}
		else
		{
			queryGetUnsetOrInvalidUUIDs = "SELECT {FieldPlayerID},{FieldName},{FieldUUID} FROM {TablePlayers} WHERE {FieldUUID} IS NULL OR {FieldUUID} LIKE '%-%';";
		}
		queryFixUUIDs = "UPDATE {TablePlayers} SET {FieldUUID}=? WHERE {FieldPlayerID}=?;";
		queryDeleteOldCooldowns = "DELETE FROM {TableCooldowns} WHERE {FieldCDTime}<?;";

		updateQuerysForDialect();

		setTableAndFieldNames();
	}

	protected void setTableAndFieldNames()
	{
		// Replace the table and filed names with the names from the config
		queryUpdatePlayerAdd        = replacePlaceholders(queryUpdatePlayerAdd);
		queryGetPlayerID            = replacePlaceholders(queryGetPlayerID);
		queryGetBP                  = replacePlaceholders(queryGetBP);
		queryInsertBp               = replacePlaceholders(queryInsertBp);
		queryUpdateBp               = replacePlaceholders(queryUpdateBp);
		queryFixUUIDs               = replacePlaceholders(queryFixUUIDs);
		queryDeleteOldBackpacks     = replacePlaceholders(queryDeleteOldBackpacks.replaceAll("\\{VarMaxAge}", maxAge + ""));
		queryGetUnsetOrInvalidUUIDs = replacePlaceholders(queryGetUnsetOrInvalidUUIDs);
		querySyncCooldown           = replacePlaceholders(querySyncCooldown);
		queryGetCooldown            = replacePlaceholders(queryGetCooldown);
		queryDeleteOldCooldowns     = replacePlaceholders(queryDeleteOldCooldowns);
	}

	protected abstract void updateQuerysForDialect();

	protected String replacePlaceholders(@Language("SQL") String query)
	{
		return query.replaceAll("(\\{\\w+})", "`$1`").replaceAll("`(\\{\\w+})`_(\\w+)", "`$1_$2`").replaceAll("fk_`(\\{\\w+})`_`(\\{\\w+})`_`(\\{\\w+})`", "`fk_$1_$2_$3`") // Fix name formatting
				.replaceAll("\\{TablePlayers}", tablePlayers).replaceAll("\\{FieldName}", fieldPlayerName).replaceAll("\\{FieldUUID}", fieldPlayerUUID).replaceAll("\\{FieldPlayerID}", fieldPlayerID) // Players
				.replaceAll("\\{TableBackpacks}", tableBackpacks).replaceAll("\\{FieldBPOwner}", fieldBpOwner).replaceAll("\\{FieldBPITS}", fieldBpIts) // Backpacks
				.replaceAll("\\{FieldBPVersion}", fieldBpVersion).replaceAll("\\{FieldBPLastUpdate}", fieldBpLastUpdate) // Backpacks
				.replaceAll("\\{TableCooldowns}", tableCooldowns).replaceAll("\\{FieldCDPlayer}", fieldCdPlayer).replaceAll("\\{FieldCDTime}", fieldCdTime); // Cooldowns
	}

	protected void runStatementAsync(final String query, final Object... args)
	{
		Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> runStatement(query, args));
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

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try(Connection connection = getConnection())
			{
				if(id <= 0)
				{
					try(PreparedStatement ps = connection.prepareStatement(queryGetPlayerID))
					{
						ps.setString(1, nameOrUUID);
						try(ResultSet rs = ps.executeQuery())
						{
							if(rs.next())
							{
								final int newID = rs.getInt(fieldPlayerID);
								DBTools.runStatement(connection, queryInsertBp, newID, data, usedSerializer);
								plugin.getServer().getScheduler().runTask(plugin, () -> backpack.setOwnerID(newID));
							}
							else
							{
								plugin.getLogger().warning("Failed saving backpack for: " + name + " (Unable to get players ID from database)");
								writeBackup(name, nameOrUUID, usedSerializer, data);
							}
						}
					}
				}
				else
				{
					DBTools.runStatement(connection, queryUpdateBp, data, usedSerializer, id);
				}
			}
			catch(SQLException e)
			{
				plugin.getLogger().warning("Failed to save backpack in database! Error: " + e.getMessage());
				e.printStackTrace();
				writeBackup(name, nameOrUUID, usedSerializer, data);
			}
		});
	}

	@Override
	protected void loadBackpack(final OfflinePlayer player, final Callback<Backpack> callback)
	{
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try(Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(queryGetBP))
			{
				ps.setString(1, getPlayerNameOrUUID(player));
				final int bpID, version;
				final byte[] data;
				try(ResultSet rs = ps.executeQuery())
				{
					if(rs.next())
					{
						bpID = rs.getInt(fieldBpOwner);
						version = rs.getInt(fieldBpVersion);
						data = rs.getBytes(fieldBpIts);
					}
					else
					{
						bpID = -1;
						version = 0;
						data = null;
					}
				}
				plugin.getServer().getScheduler().runTask(plugin, () -> {
					ItemStack[] its = itsSerializer.deserialize(data, version);
					if(its != null)
					{
						callback.onResult(new Backpack(player, its, bpID));
					}
					else
					{
						callback.onFail();
					}
				});
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				plugin.getServer().getScheduler().runTask(plugin, callback::onFail);
			}
		});
	}
	
	@Override
	public void syncCooldown(Player player, long cooldownTime)
	{
		runStatementAsync(querySyncCooldown, new Timestamp(cooldownTime), getPlayerNameOrUUID(player));
	}

	@Override
	public void getCooldown(final Player player, final Callback<Long> callback)
	{
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try(Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(queryGetCooldown))
			{
				ps.setString(1, getPlayerNameOrUUID(player));
				try(ResultSet rs = ps.executeQuery())
				{
					final long time = (rs.next()) ? rs.getLong(fieldCdTime) : 0;
					plugin.getServer().getScheduler().runTask(plugin, () -> callback.onResult(time));
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				plugin.getServer().getScheduler().runTask(plugin, () -> callback.onResult(0L));
			}
		});
	}
}