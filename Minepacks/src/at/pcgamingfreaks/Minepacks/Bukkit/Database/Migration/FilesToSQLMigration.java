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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Migration;

import at.pcgamingfreaks.Minepacks.Bukkit.Database.Files;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FilesToSQLMigration extends ToSQLMigration
{
	private final @Language("SQL") String queryInsertUsers, queryInsertBackpacks;
	private final File saveFolder;

	protected FilesToSQLMigration(@NotNull Minepacks plugin, @NotNull Files oldDb, @NotNull String dbType, boolean global) throws Exception
	{
		super(plugin, oldDb, dbType, global);
		saveFolder = new File(this.plugin.getDataFolder(), Files.FOLDER_NAME);

		queryInsertUsers = replacePlaceholders(newDb, "INSERT INTO {TablePlayers} ({FieldUUID},{FieldName}) VALUES (?,?);");
		queryInsertBackpacks = replacePlaceholders(newDb, "INSERT INTO {TableBackpacks} ({FieldBPOwner},{FieldBPITS},{FieldBPVersion}) VALUES (?,?,?);");
	}

	@Override
	public @Nullable MigrationResult migrate() throws Exception
	{
		File[] allFiles = saveFolder.listFiles((dir, name) -> name.endsWith(Files.EXT));
		if(allFiles == null) return null;
		try(Connection connection = newDb.getConnection(); PreparedStatement statementInsertUser = connection.prepareStatement(queryInsertUsers, PreparedStatement.RETURN_GENERATED_KEYS);
		    PreparedStatement statementInsertBackpack = connection.prepareStatement(queryInsertBackpacks))
		{
			int migrated = 0;
			for(File file : allFiles)
			{
				String name = file.getName().substring(0, file.getName().length() - Files.EXT.length());
				statementInsertUser.setString(1, name);
				statementInsertUser.setString(2, "UNKNOWN");
				statementInsertUser.executeUpdate();
				try(ResultSet rs = statementInsertUser.getGeneratedKeys())
				{
					if(rs.next())
					{
						try(FileInputStream fis = new FileInputStream(file))
						{
							int version = fis.read();
							byte[] data = new byte[(int) (file.length() - 1)];
							int readCount = fis.read(data);
							if(file.length() - 1 != readCount) plugin.getLogger().warning("Problem reading file, read " + readCount + " of " + (file.length() - 1) + " bytes.");
							statementInsertBackpack.setInt(1, rs.getInt(1));
							statementInsertBackpack.setBytes(2, data);
							statementInsertBackpack.setInt(3, version);
							statementInsertBackpack.executeUpdate();
							migrated++;
						}
					}
				}

			}
			return new MigrationResult("Migrated " + migrated + " backpacks from Files to " + newDb.getClass().getSimpleName(), MigrationResult.MigrationResultType.SUCCESS);
		}
		finally
		{
			newDb.close();
		}
	}
}