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
import at.pcgamingfreaks.Database.ConnectionProvider.SQLiteConnectionProvider;
import at.pcgamingfreaks.Database.DBTools;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.MinepacksPlayerData;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
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
			stmt.execute("CREATE TABLE IF NOT EXISTS `backpack_players` (`player_id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` CHAR(16) NOT NULL , `uuid` CHAR(32) UNIQUE);");
			try
			{
				stmt.execute("ALTER TABLE `backpack_players` ADD COLUMN `uuid` CHAR(32);");
			}
			catch(SQLException ignored) {}
			stmt.execute("CREATE TABLE IF NOT EXISTS `backpacks` (`owner` INT UNSIGNED PRIMARY KEY, `itemstacks` BLOB, `version` INT DEFAULT 0, `lastupdate` DATE DEFAULT CURRENT_DATE);");
			try
			{
				stmt.execute("ALTER TABLE `backpacks` ADD COLUMN `version` INT DEFAULT 0;");
			}
			catch(SQLException ignored) {}
			try
			{
				stmt.execute("ALTER TABLE `backpacks` ADD COLUMN `lastupdate` DATE DEFAULT '2020-04-20';");
			}
			catch(SQLException ignored) {}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void updatePlayer(@NotNull Connection connection, @NotNull MinepacksPlayerData player) throws SQLException
	{
		DBTools.runStatement(connection, queryUpdatePlayerAdd, player.getName(), formatUUID(player.getUUID()));
		DBTools.runStatement(connection, "UPDATE `" + tablePlayers + "` SET `" + fieldPlayerName + "`=? WHERE `" + fieldPlayerUUID + "`=?;", player.getName(), formatUUID(player.getUUID()));
	}
}