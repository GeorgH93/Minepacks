/*
 *   Copyright (C) 2018 GeorgH93
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

import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.PluginLib.Database.DatabaseConnectionPool;

import com.zaxxer.hikari.HikariConfig;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteShared extends SQLite
{
	private DatabaseConnectionPool pool;

	protected SQLiteShared(Minepacks minepacks, DatabaseConnectionPool pool)
	{
		super(minepacks);
		this.pool = pool;
	}

	@Override
	protected HikariConfig getPoolConfig()
	{
		return null;
	}

	@Override
	public @NotNull Connection getConnection() throws SQLException
	{
		Connection connection = pool.getConnection();
		try(Statement statement = connection.createStatement())
		{
			statement.execute("PRAGMA foreign_keys = ON"); // We need foreign keys!
		}
		return connection;
	}
}