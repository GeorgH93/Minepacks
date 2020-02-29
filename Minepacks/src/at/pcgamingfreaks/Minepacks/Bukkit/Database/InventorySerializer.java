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

import at.pcgamingfreaks.Bukkit.ItemStackSerializer.BukkitItemStackSerializer;
import at.pcgamingfreaks.Bukkit.ItemStackSerializer.ItemStackSerializer;
import at.pcgamingfreaks.Bukkit.ItemStackSerializer.NBTItemStackSerializerGen2;
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.ConsoleColor;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

public class InventorySerializer
{
	private static final ItemStackSerializer BUKKIT_ITEM_STACK_SERIALIZER = new BukkitItemStackSerializer();
	private final Logger logger;
	private ItemStackSerializer serializer;
	private int usedSerializer = 2;
	
	public InventorySerializer(Logger logger)
	{
		this.logger = logger;
		try
		{
			if(NBTItemStackSerializerGen2.isMCVersionCompatible())
			{
				serializer = new NBTItemStackSerializerGen2(logger);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(serializer == null)
		{
			usedSerializer = 0;
			serializer = BUKKIT_ITEM_STACK_SERIALIZER;
		}
	}
	
	public byte[] serialize(Inventory inv)
	{
		return serializer.serialize(inv.getContents());
	}

	public ItemStack[] deserialize(byte[] data, int usedSerializer)
	{
		if(data == null) return null;
		switch(usedSerializer)
		{
			case 0: return BUKKIT_ITEM_STACK_SERIALIZER.deserialize(data);
			case 1: if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_13)) logger.warning(ConsoleColor.YELLOW + "Backpack was created with an old version of minecraft. There is the chance that some items will disappear from it.");
			case 2: return serializer.deserialize(data);
			default: logger.warning(ConsoleColor.RED + "No compatible deserializer for backpack format available!" + ConsoleColor.RESET);
		}
		return null;
	}
	
	public int getUsedSerializer()
	{
		return usedSerializer;
	}
}