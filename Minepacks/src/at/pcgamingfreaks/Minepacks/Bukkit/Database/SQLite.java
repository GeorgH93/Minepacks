/*
 *   Copyright (C) 2023 GeorgH93
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
import at.pcgamingfreaks.Database.ConnectionProvider.SQLiteConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Version;
import org.bukkit.entity.Player;
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
	public SQLite(final @NotNull Minepacks plugin, final @Nullable ConnectionProvider connectionProvider)
	{
		super(plugin, (connectionProvider == null) ? new SQLiteConnectionProvider(plugin.getLogger(), plugin.getDescription().getName(), getDbFile(plugin)) : connectionProvider);
	}

	@Override
	protected void loadSettings()
	{
		// Set table and field names to fixed values to prevent users from destroying old databases.
		fieldPlayerID     = "player_id";
		fieldPlayerName   = "name";
		fieldPlayerUUID   = "uuid";
		fieldBpOwner      = "owner";
		//noinspection SpellCheckingInspection
		fieldBpIts        = "itemstacks";
		fieldBpVersion    = "version";
		fieldBpLastUpdate = "lastupdate";
		tablePlayers      = "backpack_players";
		tableBackpacks    = "backpacks";
		tableCooldowns    = "backpack_cooldowns";
		fieldCdPlayer     = "player_id";
		fieldCdTime       = "time";
		// Set fixed settings
		useUUIDSeparators = false;
		syncCooldown = false;
	}

	@Override
	protected void updateQueriesForDialect()
	{
		queryInsertBp = queryInsertBp.replaceAll("\\) VALUES \\(\\?,\\?,\\?", ",{FieldBPLastUpdate}) VALUES (?,?,?,DATE('now')");
		queryDeleteOldBackpacks = "DELETE FROM {TableBackpacks} WHERE {FieldBPLastUpdate} < DATE('now', '-{VarMaxAge} days')";
		queryUpdateBp = queryUpdateBp.replaceAll("\\{NOW}", "DATE('now')");
		queryUpdatePlayerAdd = "INSERT OR IGNORE INTO {TablePlayers} ({FieldName},{FieldUUID}) VALUES (?,?);";
	}

	@SuppressWarnings("SqlResolve")
	@Override
	protected void checkDB()
	{
		try(Connection connection = getConnection(); Statement stmt = connection.createStatement())
		{
			Version dbVersion = getDatabaseVersion(stmt);

			stmt.execute("CREATE TABLE IF NOT EXISTS `backpack_players` (`player_id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` CHAR(16) NOT NULL , `uuid` CHAR(32) UNIQUE);");
			try
			{
				stmt.execute("ALTER TABLE `backpack_players` ADD COLUMN `uuid` CHAR(32);");
			}
			catch(SQLException ignored) {}
			stmt.execute("CREATE TABLE IF NOT EXISTS `backpacks` (`owner` INT UNSIGNED PRIMARY KEY, `itemstacks` BLOB, `version` INT DEFAULT 0, `lastupdate` DATE);");
			try
			{
				stmt.execute("ALTER TABLE `backpacks` ADD COLUMN `version` INT DEFAULT 0;");
			}
			catch(SQLException ignored) {}
			try
			{
				stmt.execute("ALTER TABLE `backpacks` ADD COLUMN `lastupdate` DATE DEFAULT '2020-09-24';");
			}
			catch(SQLException ignored) {}

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
		return new Version("0");
	}

	@Override
	public void updatePlayer(final Player player)
	{
		Minepacks.getScheduler().runAsync(task -> {
			runStatement(queryUpdatePlayerAdd, player.getName(), getPlayerFormattedUUID(player));
			runStatement("UPDATE `" + tablePlayers + "` SET `" + fieldPlayerName + "`=? WHERE `" + fieldPlayerUUID + "`=?;", player.getName(), getPlayerFormattedUUID(player));
		});
	}
}