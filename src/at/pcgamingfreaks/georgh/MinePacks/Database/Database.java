/*
 *   Copyright (C) 2014-2015 GeorgH93
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

package at.pcgamingfreaks.georgh.MinePacks.Database;

import java.util.HashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MinePacks.Backpack;
import at.pcgamingfreaks.georgh.MinePacks.MinePacks;
import at.pcgamingfreaks.georgh.MinePacks.Database.ItemStackSerializer.InventorySerializer;

public class Database
{
	protected MinePacks plugin;
	
	protected boolean useUUIDs, useUUIDSeparators;
	protected long maxAge;
	
	private HashMap<OfflinePlayer, Backpack> backpacks = new HashMap<>();
	protected InventorySerializer itsSerializer = new InventorySerializer();
	
	public Database(MinePacks mp)
	{
		plugin = mp;
		useUUIDSeparators = plugin.config.getUseUUIDSeparators();
		useUUIDs = plugin.config.getUseUUIDs();
		maxAge				= plugin.config.getAutoCleanupMaxInactiveDays();
	}

	public static Database getDatabase(MinePacks Plugin)
	{
		switch(Plugin.config.getDatabaseType().toLowerCase())
		{
			case "mysql": return new MySQL(Plugin);
			case "flat":
			case "file":
			case "files": return new Files(Plugin);
			case "sqlite":
			default: return new SQLite(Plugin);
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
		if(player == null)
		{
			return null;
		}
		return backpacks.get(player);
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
					if(backpack == null)
					{
						backpack = new Backpack(player);
					}
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
					if(backpack == null)
					{
						backpack = new Backpack(player);
					}
					backpacks.put(player, backpack);
				}
			});
		}
	}
	
	// DB Functions
	public void close() { }

	public void updatePlayerAndLoadBackpack(Player player)
	{
		updatePlayer(player);
		asyncLoadBackpack(player);
	}
	
	public void updatePlayer(Player player) {}
	
	public void saveBackpack(Backpack backpack) {}
	
	protected Backpack loadBackpack(OfflinePlayer player) { return null; }

	protected void loadBackpack(final OfflinePlayer player, final Callback<Backpack> callback)
	{
		callback.onResult(loadBackpack(player));
	}

	public interface Callback<T>
	{
		void onResult(T done);
	}
}