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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite extends SQL
{
	public static String getDbFile(final @NotNull Minepacks plugin)
	{
		return plugin.getDataFolder().getAbsolutePath() + File.separator + "backpack.db";
	}

	//TODO add cooldown sync table
	public SQLite(final @NotNull Minepacks plugin, final @Nullable ConnectionProvider connectionProvider) throws SQLException
	{
		super(plugin, (connectionProvider == null) ? new SQLiteConnectionProvider(plugin.getLogger(), plugin.getDescription().getName(), getDbFile(plugin)) : connectionProvider);
	}

	@Override
	protected void loadSettings()
	{
		// Set table and field names to fixed values to prevent users from destroying old databases.
		fieldPlayerID     = "player_id";
		fieldBpOwner      = "owner";
		//noinspection SpellCheckingInspection
		fieldBpIts        = "itemstacks";
		// Set fixed settings
		useUUIDSeparators = false;

		tableCooldowns = "minepacks_cooldown";
	}

	@Override
	protected void updateQueriesForDialect()
	{
		queryInsertBp = queryInsertBp.replaceAll("\\) VALUES \\(\\?,\\?,\\?", ",{FieldBPLastUpdate}) VALUES (?,?,?,DATE('now')");
		queryDeleteOldBackpacks = "DELETE FROM {TableBackpacks} WHERE {FieldBPLastUpdate} < DATE('now', '-{VarMaxAge} days')";
		queryUpdateBp = queryUpdateBp.replaceAll("\\{NOW}", "DATE('now')");
		queryUpdatePlayerAdd = "INSERT OR IGNORE INTO {TablePlayers} ({FieldName},{FieldUUID}) VALUES (?,?);";
	}

	private void tryQuery(final @NotNull Statement statement, final @NotNull @Language("SQL") String query)
	{
		try
		{
			statement.execute(query);
		}
		catch(SQLException ignored) {}
	}

	@SuppressWarnings("SqlResolve")
	@Override
	protected void checkDB()
	{
		try(Connection connection = getConnection(); Statement stmt = connection.createStatement())
		{
			Version dbVersion = getDatabaseVersion(stmt);
			if(dbVersion.olderThan(new Version("3.0-PRE-ALPHA-SNAPSHOT")))
			{
				tryQuery(stmt, "ALTER TABLE `backpack_players` ADD COLUMN `uuid` CHAR(32);");
				tryQuery(stmt, "ALTER TABLE `backpacks` ADD COLUMN `version` INT DEFAULT 0;");
				tryQuery(stmt, "ALTER TABLE `backpacks` ADD COLUMN `lastupdate` DATE DEFAULT '2020-09-24';");
			}

			stmt.execute("CREATE TABLE IF NOT EXISTS `backpack_players` (`player_id` INT UNSIGNED PRIMARY KEY AUTOINCREMENT, `name` CHAR(16) NOT NULL , `uuid` CHAR(32) UNIQUE);");
			stmt.execute("CREATE TABLE IF NOT EXISTS `backpacks` (`owner` INT UNSIGNED PRIMARY KEY, `itemstacks` BLOB, `version` INT DEFAULT 0, `lastupdate` DATE DEFAULT CURRENT_DATE);");
			stmt.execute("CREATE TABLE IF NOT EXISTS `minepacks_cooldown` (`id` INT UNSIGNED PRIMARY KEY, `time` UNSIGNED BIG INT NOT NULL, " +
					             "CONSTRAINT fk_minepacks_cooldown_minepacks_players FOREIGN KEY (id) REFERENCES backpack_players (player_id) ON DELETE CASCADE ON UPDATE CASCADE);");

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
		try(ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='backpack_players';"))
		{ // Check if players table exists
			if(rs.next()) return new Version(2);
		}
		return plugin.getVersion();
	}

	@Override
	protected void updatePlayer(@NotNull Connection connection, @NotNull MinepacksPlayerData player) throws SQLException
	{
		DBTools.runStatement(connection, queryUpdatePlayerAdd, player.getName(), formatUUID(player.getUUID()));
		DBTools.runStatement(connection, "UPDATE `" + tablePlayers + "` SET `" + fieldPlayerName + "`=? WHERE `" + fieldPlayerUUID + "`=?;", player.getName(), formatUUID(player.getUUID()));
	}
}