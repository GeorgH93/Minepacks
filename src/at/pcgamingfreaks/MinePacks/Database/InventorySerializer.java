/*
 *   Copyright (C) 2014-2016 GeorgH93
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

import at.pcgamingfreaks.Bukkit.ItemStackSerializer.BukkitItemStackSerializer;
import at.pcgamingfreaks.Bukkit.ItemStackSerializer.ItemStackSerializer;
import at.pcgamingfreaks.Bukkit.ItemStackSerializer.NBTItemStackSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventorySerializer
{
	ItemStackSerializer serializer, baseItemStackSerializer = new BukkitItemStackSerializer();
	int usedSerializer = 1;
	
	public InventorySerializer()
	{
		String name = Bukkit.getServer().getClass().getPackage().getName();
		String[] version = name.substring(name.lastIndexOf('.') + 2).split("_");
		try
		{
			if(version[0].equals("1"))
			{
				if(version[1].equals("8") || version[1].equals("9"))
				{
					serializer = new NBTItemStackSerializer();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(serializer == null)
		{
			usedSerializer = 0;
			serializer = baseItemStackSerializer;
		}
	}
	
	public byte[] serialize(Inventory inv)
	{
		return serializer.serialize(inv.getContents());
	}

	@SuppressWarnings("unused")
	public ItemStack[] deserialize(byte[] data)
	{
		return deserialize(data, usedSerializer);
	}
	
	public ItemStack[] deserialize(byte[] data, int usedSerializer)
	{
		switch(usedSerializer)
		{
			case 0: return baseItemStackSerializer.deserialize(data);
			default: return serializer.deserialize(data);
		}
	}
	
	public int getUsedSerializer()
	{
		return usedSerializer;
	}
}