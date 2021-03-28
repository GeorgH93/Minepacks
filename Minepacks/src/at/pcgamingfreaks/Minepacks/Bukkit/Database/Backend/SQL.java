/*
 *   Copyright (C) 2020 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend;

import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.BackupHandler;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.MinepacksPlayerData;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.UUIDConverter;
import at.pcgamingfreaks.Utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;

import java.sql.*;
import java.util.*;

public abstract class SQL extends DatabaseBackend
{
	private final ConnectionProvider dataSource;

	protected String tablePlayers = "backpack_players", tableBackpacks = "backpacks", tableCooldowns = "backpack_cooldowns"; // Table Names
	protected String fieldPlayerName = "name", fieldPlayerID = "id", fieldPlayerUUID = "uuid"; // Table players
	protected String fieldBpOwner = "owner", fieldBpIts = "its", fieldBpVersion = "version", fieldBpLastUpdate = "lastupdate"; // Table Backpack
	protected String fieldCdPlayer = "id", fieldCdTime = "time"; // Table Fields

	@Language("SQL") protected String queryUpdatePlayerAdd, queryInsertBp, queryUpdateBp, queryGetPlayer, queryGetBP, queryDeleteOldBackpacks, queryGetUnsetOrInvalidUUIDs, queryFixUUIDs; // DB Querys
	@Language("SQL") protected String queryDeleteOldCooldowns, querySyncCooldown; // DB Querys
	protected boolean syncCooldown;

	public SQL(@NotNull Minepacks plugin, @NotNull ConnectionProvider connectionProvider) throws SQLException
	{
		super(plugin);

		dataSource = connectionProvider;
		if(!dataSource.isAvailable()) throw new IllegalStateException("Failed to initialize database connection!");

		loadSettings();
		buildQueries();
		checkDB();
		checkUUIDs(); // Check if there are user accounts without UUID

		// Delete old backpacks
		try(Connection connection = getConnection())
		{
			DBTools.runStatementWithoutException(connection, queryDeleteOldBackpacks);
			if(syncCooldown) DBTools.runStatementWithoutException(connection, queryDeleteOldCooldowns, System.currentTimeMillis());
		}
	}

	protected void loadSettings()
	{
		// Load table and field names
		tablePlayers      = plugin.getConfiguration().getDBTable("User", tablePlayers);
		tableBackpacks    = plugin.getConfiguration().getDBTable("Backpack", tableBackpacks);
		tableCooldowns    = plugin.getConfiguration().getDBTable("Cooldown", tableCooldowns);
		fieldPlayerID     = plugin.getConfiguration().getDBFields("User.Player_ID", fieldPlayerID);
		fieldPlayerName   = plugin.getConfiguration().getDBFields("User.Name", fieldPlayerName);
		fieldPlayerUUID   = plugin.getConfiguration().getDBFields("User.UUID", fieldPlayerUUID);
		fieldBpOwner      = plugin.getConfiguration().getDBFields("Backpack.Owner_ID", fieldBpOwner);
		fieldBpIts        = plugin.getConfiguration().getDBFields("Backpack.ItemStacks", fieldBpIts);
		fieldBpVersion    = plugin.getConfiguration().getDBFields("Backpack.Version", fieldBpVersion);
		fieldBpLastUpdate = plugin.getConfiguration().getDBFields("Backpack.LastUpdate", fieldBpLastUpdate);
		fieldCdPlayer     = plugin.getConfiguration().getDBFields("Cooldown.Player_ID", fieldCdPlayer);
		fieldCdTime       = plugin.getConfiguration().getDBFields("Cooldown.Time", fieldCdTime);
		syncCooldown      = plugin.getConfiguration().isCommandCooldownSyncEnabled();
	}

	@Override
	public void close()
	{
		Utils.blockThread(1); // Give the database some time to perform async operations
		dataSource.close();
	}

	protected void checkUUIDs()
	{
		@AllArgsConstructor
		class UpdateData // Helper class for fixing UUIDs
		{
			String  uuid;
			final int id;
		}
		try(Connection connection = getConnection())
		{
			Map<String, UpdateData> toConvert = new HashMap<>();
			List<UpdateData> toUpdate = new ArrayList<>();
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
						toConvert.put(res.getString(fieldPlayerName).toLowerCase(Locale.ROOT), new UpdateData(null, res.getInt(fieldPlayerID)));
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
					Map<String, String> newUUIDs = UUIDConverter.getUUIDsFromNames(toConvert.keySet(), onlineUUIDs, useUUIDSeparators);
					for(Map.Entry<String, String> entry : newUUIDs.entrySet())
					{
						UpdateData updateData = toConvert.get(entry.getKey().toLowerCase(Locale.ROOT));
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

	protected final void buildQueries()
	{
		// Build the SQL queries with placeholders for the table and field names
		queryGetPlayer = "SELECT * FROM {TablePlayers}" +
				(syncCooldown ? " LEFT JOIN {TableCooldowns} ON {TablePlayers}.{FieldPlayerID} = {TableCooldowns}.{FieldCDPlayer}" : "") +
				" WHERE {FieldUUID}=?;";
		queryUpdatePlayerAdd = "INSERT INTO {TablePlayers} ({FieldName},{FieldUUID}) VALUES (?,?) ON DUPLICATE KEY UPDATE {FieldName}=?;";
		queryGetBP = "SELECT * FROM {TableBackpacks} WHERE {FieldBPOwner}=?;";
		querySyncCooldown = "INSERT INTO {TableCooldowns} ({FieldCDPlayer},{FieldCDTime}) VALUES (?,?) ON DUPLICATE KEY UPDATE {FieldCDTime}=?;";
		queryDeleteOldCooldowns = "DELETE FROM {TableCooldowns} WHERE {FieldCDTime}<?;";
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

		updateQueriesForDialect();

		setTableAndFieldNames();
	}

	protected void setTableAndFieldNames()
	{
		// Replace the table and filed names with the names from the config
		queryUpdatePlayerAdd        = replacePlaceholders(queryUpdatePlayerAdd);
		queryGetPlayer              = replacePlaceholders(queryGetPlayer);
		queryGetBP                  = replacePlaceholders(queryGetBP);
		queryInsertBp               = replacePlaceholders(queryInsertBp);
		queryUpdateBp               = replacePlaceholders(queryUpdateBp);
		queryFixUUIDs               = replacePlaceholders(queryFixUUIDs);
		queryDeleteOldBackpacks     = replacePlaceholders(queryDeleteOldBackpacks.replaceAll("\\{VarMaxAge}", maxAge + ""));
		queryGetUnsetOrInvalidUUIDs = replacePlaceholders(queryGetUnsetOrInvalidUUIDs);
		querySyncCooldown           = replacePlaceholders(querySyncCooldown);
		queryDeleteOldCooldowns     = replacePlaceholders(queryDeleteOldCooldowns);
	}

	protected abstract void updateQueriesForDialect();

	protected String replacePlaceholders(@Language("SQL") String query)
	{
		query = query.replaceAll("(\\{\\w+})", "`$1`").replaceAll("`(\\{\\w+})`_(\\w+)", "`$1_$2`").replaceAll("fk_`(\\{\\w+})`_`(\\{\\w+})`_`(\\{\\w+})`", "`fk_$1_$2_$3`") // Fix name formatting
				.replaceAll("\\{TablePlayers}", tablePlayers).replaceAll("\\{FieldName}", fieldPlayerName).replaceAll("\\{FieldUUID}", fieldPlayerUUID).replaceAll("\\{FieldPlayerID}", fieldPlayerID) // Players
				.replaceAll("\\{TableBackpacks}", tableBackpacks).replaceAll("\\{FieldBPOwner}", fieldBpOwner).replaceAll("\\{FieldBPITS}", fieldBpIts) // Backpacks
				.replaceAll("\\{FieldBPVersion}", fieldBpVersion).replaceAll("\\{FieldBPLastUpdate}", fieldBpLastUpdate) // Backpacks
				.replaceAll("\\{TableCooldowns}", tableCooldowns).replaceAll("\\{FieldCDPlayer}", fieldCdPlayer).replaceAll("\\{FieldCDTime}", fieldCdTime); // Cooldowns
		if(query.matches(".*\\{\\w+}.*")) plugin.getLogger().warning("Found unresolved placeholder in query:\n" + query);
		return query;
	}

	protected void runStatementAsync(final @NotNull @Language("SQL") String query, final Object... args)
	{
		Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> runStatement(query, args));
	}

	protected void runStatement(final @NotNull @Language("SQL") String query, final Object... args)
	{
		try(Connection connection = getConnection())
		{
			DBTools.runStatement(connection, query, args);
		}
		catch(SQLException e)
		{
			plugin.getLogger().severe("Query: " + query);
			e.printStackTrace();
		}
	}

	// Plugin Functions
	protected void updatePlayer(final @NotNull Connection connection, final @NotNull MinepacksPlayerData player) throws SQLException
	{
		DBTools.runStatement(connection, queryUpdatePlayerAdd, player.getName(), formatUUID(player.getUUID()), player.getName());
	}

	@Override
	public void loadPlayer(final @NotNull MinepacksPlayerData player)
	{
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try(Connection connection = getConnection())
			{
				for(int i = 0; i < 3; i++) // Try to load player 3 times
				{
					updatePlayer(connection, player);
					try(PreparedStatement ps = connection.prepareStatement(queryGetPlayer))
					{
						ps.setString(1, formatUUID(player.getUUID()));
						try(ResultSet rs = ps.executeQuery())
						{
							if(rs.next())
							{
								final int id = rs.getInt(fieldPlayerID);
								long cooldown = 0;
								if(syncCooldown) cooldown = rs.getTimestamp(fieldCdPlayer).getTime();
								final long cd = cooldown;
								plugin.getServer().getScheduler().runTask(plugin, () -> player.setLoaded(id, cd));
								return;
							}
						}
					}
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
			plugin.getLogger().warning("Failed to get player id for player " + player.getName());
		});
	}

	@Override
	public void loadBackpack(final @NotNull MinepacksPlayerData player)
	{
		if(!player.isLoaded()) return;
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryGetBP))
			{
				ps.setInt(1, (int) player.getDatabaseKey());
				try(ResultSet rs = ps.executeQuery())
				{
					ItemStack[] its = (rs.next()) ? itsSerializer.deserialize(rs.getBytes(fieldBpIts), rs.getInt(fieldBpVersion)) : null;
					final Backpack backpack = (its != null) ? new Backpack(player, its) : new Backpack(player);
					plugin.getServer().getScheduler().runTask(plugin, () -> player.setBackpack(backpack));
				}
			}
			catch(SQLException e)
			{
				plugin.getLogger().warning("Failed to load backpack from database for player " + player.getName() + "! Error: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	@Override
	public void saveBackpack(final @NotNull Backpack backpack)
	{
		final byte[] data = itsSerializer.serialize(backpack.getInventory());
		final int usedSerializer = itsSerializer.getUsedSerializer();
		final String uuid = formatUUID(backpack.getOwner().getUUID()), name = backpack.getOwner().getName();

		if(backpack.getOwner().getDatabaseKey() == null)
		{
			plugin.getLogger().warning("Failed saving backpack for: " + name + "! Player does not haven an id! This should not have happened.");
			BackupHandler.getInstance().writeBackup(name, uuid, usedSerializer, data);
		}


		Runnable runnable = () -> {
			try(Connection connection = getConnection())
			{
				//DBTools.runStatement(connection, queryUpdateBp, data, usedSerializer, backpack.getOwner().getDatabaseKey());
				DBTools.runStatement(connection, queryInsertBp, backpack.getOwner().getDatabaseKey(), data, usedSerializer);
			}
			catch(SQLException e)
			{
				plugin.getLogger().warning("Failed to save backpack in database for player " + name + "! Error: " + e.getMessage());
				e.printStackTrace();
				BackupHandler.getInstance().writeBackup(name, uuid, usedSerializer, data);
			}
		};
		if(asyncSave) Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable); else runnable.run();
	}

	@Override
	public void saveCooldown(final @NotNull MinepacksPlayerData player)
	{
		final Timestamp ts = new Timestamp(player.getCooldown());
		runStatementAsync(querySyncCooldown, player.getDatabaseKey(), ts, ts);
	}
}