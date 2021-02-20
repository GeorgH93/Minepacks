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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend;

import at.pcgamingfreaks.Minepacks.Bukkit.Database.InventorySerializer;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.MinepacksPlayerData;
import at.pcgamingfreaks.Minepacks.Bukkit.MagicValues;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.io.*;

@Data
@AllArgsConstructor
public final class BackpackFile
{
	private static final int FILE_FORMAT_VERSION = 0x10;
	@Setter private static InventorySerializer inventorySerializer;

	private String ownerName;
	private String backpackIcon;
	private ItemStack[][] itemStacks;

	public BackpackFile(final @NotNull MinepacksPlayerData player)
	{
		ownerName = player.getName();
		backpackIcon = player.getBackpackStyleName();
		itemStacks = new ItemStack[][] { player.getBackpack().getInventory().getContents() };
	}

	//region read file
	public static BackpackFile loadFile(final @NotNull File file)
	{
		try(FileInputStream fis = new FileInputStream(file))
		{
			int version = fis.read();
			if(version > 0x0f) return load(fis, file, version & 0xf0);
			else return loadLegacy(fis, file, version);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private static BackpackFile load(final @NotNull FileInputStream fis, final @NotNull File file, final int version) throws IOException
	{
		try(DataInputStream dataStream = new DataInputStream(fis))
		{
			String owner = dataStream.readUTF();
			String icon = dataStream.readUTF();
			int pages = dataStream.readInt();
			ItemStack[][] itemStacks = new ItemStack[pages][];
			for(int page = 0; page < pages; page++)
			{
				int dataSize = dataStream.readInt();
				byte[] data = new byte[dataSize];
				int readCount = dataStream.read(data);
				if(dataSize != readCount) Minepacks.getInstance().getLogger().warning("Problem reading file, read " + readCount + " of " + (file.length() - 1) + " bytes.");
				itemStacks[page] = inventorySerializer.deserialize(data, version);
			}

			return new BackpackFile(owner, icon, itemStacks);
		}
	}

	private static BackpackFile loadLegacy(final @NotNull FileInputStream fis, final @NotNull File file, final int version) throws IOException
	{
		byte[] out = new byte[(int) (file.length() - 1)];
		int readCount = fis.read(out);
		if(file.length() - 1 != readCount) Minepacks.getInstance().getLogger().warning("Problem reading file, read " + readCount + " of " + (file.length() - 1) + " bytes.");
		ItemStack[] itemStacks = inventorySerializer.deserialize(out, version);
		return new BackpackFile("Unknown", MagicValues.BACKPACK_STYLE_NAME_DEFAULT, new ItemStack[][] { itemStacks});
	}
	//endregion

	public void writeFile(final @NotNull File file)
	{
		try(FileOutputStream fos = new FileOutputStream(file))
		{
			fos.write(inventorySerializer.getUsedSerializer() | FILE_FORMAT_VERSION);
			try(DataOutputStream dataStream = new DataOutputStream(fos))
			{
				dataStream.writeUTF(ownerName);
				dataStream.writeUTF(backpackIcon);
				dataStream.writeInt(itemStacks.length);
				for(ItemStack[] items : itemStacks)
				{
					byte[] data = inventorySerializer.serialize(items);
					dataStream.writeInt(data.length);
					dataStream.write(data);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}