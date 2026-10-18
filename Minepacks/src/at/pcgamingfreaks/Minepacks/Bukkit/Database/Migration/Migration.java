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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Migration;

import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.DatabaseBackend;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Migration
{
	protected final Minepacks plugin;
	protected final DatabaseBackend oldDb;

	protected Migration(@NotNull Minepacks plugin, @NotNull DatabaseBackend oldDb)
	{
		this.plugin = plugin;
		this.oldDb = oldDb;
	}

	public abstract @Nullable MigrationResult migrate() throws Exception;
}