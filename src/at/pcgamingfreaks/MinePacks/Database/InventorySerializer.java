/*
 *   Copyright (C) 2014-2018 GeorgH93
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
import at.pcgamingfreaks.Bukkit.ItemStackSerializer.NBTItemStackSerializerGen2;
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.ConsoleColor;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

public class InventorySerializer
{
	private ItemStackSerializer serializer, serializerGen1, serializerGen2, bukkitItemStackSerializer = new BukkitItemStackSerializer();
	private int usedSerializer = 2;
	private Logger logger;
	
	public InventorySerializer(Logger logger)
	{
		this.logger = logger;
		try
		{
			if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_7) && MCVersion.isOlderOrEqualThan(MCVersion.MC_1_7_10) &&
					(Bukkit.getServer().getName().toLowerCase().contains("cauldron") || Bukkit.getServer().getName().toLowerCase().contains("uranium")))
			{
				serializerGen1 = new CauldronNBTItemStackSerializer();
				usedSerializer = 1;
				serializer = serializerGen1;
			}
			else
			{
				if(NBTItemStackSerializer.isMCVersionCompatible())
				{
					serializerGen1 = new NBTItemStackSerializer(logger);
					usedSerializer = 1;
					serializer = serializerGen1;
				}
				if(NBTItemStackSerializerGen2.isMCVersionCompatible())
				{
					serializerGen2 = new NBTItemStackSerializerGen2(logger);
					usedSerializer = 2;
					serializer = serializerGen2;
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
			serializer = bukkitItemStackSerializer;
		}
	}
	
	public byte[] serialize(Inventory inv)
	{
		return serializer.serialize(inv.getContents());
	}

	public byte[] serialize(ItemStack[] inv)
	{
		return serializer.serialize(inv);
	}

	public ItemStack[] deserialize(byte[] data, int usedSerializer)
	{
		switch(usedSerializer)
		{
			case 0: return bukkitItemStackSerializer.deserialize(data);
			case 1: return serializerGen1.deserialize(data);
			case 2: return serializerGen2.deserialize(data);
			default: logger.warning(ConsoleColor.RED + "No compatible serializer for item format available!" + ConsoleColor.RESET);
		}
		return null;
	}
	
	public int getUsedSerializer()
	{
		return usedSerializer;
	}
}