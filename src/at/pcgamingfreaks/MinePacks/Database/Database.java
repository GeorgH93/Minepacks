/*
 *   Copyright (C) 2014-2017 GeorgH93
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

package at.pcgamingfreaks.MinePacks.Database;

import at.pcgamingfreaks.MinePacks.Backpack;
import at.pcgamingfreaks.MinePacks.MinePacks;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Database
{
	protected MinePacks plugin;

	protected boolean useUUIDs, useUUIDSeparators, bungeeMode;
	protected long maxAge;

	private HashMap<OfflinePlayer, Backpack> backpacks = new HashMap<>();
	protected InventorySerializer itsSerializer = new InventorySerializer();

	public Database(MinePacks mp)
	{
		plugin = mp;
		useUUIDSeparators = plugin.config.getUseUUIDSeparators();
		useUUIDs = plugin.config.getUseUUIDs();
		maxAge = plugin.config.getAutoCleanupMaxInactiveDays();
		bungeeMode = plugin.config.isBungeeCordModeEnabled();
	}

	public static Database getDatabase(MinePacks Plugin)
	{
		switch(Plugin.config.getDatabaseType().toLowerCase())
		{
			case "mysql":
				return new MySQL(Plugin);
			case "flat":
			case "file":
			case "files":
				return new Files(Plugin);
			case "sqlite":
			default:
				return new SQLite(Plugin);
		}
	}

	protected String getPlayerNameOrUUID(OfflinePlayer player)
	{
		if(useUUIDs)
		{
			return (useUUIDSeparators) ? player.getUniqueId().toString() : player.getUniqueId().toString().replace("-", "");
		}
		else
		{
			return player.getName();
		}
	}

	protected String getPlayerFormattedUUID(OfflinePlayer player)
	{
		if(useUUIDs)
		{
			return (useUUIDSeparators) ? player.getUniqueId().toString() : player.getUniqueId().toString().replace("-", "");
		}
		return null;
	}

	public Backpack getBackpack(OfflinePlayer player)
	{
		return (player == null) ? null : backpacks.get(player);
	}

	@SuppressWarnings("unused")
	public Backpack getBackpack(OfflinePlayer player, boolean loadedOnly)
	{
		if(player == null)
		{
			return null;
		}
		Backpack lbp = backpacks.get(player);
		if(lbp == null && !loadedOnly)
		{
			lbp = loadBackpack(player);
			if(lbp == null)
			{
				lbp = new Backpack(player);
			}
			backpacks.put(player, lbp);
		}
		return lbp;
	}

	public void getBackpack(final OfflinePlayer player, final Callback<Backpack> callback)
	{
		if(player == null)
		{
			return;
		}
		Backpack lbp = backpacks.get(player);
		if(lbp == null)
		{
			loadBackpack(player, new Callback<Backpack>()
			{
				@Override
				public void onResult(Backpack backpack)
				{
					backpacks.put(player, backpack);
					callback.onResult(backpack);
				}

				@Override
				public void onFail()
				{
					Backpack backpack = new Backpack(player);
					backpacks.put(player, backpack);
					callback.onResult(backpack);
				}
			});
		}
		else
		{
			callback.onResult(lbp);
		}
	}

	public void unloadBackpack(Backpack backpack)
	{
		backpacks.remove(backpack.getOwner());
	}

	public void asyncLoadBackpack(final OfflinePlayer player)
	{
		if(player != null && backpacks.get(player) == null)
		{
			loadBackpack(player, new Callback<Backpack>()
			{
				@Override
				public void onResult(Backpack backpack)
				{
					backpacks.put(player, backpack);
				}

				@Override
				public void onFail()
				{
					backpacks.put(player, new Backpack(player));
				}
			});
		}
	}

	// DB Functions
	public void close() { }

	public void updatePlayerAndLoadBackpack(Player player)
	{
		updatePlayer(player);
		if(!bungeeMode) asyncLoadBackpack(player);
	}

	public void updatePlayer(Player player) {}

	public void saveBackpack(Backpack backpack) {}

	protected Backpack loadBackpack(OfflinePlayer player) { return null; }

	protected void loadBackpack(final OfflinePlayer player, final Callback<Backpack> callback)
	{
		Backpack loadedBackpack = loadBackpack(player);
		if(loadedBackpack == null)
		{
			callback.onFail();
		}
		else
		{
			callback.onResult(loadedBackpack);
		}
	}

	public interface Callback<T>
	{
		void onResult(T done);

		void onFail();
	}
}