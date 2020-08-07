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

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum DatabaseType
{
	MYSQL,
	SQLITE,
	FILES,
	SHARED,
	UNKNOWN;

	public static @NotNull DatabaseType fromName(@NotNull String typeName)
	{
		typeName = typeName.toLowerCase(Locale.ENGLISH);
		switch(typeName)
		{
			case "mysql": return MYSQL;
			case "sqlite": return SQLITE;
			case "files": case "file": case "flat": return FILES;
			case "shared": case "external": case "global": return SHARED;
		}
		return UNKNOWN;
	}
}