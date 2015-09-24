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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Date;

import javax.swing.filechooser.FileFilter;

import at.pcgamingfreaks.UUIDConverter;
import org.bukkit.OfflinePlayer;

import at.pcgamingfreaks.georgh.MinePacks.Backpack;
import at.pcgamingfreaks.georgh.MinePacks.MinePacks;

public class Files extends Database
{
	private File saveFolder;
	private final String ext =  ".backpack";
	
	public Files(MinePacks mp)
	{
		super(mp);
		maxAge *= 24 * 3600000L;
		saveFolder = new File(plugin.getDataFolder(), "backpacks");
		if(!saveFolder.exists())
		{
			//noinspection ResultOfMethodCallIgnored
			saveFolder.mkdirs();
		}
		else
		{
			CheckFiles();
		}
	}
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void CheckFiles()
	{
		File[] allFiles = saveFolder.listFiles(new BackpackFileFilter());
		int len;
		for (File file : allFiles)
		{
			if(maxAge > 0 && (new Date()).getTime() - file.lastModified() > maxAge) // Check if the file is older then x days
			{
				file.delete(); // Delete old files
				continue; // We don't have to check if the file name is correct cause we have the deleted the file
			}
			len = file.getName().length() - ext.length();
			if(useUUIDs) // Use UUID-based saving
			{
				if(len <= 16) // It's a player name
				{
					file.renameTo(new File(saveFolder, UUIDConverter.getUUIDFromName(file.getName().substring(0, len), true, useUUIDSeparators) + ext));
				}
				else // It's an UUID
				{
					if(file.getName().contains("-"))
					{
						if(!useUUIDSeparators)
						{
							file.renameTo(new File(saveFolder, file.getName().replaceAll("-", "")));
						}
					}
					else
					{
						if(useUUIDSeparators)
						{
							file.renameTo(new File(saveFolder, file.getName().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})" + ext, "$1-$2-$3-$4-$5" + ext)));
						}
					}
				}
			}
			else // Use name-based saving
			{
				if(len > 16) // We only have to rename it if it's name is more than 16 chars (minecraft max player name length)
				{
					file.renameTo(new File(saveFolder, UUIDConverter.getNameFromUUID(file.getName().substring(0, len)) + ext));
				}
			}
		}
	}
	
	private String getFileName(OfflinePlayer player)
	{
		return getPlayerNameOrUUID(player) + ext;
	}
	
	// DB Functions
	@Override
	public void saveBackpack(Backpack backpack)
	{
		File save = new File(saveFolder, getFileName(backpack.getOwner()));
		try
		{
			FileOutputStream fos = new FileOutputStream(save);
			fos.write(itsSerializer.getUsedSerializer());
			fos.write(itsSerializer.serialize(backpack.getInventory()));
			fos.flush();
			fos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Backpack loadBackpack(OfflinePlayer player)
	{
		File save = new File(saveFolder, getFileName(player));
		try
		{
			if(save.exists())
			{
				FileInputStream fis = new FileInputStream(save);
				int v = fis.read();
				byte[] out = new byte[(int)(save.length()-1)];
				//noinspection ResultOfMethodCallIgnored
				fis.read(out);
				fis.close();
				return new Backpack(player, itsSerializer.deserialize(out, v), -1);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	class BackpackFileFilter extends FileFilter implements FilenameFilter
	{
		String description, extension;

		public BackpackFileFilter()
		{
		    description = "Filters for Minepack backpack files.";
		    extension = "backpack";
		}

		@Override
		public String getDescription()
		{
			return description;
		}

		@Override
		public boolean accept(File file)
		{
		    if (!file.isDirectory())
		    {
		    	String path = file.getAbsolutePath().toLowerCase();
	    		if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.'))
	    		{
	    			return true;
	    		}
		    }
		    return false;
		}

		@Override
		public boolean accept(File dir, String name)
		{
			return (name.endsWith(extension) && (name.charAt(name.length() - extension.length() - 1)) == '.');
		}
	}
}