/*
 *   Copyright (C) 2014-2019 GeorgH93
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class Database
{
	private final File backupFolder;
	protected final MinePacks plugin;
	protected final InventorySerializer itsSerializer;
	private final HashMap<OfflinePlayer, Backpack> backpacks = new HashMap<>();

	protected boolean useUUIDs, useUUIDSeparators, bungeeMode;
	protected long maxAge;

	public Database(MinePacks mp)
	{
		plugin = mp;
		itsSerializer = new InventorySerializer(plugin.getLogger());
		useUUIDSeparators = plugin.config.getUseUUIDSeparators();
		useUUIDs = plugin.config.getUseUUIDs();
		maxAge = plugin.config.getAutoCleanupMaxInactiveDays();
		bungeeMode = plugin.config.isBungeeCordModeEnabled();

		backupFolder = new File(plugin.getDataFolder(), "backups" + File.separator + "oldFormat");
		if(!backupFolder.exists())
		{
			backupFolder.mkdirs();
		}
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

	public void closeAllBackpacks()
	{
		// Ensure that all backpacks are closed and saved before killing the database
		for(Map.Entry<OfflinePlayer, Backpack> backpackEntry : backpacks.entrySet())
		{
			backpackEntry.getValue().closeAll();
		}
	}

	// DB Functions
	public void close()
	{
		closeAllBackpacks();
		backpacks.clear();
	}

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

	protected abstract void rewrite();

	protected void backup(int version, byte[] data, String name)
	{
		try(FileOutputStream fos = new FileOutputStream(new File(backupFolder, name)))
		{
			fos.write(version);
			fos.write(data);
			fos.flush();
		}
		catch(Exception e)
		{
			plugin.log.warning("Failed to write backup of backpack: " + name);
			e.printStackTrace();
		}
	}
}