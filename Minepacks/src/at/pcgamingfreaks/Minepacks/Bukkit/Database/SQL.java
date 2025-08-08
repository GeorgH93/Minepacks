/*
 *   Copyright (C) 2024 GeorgH93
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

import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.logging.Level;

public abstract class SQL extends Database
{
	private final ConnectionProvider dataSource;

	protected String tablePlayers, tableBackpacks, tableCooldowns; // Table Names
	protected String fieldPlayerName, fieldPlayerID, fieldPlayerUUID, fieldBpOwner, fieldBpIts, fieldBpVersion, fieldBpLastUpdate, fieldCdPlayer, fieldCdTime; // Table Fields
	@Language("SQL") protected String queryUpdatePlayerAdd, queryGetPlayerID, queryInsertBp, queryUpdateBp, queryGetBP, queryDeleteOldBackpacks; // DB Queries
	@Language("SQL") protected String queryDeleteOldCooldowns, querySyncCooldown, queryGetCooldown; // DB Queries
	protected boolean syncCooldown;

	public SQL(@NotNull Minepacks plugin, @NotNull ConnectionProvider connectionProvider)
	{
		super(plugin);

		dataSource = connectionProvider;
		if(!dataSource.isAvailable()) throw new IllegalStateException("Failed to initialize database connection!");

		loadSettings();
		buildQueries();
		checkDB();
		checkUUIDs(); // Check if there are user accounts without UUID

		// Delete old backpacks
		if(maxAge > 0)
		{
			try(Connection connection = getConnection(); Statement statement = connection.createStatement())
			{
				statement.execute(queryDeleteOldBackpacks);
			}
			catch(SQLException e)
			{
				plugin.getLogger().log(Level.SEVERE, "Failed to delete old backpacks! Error: {0}", e.getMessage());
			}
		}
		// Delete old cooldowns
		if(syncCooldown)
		{
			try(Connection connection = getConnection())
			{
				DBTools.runStatement(connection, queryDeleteOldCooldowns, new Timestamp(System.currentTimeMillis()));
			}
			catch(SQLException e)
			{
				plugin.getLogger().log(Level.SEVERE, "Failed to delete cooldowns! Error: {0}", e.getMessage());
			}
		}
	}

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
		try(Connection connection = getConnection())
		{
			DBTools.validateUUIDs(plugin.getLogger(), connection, tablePlayers, fieldPlayerName, fieldPlayerUUID, fieldPlayerID, useUUIDSeparators, onlineUUIDs);
		}
		catch(SQLException e)
		{
			plugin.getLogger().log(Level.SEVERE, "Failed to check UUIDs! Error: {0}", e.getMessage());
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
		queryGetBP = "SELECT {FieldBPOwner},{FieldBPITS},{FieldBPVersion} FROM {TableBackpacks} INNER JOIN {TablePlayers} ON {TableBackpacks}.{FieldBPOwner}={TablePlayers}.{FieldPlayerID} WHERE {FieldUUID}=?;";
		querySyncCooldown = "INSERT INTO {TableCooldowns} ({FieldCDPlayer},{FieldCDTime}) SELECT {FieldPlayerID},? FROM {TablePlayers} WHERE {FieldUUID}=? ON DUPLICATE KEY UPDATE {FieldCDTime}=?;";
		queryUpdatePlayerAdd = "INSERT INTO {TablePlayers} ({FieldName},{FieldUUID}) VALUES (?,?) ON DUPLICATE KEY UPDATE {FieldName}=?;";
		queryGetPlayerID = "SELECT {FieldPlayerID} FROM {TablePlayers} WHERE {FieldUUID}=?;";
		queryGetCooldown = "SELECT * FROM {TableCooldowns} WHERE {FieldCDPlayer} IN (SELECT {FieldPlayerID} FROM {TablePlayers} WHERE {FieldUUID}=?);";
		queryInsertBp = "REPLACE INTO {TableBackpacks} ({FieldBPOwner},{FieldBPITS},{FieldBPVersion}) VALUES (?,?,?);";
		queryUpdateBp = "UPDATE {TableBackpacks} SET {FieldBPITS}=?,{FieldBPVersion}=?,{FieldBPLastUpdate}={NOW} WHERE {FieldBPOwner}=?;";
		queryDeleteOldBackpacks = "DELETE FROM {TableBackpacks} WHERE {FieldBPLastUpdate} < DATE('now', '-{VarMaxAge} days')";
		queryDeleteOldCooldowns = "DELETE FROM {TableCooldowns} WHERE {FieldCDTime}<?;";

		updateQueriesForDialect();

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
		queryDeleteOldBackpacks     = replacePlaceholders(queryDeleteOldBackpacks.replace("{VarMaxAge}", String.valueOf(maxAge)));
		querySyncCooldown           = replacePlaceholders(querySyncCooldown);
		queryGetCooldown            = replacePlaceholders(queryGetCooldown);
		queryDeleteOldCooldowns     = replacePlaceholders(queryDeleteOldCooldowns);
	}

	protected abstract void updateQueriesForDialect();

	protected String replacePlaceholders(@Language("SQL") String query)
	{
		return query.replaceAll("(\\{\\w+})", "`$1`").replaceAll("`(\\{\\w+})`_(\\w+)", "`$1_$2`").replaceAll("fk_`(\\{\\w+})`_`(\\{\\w+})`_`(\\{\\w+})`", "`fk_$1_$2_$3`") // Fix name formatting
				.replace("{TablePlayers}", tablePlayers).replace("{FieldName}", fieldPlayerName).replace("{FieldUUID}", fieldPlayerUUID).replace("{FieldPlayerID}", fieldPlayerID) // Players
				.replace("{TableBackpacks}", tableBackpacks).replace("{FieldBPOwner}", fieldBpOwner).replace("{FieldBPITS}", fieldBpIts) // Backpacks
				.replace("{FieldBPVersion}", fieldBpVersion).replace("{FieldBPLastUpdate}", fieldBpLastUpdate) // Backpacks
				.replace("{TableCooldowns}", tableCooldowns).replace("{FieldCDPlayer}", fieldCdPlayer).replace("{FieldCDTime}", fieldCdTime); // Cooldowns
	}

	protected void runStatementAsync(final String query, final Object... args)
	{
		Minepacks.getScheduler().runAsync(task -> runStatement(query, args));
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
			plugin.getLogger().log(Level.SEVERE, e, () -> "Query: " + query);
		}
	}

	// Plugin Functions
	@Override
	public void updatePlayer(final Player player)
	{
		runStatementAsync(queryUpdatePlayerAdd, player.getName(), getPlayerFormattedUUID(player), player.getName());
	}

	@Override
	public void saveBackpack(final Backpack backpack)
	{
		final byte[] data = itsSerializer.serialize(backpack.getInventory());
		final int id = backpack.getOwnerDatabaseId(), usedSerializer = itsSerializer.getUsedSerializer();
		final String nameOrUUID = getPlayerFormattedUUID(backpack.getOwnerId()), name = backpack.getOwner().getName();

		Runnable runnable = () -> {
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
								Minepacks.getScheduler().runNextTick(task -> backpack.setOwnerDatabaseId(newID));
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
				plugin.getLogger().log(Level.SEVERE, "Failed to save backpack in database! Error: {0}", e.getMessage());
				writeBackup(name, nameOrUUID, usedSerializer, data);
			}
		};
		if(asyncSave) Minepacks.getScheduler().runAsync(task -> runnable.run()); else runnable.run();
	}

	@Override
	protected void loadBackpack(final OfflinePlayer player, final Callback<Backpack> callback)
	{
		Minepacks.getScheduler().runAsync(task -> {
			try(Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(queryGetBP))
			{
				final String playerUUID = getPlayerFormattedUUID(player);
				ps.setString(1, playerUUID);
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

				ItemStack[] its = itsSerializer.deserialize(data, version);
				if (data != null && data.length != 0 && its == null)
				{
					writeBackup(player.getName(), playerUUID, version, data);
				}
				final Backpack backpack = (its != null) ? new Backpack(player, its, bpID) : null;
				Minepacks.getScheduler().runNextTick(task1 -> {
					if(backpack != null)
					{
						callback.onResult(backpack);
					}
					else
					{
						callback.onFail();
					}
				});
			}
			catch(SQLException e)
			{
				plugin.getLogger().log(Level.SEVERE, "Failed to load backpack! Error: {0}", e.getMessage());
				Minepacks.getScheduler().runNextTick(task1 -> callback.onFail());
			}
		});
	}
	
	@Override
	public void syncCooldown(Player player, long cooldownTime)
	{
		Timestamp ts = new Timestamp(cooldownTime);
		runStatementAsync(querySyncCooldown, ts, getPlayerFormattedUUID(player), ts);
	}

	@Override
	public void getCooldown(final Player player, final Callback<Long> callback)
	{
		Minepacks.getScheduler().runAsync(asyncTask -> {
			try(Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(queryGetCooldown))
			{
				ps.setString(1, getPlayerFormattedUUID(player));
				try(ResultSet rs = ps.executeQuery())
				{
					final long time = (rs.next()) ? rs.getTimestamp(fieldCdTime).getTime() : 0;
					Minepacks.getScheduler().runNextTick(task -> callback.onResult(time));
				}
			}
			catch(SQLException e)
			{
				plugin.getLogger().log(Level.SEVERE, "Failed to load cooldown! Error: {0}", e.getMessage());
				Minepacks.getScheduler().runNextTick(task -> callback.onResult(0L));
			}
		});
	}
}