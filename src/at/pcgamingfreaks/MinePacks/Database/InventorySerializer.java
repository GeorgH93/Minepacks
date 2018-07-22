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
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.ConsoleColor;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

public class InventorySerializer
{
	private ItemStackSerializer serializer, baseItemStackSerializer = new BukkitItemStackSerializer();
	private int usedSerializer = 1;
	private Logger logger;
	
	public InventorySerializer(Logger logger)
	{
		this.logger = logger;
		try
		{
			if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_7) && MCVersion.isOlderOrEqualThan(MCVersion.MC_1_7_10) &&
					(Bukkit.getServer().getName().toLowerCase().contains("cauldron") || Bukkit.getServer().getName().toLowerCase().contains("uranium")))
			{
				serializer = new CauldronNBTItemStackSerializer();
			}
			else if(NBTItemStackSerializer.isMCVersionCompatible())
			{
				serializer = new NBTItemStackSerializer();
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

	public ItemStack[] deserialize(byte[] data, int usedSerializer)
	{
		switch(usedSerializer)
		{
			case 0: return baseItemStackSerializer.deserialize(data);
			case 1:
				if(usedSerializer != this.usedSerializer)
				{
					logger.warning(ConsoleColor.RED + "No compatible serializer for item format available!" + ConsoleColor.RESET);
					return null;
				}
			default: return serializer.deserialize(data);
		}
	}
	
	public int getUsedSerializer()
	{
		return usedSerializer;
	}
}