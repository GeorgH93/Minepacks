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

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.DatabaseBackend;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.Files;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.MySQL;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.SQLite;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.UnCacheStrategies.OnDisconnect;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.UnCacheStrategies.UnCacheStrategie;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class Database implements Listener
{
	public static final String MESSAGE_UNKNOWN_DB_TYPE = ConsoleColor.RED + "Unknown database type \"%s\"!" + ConsoleColor.RESET;

	protected final Minepacks plugin;
	private final boolean bungeeCordMode;
	private final BackupHandler backupHandler;
	@Getter private final DatabaseBackend backend;
	private final Map<OfflinePlayer, Backpack> backpacks = new ConcurrentHashMap<>();
	private final UnCacheStrategie unCacheStrategie;

	public Database(final @NotNull Minepacks plugin)
	{
		this.plugin = plugin;
		bungeeCordMode = plugin.getConfiguration().isBungeeCordModeEnabled();
		unCacheStrategie = bungeeCordMode ? new OnDisconnect(this) : UnCacheStrategie.getUnCacheStrategie(this);
		backupHandler = new BackupHandler(plugin);
		backend = getDatabaseBackend(plugin);
		if(!available()) return;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void close()
	{
		HandlerList.unregisterAll(this);
		backend.setAsyncSave(false);
		backpacks.forEach((key, value) -> value.closeAll());
		backpacks.clear();
		unCacheStrategie.close();
		backend.close();
	}

	public boolean available()
	{
		return backend != null;
	}

	public static @Nullable ConnectionProvider getGlobalConnectionProvider(final @NotNull Logger logger)
	{
		/*if[STANDALONE]
		logger.warning(ConsoleColor.RED + "The shared database connection option is not available in standalone mode!" + ConsoleColor.RESET);
		return null;
		else[STANDALONE]*/
		at.pcgamingfreaks.PluginLib.Database.DatabaseConnectionPool pool = at.pcgamingfreaks.PluginLib.Bukkit.PluginLib.getInstance().getDatabaseConnectionPool();
		if(pool == null)
		{
			logger.warning(ConsoleColor.RED + "The shared connection pool is not initialized correctly!" + ConsoleColor.RESET);
			return null;
		}
		return pool.getConnectionProvider();
		/*end[STANDALONE]*/
	}

	protected static @Nullable DatabaseBackend getDatabaseBackend(Minepacks plugin)
	{
		try
		{
			DatabaseType dbType = plugin.getConfiguration().getDatabaseType();
			ConnectionProvider connectionProvider = dbType == DatabaseType.SHARED ? getGlobalConnectionProvider(plugin.getLogger()) : null;
			if(connectionProvider != null) dbType = DatabaseType.fromName(connectionProvider.getDatabaseType());
			DatabaseBackend databaseBackend;
			switch(dbType)
			{
				case MYSQL: databaseBackend = new MySQL(plugin, connectionProvider); break;
				case SQLITE: databaseBackend = new SQLite(plugin, connectionProvider); break;
				case FILES: databaseBackend = new Files(plugin); break;
				default: plugin.getLogger().warning(String.format(MESSAGE_UNKNOWN_DB_TYPE,  plugin.getConfiguration().getDatabaseTypeName())); return null;
			}
			//databaseBackend.init();
			return databaseBackend;
		}
		catch(IllegalStateException ignored) {}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void backup(@NotNull Backpack backpack)
	{
		backupHandler.backup(backpack);
	}

	public @Nullable ItemStack[] loadBackup(final String backupName)
	{
		return backupHandler.loadBackup(backupName);
	}

	public List<String> getBackups()
	{
		return backupHandler.getBackups();
	}

	public @NotNull Collection<Backpack> getLoadedBackpacks()
	{
		return backpacks.values();
	}

	/**
	 * Gets a backpack for a player. This only includes backpacks that are cached! Do not use it unless you are sure that you only want to use cached data!
	 *
	 * @param player The player who's backpack should be retrieved.
	 * @return The backpack for the player. null if the backpack is not in the cache.
	 */
	public @Nullable Backpack getBackpack(@Nullable OfflinePlayer player)
	{
		return (player == null) ? null : backpacks.get(player);
	}

	public void getBackpack(final OfflinePlayer player, final Callback<at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack> callback, final boolean createNewOnFail)
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
					if(createNewOnFail)
					{
						Backpack backpack = new Backpack(player);
						backpacks.put(player, backpack);
						callback.onResult(backpack);
					}
					else
					{
						callback.onFail();
					}
				}
			});
		}
		else
		{
			callback.onResult(lbp);
		}
	}

	public void getBackpack(final OfflinePlayer player, final Callback<at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack> callback)
	{
		getBackpack(player, callback, true);
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

	public void updatePlayerAndLoadBackpack(Player player)
	{
		updatePlayer(player);
		if(!bungeeCordMode) asyncLoadBackpack(player);
	}

	// DB Functions
	public void updatePlayer(final @NotNull Player player)
	{
		backend.updatePlayer(player);
	}

	public void saveBackpack(final @NotNull Backpack backpack)
	{
		backend.saveBackpack(backpack);
	}

	public void syncCooldown(final @NotNull Player player, final long time)
	{
		backend.syncCooldown(player, time);
	}

	public void getCooldown(final Player player, final Callback<Long> callback)
	{
		backend.getCooldown(player, callback);
	}

	protected void loadBackpack(final OfflinePlayer player, final Callback<Backpack> callback)
	{
		backend.loadBackpack(player, callback);
	}
}