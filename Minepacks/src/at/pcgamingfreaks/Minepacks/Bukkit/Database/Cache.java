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

import at.pcgamingfreaks.Database.Cache.ICacheablePlayer;
import at.pcgamingfreaks.Database.Cache.IPlayerCache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cache implements IPlayerCache
{
	private final Map<UUID, MinepacksPlayerData> players = new ConcurrentHashMap<>();

	public Cache() {}

	public void close()
	{
		clear();
	}

	public void clear()
	{
		players.clear();
	}

	public void cache(final @NotNull MinepacksPlayerData player)
	{
		players.put(player.getUUID(), player);
	}

	@Override
	public void unCache(final @NotNull ICacheablePlayer player)
	{
		players.remove(player.getUUID());
	}

	@Override
	public @Nullable MinepacksPlayerData getCachedPlayer(final @NotNull UUID uuid)
	{
		return players.get(uuid);
	}

	@Override
	public @NotNull Collection<? extends MinepacksPlayerData> getCachedPlayers()
	{
		return players.values();
	}
}