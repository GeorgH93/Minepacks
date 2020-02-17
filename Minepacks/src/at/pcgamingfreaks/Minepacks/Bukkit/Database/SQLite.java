/*
 *   Copyright (C) 2019 GeorgH93
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
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Bukkit;
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
	//TODO add cooldown sync table
	public SQLite(@NotNull Minepacks plugin, @Nullable ConnectionProvider connectionProvider)
	{
		super(plugin, (connectionProvider == null) ? new SQLiteConnectionProvider(plugin.getLogger(), plugin.getDescription().getName(), plugin.getDataFolder().getAbsolutePath() + File.separator + "backpack.db") : connectionProvider);
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
		//noinspection SpellCheckingInspection
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
	protected void updateQuerysForDialect()
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
			stmt.execute("CREATE TABLE IF NOT EXISTS `backpacks` (`owner` INT UNSIGNED PRIMARY KEY, `itemstacks` BLOB, `version` INT DEFAULT 0);");
			try
			{
				stmt.execute("ALTER TABLE `backpacks` ADD COLUMN `version` INT DEFAULT 0;");
			}
			catch(SQLException ignored) {}
			try(ResultSet rs = stmt.executeQuery("SELECT DATE('now');"))
			{
				rs.next();
				stmt.execute("ALTER TABLE `backpacks` ADD COLUMN `lastupdate` DATE DEFAULT '" + rs.getString(1) + "';");
			}
			catch(SQLException ignored) {}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void updatePlayer(final Player player)
	{
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			runStatement(queryUpdatePlayerAdd, player.getName(), getPlayerFormattedUUID(player));
			runStatement("UPDATE `" + tablePlayers + "` SET `" + fieldPlayerName + "`=? WHERE `" + fieldPlayerUUID + "`=?;", player.getName(), getPlayerFormattedUUID(player));
		});
	}
}