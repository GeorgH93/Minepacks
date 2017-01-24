/*
 *   Copyright (C) 2016 GeorgH93
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

import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.UnCacheStrategies.UnCacheStrategie;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Database implements Listener
{
	protected static final String START_UUID_UPDATE = "Start updating database to UUIDs ...", UUIDS_UPDATED = "Updated %d accounts to UUIDs.";

	protected final Minepacks plugin;
	protected final InventorySerializer itsSerializer = new InventorySerializer();
	protected boolean useUUIDs, useUUIDSeparators;
	protected long maxAge;
	private final Map<OfflinePlayer, Backpack> backpacks = new ConcurrentHashMap<>();
	private final UnCacheStrategie unCacheStrategie;

	public Database(Minepacks mp)
	{
		plugin = mp;
		useUUIDSeparators = plugin.config.getUseUUIDSeparators();
		useUUIDs = plugin.config.getUseUUIDs();
		maxAge = plugin.config.getAutoCleanupMaxInactiveDays();
		unCacheStrategie = UnCacheStrategie.getUnCacheStrategie(this);
	}

	public void init()
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void close()
	{
		HandlerList.unregisterAll(this);
		unCacheStrategie.close();
	}

	public static Database getDatabase(Minepacks Plugin)
	{
		Database database;
		switch(Plugin.config.getDatabaseType().toLowerCase())
		{
			case "mysql":
				database = new MySQL(Plugin); break;
			case "flat":
			case "file":
			case "files":
				database = new Files(Plugin); break;
			case "sqlite":
			default:
				database = new SQLite(Plugin);
		}
		database.init();
		return database;
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

	public @NotNull Collection<Backpack> getLoadedBackpacks()
	{
		return backpacks.values();
	}

	public @Nullable Backpack getBackpack(@Nullable OfflinePlayer player)
	{
		return (player == null) ? null : backpacks.get(player);
	}

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

	@EventHandler
	public void onPlayerLoginEvent(PlayerJoinEvent event)
	{
		updatePlayerAndLoadBackpack(event.getPlayer());
	}

	// DB Functions
	public void updatePlayerAndLoadBackpack(Player player)
	{
		updatePlayer(player);
		asyncLoadBackpack(player);
	}

	public abstract void updatePlayer(Player player);

	public abstract void saveBackpack(Backpack backpack);

	public abstract void syncCooldown(Player player, long time);

	protected abstract Backpack loadBackpack(OfflinePlayer player);

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
}