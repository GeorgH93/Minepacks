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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend;

import at.pcgamingfreaks.DataHandler.HasPlaceholders;
import at.pcgamingfreaks.DataHandler.ILoadableStringFieldsHolder;
import at.pcgamingfreaks.DataHandler.IStringFieldsWithPlaceholdersHolder;
import at.pcgamingfreaks.DataHandler.Loadable;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backpack.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.BackupHandler;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.MinepacksPlayerData;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.Minepacks.MagicValues;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public abstract class SQL extends DatabaseBackend implements IStringFieldsWithPlaceholdersHolder, ILoadableStringFieldsHolder
{
	private final ConnectionProvider connectionProvider;

	@Loadable protected String tablePlayers = "minepacks_players", tableBackpacks = "minepacks_backpacks", tableCooldowns = "minepacks_cooldowns"; // Table names
	@Loadable protected String tableBackpackStyles = "minepacks_backpack_styles", tablePlayerSettings = "minepacks_player_settings"; // Table names
	@Loadable(metadata = "User") protected String fieldPlayerName = "name", fieldPlayerID = "id", fieldPlayerUUID = "uuid"; // Table fields players
	@Loadable(metadata = "Backpack") protected String fieldBpOwnerID = "owner", fieldBpItemStacks = "its", fieldBpVersion = "version", fieldBpLastUpdate = "lastupdate"; // Table fields backpack
	@Loadable(metadata = "Cooldown") protected String fieldCdPlayerID = "id", fieldCdTime = "time"; // Table fields cooldown
	@Loadable(metadata = "BackpackStyles") protected String fieldBsStyleID = "id", fieldBsStyleName = "name"; // Table fields backpack styles
	@Loadable(metadata = "PlayerSettings") protected String fieldPsPlayerID = "id", fieldPsStyleID = "bp_style"; // Table fields player settings

	@HasPlaceholders @Language("SQL") protected String queryInsertPlayer, queryUpdatePlayer, queryInsertBp, queryUpdateBp, queryGetPlayer, queryGetBP, querySyncCooldown, querySaveBackpackStyle; // DB queries
	@HasPlaceholders @Language("SQL") protected String queryDeleteOldCooldowns, queryDeleteOldBackpacks, queryGetUnsetOrInvalidUUIDs, queryFixUUIDs, queryGetStyleId, queryAddStyle, queryAddIDedStyle; // Maintenance queries
	protected boolean syncCooldown;

	protected final Map<Integer, ItemConfig> backpackStyleMap = new HashMap<>();

	public SQL(final @NotNull Minepacks plugin, final @NotNull ConnectionProvider connectionProvider) throws SQLException
	{
		super(plugin);

		this.connectionProvider = connectionProvider;
		if(!this.connectionProvider.isAvailable()) throw new IllegalStateException("Failed to initialize database connection!");
	}

	@Override
	public void init() throws SQLException
	{
		loadSettings();
		buildQueries();
		checkDB();
		checkUUIDs(); // Check if there are user accounts without UUID

		// Delete old backpacks and cooldowns
		try(Connection connection = getConnection())
		{
			if(maxAge > 0) DBTools.runStatementWithoutException(connection, queryDeleteOldBackpacks);
			if(syncCooldown) DBTools.runStatementWithoutException(connection, queryDeleteOldCooldowns, System.currentTimeMillis());
		}

		loadBackpackStylesMap();
	}

	protected void loadSettings()
	{
		loadFields(); // Load table and field names

		syncCooldown = plugin.getConfiguration().isCommandCooldownSyncEnabled();
	}

	@Override
	public String loadField(@NotNull String fieldName, @NotNull String metadata, @Nullable String currentValue)
	{
		if(fieldName.startsWith("table"))
		{
			return plugin.getConfiguration().getDBTable(fieldName.substring("table".length(), fieldName.length() - 1), currentValue);
		}
		else if(fieldName.startsWith("field"))
		{
			fieldName = fieldName.substring("field".length());
			if(fieldName.startsWith("Player") && !fieldName.equals("PlayerID"))
			{
				fieldName = fieldName.substring("Player".length());
			}
			else if(fieldName.startsWith("Bp") || fieldName.startsWith("Cd") || fieldName.startsWith("Bs") || fieldName.startsWith("Ps"))
				fieldName = fieldName.substring(2);
			return plugin.getConfiguration().getDBFields(metadata + "." + fieldName, currentValue);
		}
		return null;
	}

	@Override
	public void close()
	{
		Utils.blockThread(1); // Give the database some time to perform async operations
		connectionProvider.close();
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
		return connectionProvider.getConnection();
	}

	protected abstract void checkDB();

	protected final void buildQueries()
	{
		// Build the SQL queries with placeholders for the table and field names
		queryGetPlayer = "SELECT {TablePlayers}.{FieldPlayerID} AS {FieldPlayerID}, " +
				(syncCooldown ? "{TableCooldowns}.{FieldCDTime} AS {FieldCDTime}, " : "") +
				"{TablePlayerSettings}.{FieldPSBackpackStyle} AS {FieldPSBackpackStyle} FROM {TablePlayers} " +
				(syncCooldown ? " LEFT JOIN {TableCooldowns} ON {TablePlayers}.{FieldPlayerID} = {TableCooldowns}.{FieldCDPlayer} " : "") +
				"LEFT JOIN {TablePlayerSettings} ON {TablePlayerSettings}.{FieldPSPlayerID} = {TablePlayers}.{FieldPlayerID} " +
				"WHERE {FieldUUID}=?;";
		queryInsertPlayer = "INSERT IGNORE INTO {TablePlayers} ({FieldName},{FieldUUID}) VALUES (?,?);";
		queryUpdatePlayer = "UPDATE {TablePlayers} SET {FieldName}=? WHERE {FieldUUID}=?;";
		queryGetBP = "SELECT * FROM {TableBackpacks} WHERE {FieldBPOwner}=?;";
		querySyncCooldown = "INSERT INTO {TableCooldowns} ({FieldCDPlayer},{FieldCDTime}) VALUES (?,?) ON DUPLICATE KEY UPDATE {FieldCDTime}=?;";
		querySaveBackpackStyle = "INSERT INTO {TablePlayerSettings} ({FieldPSPlayerID},{FieldPSBackpackStyle}) VALUES (?,?) ON DUPLICATE KEY UPDATE {FieldPSBackpackStyle}=?;";
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
		queryGetStyleId = "SELECT {FieldBSStyleID} FROM {TableBackpackStyles} WHERE {FieldBSStyleName}=?;";
		queryAddStyle = "INSERT IGNORE INTO {TableBackpackStyles} ({FieldBSStyleName}) VALUES (?);";
		queryAddIDedStyle = "INSERT IGNORE INTO {TableBackpackStyles} ({FieldBSStyleID}, {FieldBSStyleName}) VALUES (?,?) ON DUPLICATE KEY UPDATE {FieldBSStyleName}=?;";

		updateQueriesForDialect();

		queryDeleteOldBackpacks = queryDeleteOldBackpacks.replace("{VarMaxAge}", maxAge + "");
		replacePlaceholders();
	}

	protected abstract void updateQueriesForDialect();

	@Override
	public @NotNull String replacePlaceholders(@NotNull @Language("SQL") String query)
	{
		query = query.replaceAll("(\\{\\w+})", "`$1`").replaceAll("`(\\{\\w+})`_(\\w+)", "`$1_$2`").replaceAll("fk_`(\\{\\w+})`_`(\\{\\w+})`_`(\\{\\w+})`", "`fk_$1_$2_$3`") // Fix name formatting
				.replace("{TablePlayers}", tablePlayers).replace("{FieldName}", fieldPlayerName).replace("{FieldUUID}", fieldPlayerUUID).replace("{FieldPlayerID}", fieldPlayerID) // Players
				.replace("{TableBackpacks}", tableBackpacks).replace("{FieldBPOwner}", fieldBpOwnerID).replace("{FieldBPITS}", fieldBpItemStacks) // Backpacks
				.replace("{FieldBPVersion}", fieldBpVersion).replace("{FieldBPLastUpdate}", fieldBpLastUpdate) // Backpacks
				.replace("{TableCooldowns}", tableCooldowns).replace("{FieldCDPlayer}", fieldCdPlayerID).replace("{FieldCDTime}", fieldCdTime) // Cooldowns
				.replace("{TableBackpackStyles}", tableBackpackStyles).replace("{FieldBSStyleID}", fieldBsStyleID).replace("{FieldBSStyleName}", fieldBsStyleName) // Backpack styles
				.replace("{TablePlayerSettings}", tablePlayerSettings).replace("{FieldPSPlayerID}", fieldPsPlayerID).replace("{FieldPSBackpackStyle}", fieldPsStyleID) // Player settings
		;
		if(query.matches(".*\\{\\w+}.*")) plugin.getLogger().warning("Found unresolved placeholder in query:\n" + query);
		return query;
	}

	protected void runStatementAsync(final @NotNull @Language("SQL") String query, final Object... args)
	{
		Minepacks.getScheduler().runAsync(task -> runStatement(query, args));
	}

	protected void runStatement(final @NotNull @Language("SQL") String query, final Object... args)
	{
		try(Connection connection = getConnection())
		{
			DBTools.runStatement(connection, query, args);
		}
		catch(SQLException e)
		{
			plugin.getLogger().log(Level.SEVERE, e, () -> "Query: " + query);
		}
	}

	protected void loadBackpackStylesMap() throws SQLException
	{
		List<String> noId = new ArrayList<>();
		try(Connection connection = getConnection())
		{
			// Make sure disabled is always id 0 and default is always 1
			if(StringUtils.countMatches(queryAddIDedStyle, "?") == 3)
			{
				DBTools.runStatement(connection, queryAddIDedStyle, 0, MagicValues.BACKPACK_STYLE_NAME_DEFAULT, MagicValues.BACKPACK_STYLE_NAME_DEFAULT);
				DBTools.runStatement(connection, queryAddIDedStyle, 1, MagicValues.BACKPACK_STYLE_NAME_DEFAULT, MagicValues.BACKPACK_STYLE_NAME_DISABLED);
			}
			else
			{
				DBTools.runStatement(connection, queryAddIDedStyle, 0, MagicValues.BACKPACK_STYLE_NAME_DEFAULT);
				DBTools.runStatement(connection, queryAddIDedStyle, 1, MagicValues.BACKPACK_STYLE_NAME_DISABLED);
			}
			Collection<ItemConfig> itemConfigs = plugin.getBackpacksConfig().getBackpackItems();
			try(PreparedStatement ps = connection.prepareStatement(queryAddStyle))
			{
				for(ItemConfig style : itemConfigs)
				{
					ps.setString(1, style.getName());
					ps.addBatch();
				}
				ps.executeBatch();
			}
			catch(SQLException ignored) {}
			try(PreparedStatement ps = connection.prepareStatement(queryGetStyleId))
			{
				for(ItemConfig style : itemConfigs)
				{
					if(style.getDatabaseKey() != null) continue;
					ps.setString(1, style.getName());
					try(ResultSet rs = ps.executeQuery())
					{
						if(rs.next())
						{
							style.setDatabaseKey(rs.getInt(fieldBsStyleID));
							backpackStyleMap.put((Integer) style.getDatabaseKey(), style);
						}
						else noId.add(style.getName());
					}
				}
			}
			backpackStyleMap.put(1, null);
			backpackStyleMap.put(null, plugin.getBackpacksConfig().getBackpackStylesMap().get(MagicValues.BACKPACK_STYLE_NAME_DEFAULT));
		}
		if(!noId.isEmpty())
		{
			plugin.getLogger().warning("Failed to obtain id for backpack styles: " + String.join(", ", noId));
		}
	}

	// Plugin Functions
	protected void updatePlayer(final @NotNull Connection connection, final @NotNull MinepacksPlayerData player) throws SQLException
	{
		DBTools.runStatement(connection, queryInsertPlayer, player.getName(), formatUUID(player.getUUID()));
		DBTools.runStatement(connection, queryUpdatePlayer, player.getName(), formatUUID(player.getUUID()));
	}

	@Override
	public void loadPlayer(final @NotNull MinepacksPlayerData player)
	{
		Minepacks.getScheduler().runAsync(task -> {
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
								if(syncCooldown && rs.getTimestamp(fieldCdTime) != null) cooldown = rs.getTimestamp(fieldCdTime).getTime();
								final long cd = cooldown;
								final ItemConfig backpackItem = backpackStyleMap.get(rs.getInt(fieldPsStyleID));

								Minepacks.getScheduler().runNextTick(task1 -> player.setLoaded(id, cd, backpackItem));
								return;
							}
						}
					}
				}
			}
			catch(SQLException e)
			{
				plugin.getLogger().log(Level.SEVERE, "Failed to load player from database! Error: {0}", e.getMessage());
			}
			plugin.getLogger().warning("Failed to get player id for player " + player.getName());
		});
	}

	@Override
	public void loadBackpack(final @NotNull MinepacksPlayerData player)
	{
		if(!player.isLoaded()) return;
		Minepacks.getScheduler().runAsync(task -> {
			try(Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(queryGetBP))
			{
				ps.setInt(1, (int) player.getDatabaseKey());
				try(ResultSet rs = ps.executeQuery())
				{
					final Backpack backpack;
					if(rs.next())
					{
						final byte[] data = rs.getBytes(fieldBpItemStacks);
						final int version = rs.getInt(fieldBpVersion);
						ItemStack[] its = itsSerializer.deserialize(data, version);
						if(its == null && data != null && data.length != 0)
						{
							plugin.getLogger().warning("Failed to load the backpack of " + player.getName() + " from the database! Writing a backup of the corrupted data.");
							BackupHandler.getInstance().writeBackup(player.getName(), formatUUID(player.getUUID()), version, data);
						}
						backpack = (its != null) ? new Backpack(player, its) : new Backpack(player);
					}
					else
					{
						backpack = new Backpack(player);
					}
					Minepacks.getScheduler().runNextTick(task1 -> player.setBackpack(backpack));
				}
			}
			catch(SQLException e)
			{
				plugin.getLogger().log(Level.SEVERE, "Failed to load backpack from database for player " + player.getName() + "! Error: {0}", e.getMessage());
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
				plugin.getLogger().log(Level.SEVERE, "Failed to save backpack in database for player " + name + "! Error: {0}", e.getMessage());
				BackupHandler.getInstance().writeBackup(name, uuid, usedSerializer, data);
			}
		};
		if(asyncSave) Minepacks.getScheduler().runAsync(task -> runnable.run()); else runnable.run();
	}

	@Override
	public void saveCooldown(final @NotNull MinepacksPlayerData player)
	{
		final Timestamp ts = new Timestamp(player.getCooldown());
		runStatementAsync(querySyncCooldown, player.getDatabaseKey(), ts, ts);
	}

	@Override
	public void saveBackpackStyle(@NotNull MinepacksPlayerData player)
	{
		Object backpackStyle = player.getBackpackStyle() != null ? player.getBackpackStyle().getDatabaseKey() : 1;
		runStatementAsync(querySaveBackpackStyle, player.getDatabaseKey(), backpackStyle, backpackStyle);
	}
}