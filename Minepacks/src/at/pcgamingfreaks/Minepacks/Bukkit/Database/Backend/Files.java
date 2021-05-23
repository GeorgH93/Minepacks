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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend;

import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.MinepacksPlayerData;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.UUIDConverter;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Files extends DatabaseBackend
{
	public static final String EXT = ".backpack", EXT_REGEX = "\\.backpack", FOLDER_NAME = "backpacks";

	private final File saveFolder;
	
	public Files(final @NotNull Minepacks plugin)
	{
		super(plugin);
		maxAge *= 24 * 3600000L;
		saveFolder = new File(plugin.getDataFolder(), FOLDER_NAME);
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

	private void checkFiles()
	{
		File[] allFiles = saveFolder.listFiles((dir, name) -> name.endsWith(EXT));
		if(allFiles == null) return;
		for (File file : allFiles)
		{
			if(maxAge > 0 && System.currentTimeMillis() - file.lastModified() > maxAge) // Check if the file is older then x days
			{
				if(!file.delete())
				{
					plugin.getLogger().warning("Failed to delete file (" + file.getAbsolutePath() + ").");
				}
				continue; // We don't have to check if the file name is correct cause we have the deleted the file
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

	private void savePlayer(final @NotNull MinepacksPlayerData player)
	{
		if(!player.isLoaded()) return;
		BackpackFile backpackFile = new BackpackFile(player);
		backpackFile.writeFile((File) player.getDatabaseKey());
		//TODO handle player settings
	}
	
	// DB Functions
	@Override
	public void saveBackpack(final @NotNull Backpack backpack)
	{
		savePlayer(backpack.getOwner());
	}

	@Override
	public void saveBackpackStyle(@NotNull MinepacksPlayerData player)
	{
		savePlayer(player);
	}

	@Override
	public void loadPlayer(final @NotNull MinepacksPlayerData player)
	{
		File file = new File(saveFolder, formatUUID(player.getUUID()) + EXT);
		Backpack bp = null;
		if(file.exists())
		{
			BackpackFile backpackFile = BackpackFile.loadFile(file);
			if(backpackFile != null)
				bp = new Backpack(player, backpackFile.getItemStacks()[0]);
		}
		if(bp == null) bp = new Backpack(player);
		player.setLoaded(file, 0, null); // TODO
		player.setBackpack(bp);
	}

	@Override
	public void loadBackpack(final @NotNull MinepacksPlayerData player) {} // Already loaded with the player
}