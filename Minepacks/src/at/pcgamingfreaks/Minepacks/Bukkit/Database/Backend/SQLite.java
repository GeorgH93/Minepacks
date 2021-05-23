/*
 *   Copyright (C) 2021 GeorgH93
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

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Database.ConnectionProvider.SQLiteConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.MinepacksPlayerData;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Version;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;

public class SQLite extends SQL
{
	public static String getDbFile(final @NotNull Minepacks plugin)
	{
		return plugin.getDataFolder().getAbsolutePath() + File.separator + "backpack.db";
	}

	public SQLite(final @NotNull Minepacks plugin, final @Nullable ConnectionProvider connectionProvider) throws SQLException
	{
		super(plugin, (connectionProvider == null) ? new SQLiteConnectionProvider(plugin.getLogger(), plugin.getDescription().getName(), getDbFile(plugin)) : connectionProvider);
	}

	@Override
	protected void loadSettings()
	{
		// Set fixed settings
		useUUIDSeparators = false;

		syncCooldown = plugin.getConfiguration().isCommandCooldownSyncEnabled();
	}

	@Override
	protected void updateQueriesForDialect()
	{
		queryInsertBp = queryInsertBp.replaceAll("\\) VALUES \\(\\?,\\?,\\?", ",{FieldBPLastUpdate}) VALUES (?,?,?,DATE('now')");
		queryDeleteOldBackpacks = "DELETE FROM {TableBackpacks} WHERE {FieldBPLastUpdate} < DATE('now', '-{VarMaxAge} days')";
		queryUpdateBp = queryUpdateBp.replaceAll("\\{NOW}", "DATE('now')");
		querySyncCooldown = "INSERT OR REPLACE INTO {TableCooldowns} ({FieldCDPlayer},{FieldCDTime}) VALUES (?,?);";
		queryInsertPlayer = queryInsertPlayer.replace("INSERT IGNORE INTO", "INSERT OR IGNORE INTO");
		queryAddStyle = queryAddStyle.replace("INSERT IGNORE INTO", "INSERT OR IGNORE INTO");
		queryAddIDedStyle = queryAddIDedStyle.replace("REPLACE INTO", "INSERT OR REPLACE INTO");
		querySyncCooldown = "INSERT OR IGNORE INTO {TablePlayerSettings} ({FieldPSPlayerID},{FieldPSBackpackStyle}) VALUES (?,?); UPDATE {TablePlayerSettings} SET {FieldPSBackpackStyle}=? WHERE {FieldPSPlayerID}=?;";
	}

	@Override
	public @NotNull Connection getConnection() throws SQLException
	{
		Connection connection = super.getConnection();
		try(Statement statement = connection.createStatement())
		{
			statement.execute("PRAGMA foreign_keys = ON"); // We need foreign keys!
		}
		return connection;
	}

	private void doPHQuery(final @NotNull Statement statement, final @NotNull @Language("SQL") String query) throws SQLException
	{
		statement.execute(replacePlaceholders(query));
	}

	@SuppressWarnings("SqlResolve")
	@Override
	protected void checkDB()
	{
		try(Connection connection = getConnection(); Statement stmt = connection.createStatement())
		{
			Version dbVersion = getDatabaseVersion(stmt);

			// Create tables if they do not exist
			doPHQuery(stmt, "CREATE TABLE IF NOT EXISTS {TablePlayers} ({FieldPlayerID} INTEGER PRIMARY KEY AUTOINCREMENT, {FieldName} CHAR(16) NOT NULL, {FieldUUID} CHAR(32) UNIQUE);");
			doPHQuery(stmt, "CREATE TABLE IF NOT EXISTS {TableBackpacks} ({FieldBPOwner} INTEGER PRIMARY KEY, {FieldBPITS} BLOB, {FieldBPVersion} INT NOT NULL, {FieldBPLastUpdate} DATE NOT NULL," +
							"CONSTRAINT fk_{TableBackpacks}_{TablePlayers}_{FieldPlayerID} FOREIGN KEY ({FieldBPOwner}) REFERENCES {TablePlayers} ({FieldPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE);");
			if(syncCooldown)
			{
				doPHQuery(stmt, "CREATE TABLE IF NOT EXISTS {TableCooldowns} ({FieldCDPlayer} INTEGER PRIMARY KEY, {FieldCDTime} UNSIGNED BIG INT NOT NULL, " +
								"CONSTRAINT fk_{TableCooldowns}_{TablePlayers}_{FieldPlayerID} FOREIGN KEY ({FieldCDPlayer}) REFERENCES {TablePlayers} ({FieldPlayerID}) ON DELETE CASCADE ON UPDATE CASCADE);");
			}
			doPHQuery(stmt, "CREATE TABLE IF NOT EXISTS {TableBackpackStyles} ({FieldBSStyleID} INTEGER PRIMARY KEY AUTOINCREMENT, {FieldBSStyleName} TEXT NOT NULL UNIQUE);");
			doPHQuery(stmt, "CREATE TABLE IF NOT EXISTS {TablePlayerSettings} ({FieldPSPlayerID} INTEGER PRIMARY KEY, {FieldPSBackpackStyle} INTEGER DEFAULT 0," +
							"CONSTRAINT fk_{TablePlayerSettings}_{TableBackpackStyles}_{FieldBSStyleID} FOREIGN KEY ({FieldPSBackpackStyle}) REFERENCES {TableBackpackStyles} ({FieldBSStyleID}) ON DELETE SET DEFAULT ON UPDATE CASCADE);");

			if(dbVersion.olderThan(new Version("3.0-ALPHA-SNAPSHOT")))
			{ // Copy old data to new tables
				plugin.getLogger().info(ConsoleColor.YELLOW + "Migrating data to new table structure. Please do not stop the server till it is done!" + ConsoleColor.RESET);
				doPHQuery(stmt, "INSERT OR IGNORE INTO {TablePlayers} ({FieldPlayerID}, {FieldName}, {FieldUUID}) SELECT player_id, name, uuid FROM `backpack_players`;");
				doPHQuery(stmt, "INSERT OR IGNORE INTO {TableBackpacks} ({FieldBPOwner}, {FieldBPITS}, {FieldBPVersion}, {FieldBPLastUpdate}) SELECT owner, itemstacks, version, lastupdate FROM backpacks WHERE owner IN (SELECT {FieldPlayerID} FROM {TablePlayers});");
				plugin.getLogger().info(ConsoleColor.GREEN + "Data migrated successful!" + ConsoleColor.RESET);
			}

			DBTools.runStatement(connection, "INSERT OR REPLACE INTO `minepacks_metadata` (`key`, `value`) VALUES ('db_version',?);", plugin.getDescription().getVersion());
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	private @NotNull Version getDatabaseVersion(final @NotNull Statement stmt) throws SQLException
	{
		stmt.execute("CREATE TABLE IF NOT EXISTS `minepacks_metadata` (`key` CHAR(32) PRIMARY KEY NOT NULL, `value` TEXT);");
		try(ResultSet rs = stmt.executeQuery("SELECT `value` FROM `minepacks_metadata` WHERE `key`='db_version';"))
		{
			if(rs.next()) return new Version(rs.getString("value"));
		}
		try(ResultSet rs = stmt.executeQuery("SELECT `name` FROM `sqlite_master` WHERE `type`='table' AND `name`='backpack_players';"))
		{ // Check if old players table exists
			if(rs.next()) return new Version(2);
		}
		return plugin.getVersion();
	}

	@Override
	public void saveCooldown(final @NotNull MinepacksPlayerData player)
	{
		runStatementAsync(querySyncCooldown, player.getDatabaseKey(), new Timestamp(player.getCooldown()));
	}

	@Override
	public void saveBackpackStyle(final @NotNull MinepacksPlayerData player)
	{
		Object backpackStyle = player.getBackpackStyle() != null ? player.getBackpackStyle().getDatabaseKey() : null;
		runStatementAsync(querySaveBackpackStyle, player.getDatabaseKey(), backpackStyle, backpackStyle, player.getDatabaseKey());
	}
}