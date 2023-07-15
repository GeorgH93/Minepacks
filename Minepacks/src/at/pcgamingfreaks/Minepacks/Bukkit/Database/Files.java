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

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.UUIDConverter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Files extends Database
{
	public static final String EXT = ".backpack", EXT_REGEX = "\\.backpack", FOLDER_NAME = "backpacks";

	private final File saveFolder;
	
	public Files(Minepacks plugin)
	{
		super(plugin);
		maxAge *= 24 * 3600000L;
		saveFolder = new File(this.plugin.getDataFolder(), FOLDER_NAME);
		if(!saveFolder.exists())
		{
			if(!saveFolder.mkdirs())
			{
				plugin.getLogger().warning("Failed to create save folder (" + saveFolder.getAbsolutePath() + ").");
			}
		}
		else
		{
			checkFiles();
		}
	}

	@Override
	public void updatePlayer(Player player)
	{
		// Files are stored with the users name or the uuid, there is no reason to update anything
	}

	private void checkFiles()
	{
		File[] allFiles = saveFolder.listFiles((dir, name) -> name.endsWith(EXT));
		if(allFiles == null) return;
		for (File file : allFiles)
		{
			if(maxAge > 0 && System.currentTimeMillis() - file.lastModified() > maxAge) // Check if the file is older than x days
			{
				if(!file.delete())
				{
					plugin.getLogger().warning("Failed to delete file (" + file.getAbsolutePath() + ").");
				}
				continue; // We don't have to check if the file name is correct because we have the deleted the file
			}
			int len = file.getName().length() - EXT.length();
			if(len <= 16) // It's a player name
			{
				if(!file.renameTo(new File(saveFolder, UUIDConverter.getUUIDFromName(file.getName().substring(0, len), onlineUUIDs, useUUIDSeparators) + EXT)))
				{
					plugin.getLogger().warning("Failed to rename file (" + file.getAbsolutePath() + ").");
				}
			}
			else // It's an UUID
			{
				if(file.getName().contains("-"))
				{
					if(!useUUIDSeparators)
					{
						if(!file.renameTo(new File(saveFolder, file.getName().replaceAll("-", ""))))
						{
							plugin.getLogger().warning("Failed to rename file (" + file.getAbsolutePath() + ").");
						}
					}
				}
				else
				{
					if(useUUIDSeparators)
					{
						if(!file.renameTo(new File(saveFolder, file.getName().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})" + EXT_REGEX, "$1-$2-$3-$4-$5" + EXT))))
						{
							plugin.getLogger().warning("Failed to rename file (" + file.getAbsolutePath() + ").");
						}
					}
				}
			}
		}
	}
	
	private String getFileName(UUID uuid)
	{
		return getPlayerFormattedUUID(uuid) + EXT;
	}
	
	// DB Functions
	@Override
	public void saveBackpack(Backpack backpack)
	{
		File save = new File(saveFolder, getFileName(backpack.getOwnerId()));
		try(FileOutputStream fos = new FileOutputStream(save))
		{
			fos.write(itsSerializer.getUsedSerializer());
			fos.write(itsSerializer.serialize(backpack.getInventory()));
			fos.flush();
		}
		catch(Exception e)
		{
			plugin.getLogger().log(Level.SEVERE, "Failed to save backpack.", e);
		}
	}

	@Override
	protected void loadBackpack(final OfflinePlayer player, final Callback<Backpack> callback)
	{ //TODO this needs to be done async!
		File save = new File(saveFolder, getFileName(player));
		ItemStack[] itemStacks = readFile(itsSerializer, save, plugin.getLogger());
		if(itemStacks != null)
		{
			callback.onResult(new Backpack(player, itemStacks, -1));
		}
		else
		{
			callback.onFail();
		}
	}

	protected static @Nullable ItemStack[] readFile(@NotNull InventorySerializer itsSerializer, @NotNull File file, @NotNull Logger logger)
	{
		if(file.exists())
		{
			try(FileInputStream fis = new FileInputStream(file))
			{
				int version = fis.read();
				byte[] out = new byte[(int) (file.length() - 1)];
				int readCount = fis.read(out);
				if(file.length() - 1 != readCount) logger.warning("Problem reading file, read " + readCount + " of " + (file.length() - 1) + " bytes.");
				return itsSerializer.deserialize(out, version);
			}
			catch(Exception e)
			{
				logger.log(Level.WARNING, "Failed to read backpack.", e);
			}
		}
		return null;
	}
}