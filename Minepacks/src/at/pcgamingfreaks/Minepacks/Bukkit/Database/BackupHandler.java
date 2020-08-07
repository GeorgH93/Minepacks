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
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend.Files;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class BackupHandler
{
	@Setter(AccessLevel.PRIVATE) @Getter private static BackupHandler instance;

	private final Logger logger;
	private final InventorySerializer itsSerializer;
	private final boolean useUUIDSeparators;
	private final File backupFolder;

	public BackupHandler(final @NotNull Minepacks plugin)
	{
		logger = plugin.getLogger();
		itsSerializer = new InventorySerializer(logger);
		useUUIDSeparators = plugin.getConfiguration().getUseUUIDSeparators();

		backupFolder = new File(plugin.getDataFolder(), "backups");
		if(!backupFolder.exists() && !backupFolder.mkdirs()) plugin.getLogger().info("Failed to create backups folder.");

		setInstance(this);
	}

	public void backup(final @NotNull Backpack backpack)
	{
		final String formattedUUID = (useUUIDSeparators) ? backpack.getOwner().getUniqueId().toString() : backpack.getOwner().getUniqueId().toString().replace("-", "");
		writeBackup(backpack.getOwner().getName(), formattedUUID, itsSerializer.getUsedSerializer(), itsSerializer.serialize(backpack.getInventory()));
	}

	public @Nullable ItemStack[] loadBackup(final String backupName)
	{
		File backup = new File(backupFolder, backupName + Files.EXT);
		return Files.readFile(itsSerializer, backup, logger);
	}

	public void writeBackup(@Nullable String userName, @NotNull String userIdentifier, final int usedSerializer, final @NotNull byte[] data)
	{
		if(userIdentifier.equalsIgnoreCase(userName)) userName = null;
		if(userName != null) userIdentifier = userName + "_" + userIdentifier;
		final File save = new File(backupFolder, userIdentifier + "_" + System.currentTimeMillis() + Files.EXT);
		try(FileOutputStream fos = new FileOutputStream(save))
		{
			fos.write(usedSerializer);
			fos.write(data);
			logger.info("Backup of the backpack has been created: " + save.getAbsolutePath());
		}
		catch(Exception e)
		{
			logger.warning(ConsoleColor.RED + "Failed to write backup! Error: " + e.getMessage() + ConsoleColor.RESET);
		}
	}

	public List<String> getBackups()
	{
		File[] files = backupFolder.listFiles((dir, name) -> name.endsWith(Files.EXT));
		if(files != null)
		{
			ArrayList<String> backups = new ArrayList<>(files.length);
			for(File file : files)
			{
				if(!file.isFile()) continue;
				backups.add(file.getName().replaceAll(Files.EXT_REGEX, ""));
			}
			return backups;
		}
		return new ArrayList<>();
	}
}