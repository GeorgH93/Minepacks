/*
 *   Copyright (C) 2021 GeorgH93
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

import at.pcgamingfreaks.Bukkit.Database.Cache.UnCacheStrategies.UnCacheStrategyMaker;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.Cache.BaseUnCacheStrategy;
import at.pcgamingfreaks.Database.ConnectionProvider.ConnectionProvider;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Events.MinepacksPlayerJoinEvent;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.DatabaseBackend;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.Files;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.MySQL;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.SQLite;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Enums.DatabaseType;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksPlayerExtended;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

import java.util.List;
import java.util.logging.Logger;

public final class Database implements Listener
{
	public static final String MESSAGE_UNKNOWN_DB_TYPE = ConsoleColor.RED + "Unknown database type \"%s\"!" + ConsoleColor.RESET;

	private final boolean bungeeCordMode, syncCooldown;
	private final BackupHandler backupHandler;
	@Getter private final DatabaseBackend backend;
	private final Cache cache = new Cache();
	private final BaseUnCacheStrategy unCacheStrategy;

	public Database(final @NotNull Minepacks plugin)
	{
		bungeeCordMode = plugin.getConfiguration().isBungeeCordModeEnabled();
		syncCooldown = plugin.getConfiguration().isCommandCooldownSyncEnabled();
		unCacheStrategy = UnCacheStrategyMaker.make(plugin, cache, plugin.getConfiguration());
		backupHandler = new BackupHandler(plugin);
		backend = getDatabaseBackend(plugin);
		if(!available()) return;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void close()
	{
		HandlerList.unregisterAll(this);
		backend.setAsyncSave(false);
		unCacheStrategy.close();
		cache.getCachedPlayers().forEach(player -> {
			Backpack bp = player.getBackpack();
			if(bp != null) bp.closeAll();
		}); //TODO change when multi-page backpacks are added
		cache.close();
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
			plugin.getLogger().severe("Failed to create database backend!");
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

	public @NotNull List<String> getBackups()
	{
		return backupHandler.getBackups();
	}

	private void loadPlayer(final @NotNull MinepacksPlayerData player)
	{
		if(syncCooldown)
			player.setCooldownData(System.currentTimeMillis());

		backend.loadPlayer(player);

		player.notifyOnLoad(p -> {
			if(!bungeeCordMode)
			{
				loadBackpack(player);
			}
		});
	}

	public void loadBackpack(final @NotNull MinepacksPlayerData player)
	{
		player.setBackpackLoadingRequested(true);
		backend.loadBackpack(player);
	}

	@EventHandler
	public void onPlayerLoginEvent(final @NotNull PlayerJoinEvent event)
	{
		getPlayer(event.getPlayer()) // Trigger player data prefetch
				.notifyOnLoad(player -> {  // Trigger MinepacksPlayerJoinEvent
					if(player.isOnline())
						Bukkit.getPluginManager().callEvent(new MinepacksPlayerJoinEvent(player));
				});
	}

	public @NotNull MinepacksPlayerExtended getPlayer(final @NotNull OfflinePlayer offlinePlayer)
	{
		MinepacksPlayerData player = cache.getCachedPlayer(offlinePlayer.getUniqueId());
		if(player != null) return player;
		player = new MinepacksPlayerData(offlinePlayer);
		cache.cache(player);
		loadPlayer(player);
		return player;
	}

	public @Nullable MinepacksPlayerExtended getPlayerCached(final @NotNull OfflinePlayer offlinePlayer)
	{
		return cache.getCachedPlayer(offlinePlayer.getUniqueId());
	}

	// DB Functions
	public void saveBackpack(final @NotNull Backpack backpack)
	{
		backend.saveBackpack(backpack);
	}

	public void saveCooldown(final @NotNull MinepacksPlayerData player)
	{
		if(syncCooldown)
			backend.saveCooldown(player);
	}
}