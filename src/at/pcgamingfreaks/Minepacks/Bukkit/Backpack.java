/*
 *   Copyright (C) 2016, 2017 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit;

import at.pcgamingfreaks.Bukkit.NMSReflection;
import at.pcgamingfreaks.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Backpack implements at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack
{
	private final static Method METHOD_GET_INVENTORY = NMSReflection.getOBCMethod("inventory.CraftInventory", "getInventory");
	private final static Field FIELD_TITLE = NMSReflection.getOBCField("inventory.CraftInventoryCustom$MinecraftInventory", "title");

	private final OfflinePlayer owner;
	private final String titleOther;
	private final HashMap<Player, Boolean> opened = new HashMap<>();
	private Inventory bp;
	private int size, ownerID;
	private boolean hasChanged;
	
	public Backpack(OfflinePlayer owner)
	{
		this(owner, 9);
	}
	
	public Backpack(OfflinePlayer owner, int size)
	{
		this(owner, size, -1);
	}

	public Backpack(OfflinePlayer owner, int Size, int ID)
	{
		this.owner = owner;
		titleOther = StringUtils.limitLength(String.format(Minepacks.getInstance().backpackTitleOther, owner.getName()), 32);
		bp = Bukkit.createInventory(this, Size, titleOther);
		size = Size;
		ownerID = ID;
	}
	
	public Backpack(OfflinePlayer owner, ItemStack[] backpack, int ID)
	{
		this(owner, backpack.length, ID);
		bp.setContents(backpack);
	}
	
	public int getOwnerID()
	{
		return ownerID;
	}
	
	public void setOwnerID(int id)
	{
		ownerID = id;
	}

	@Override
	public @NotNull OfflinePlayer getOwner()
	{
		return owner;
	}

	@Override
	public void open(@NotNull Player player, boolean editable)
	{
		if(owner.isOnline())
		{
			Player owner = this.owner.getPlayer();
			if(owner != null)
			{
				int size = Minepacks.getInstance().getBackpackPermSize(owner);
				if(size != bp.getSize())
				{
					List<ItemStack> items = setSize(size);
					for(ItemStack i : items)
					{
						if (i != null)
						{
							owner.getWorld().dropItemNaturally(owner.getLocation(), i);
						}
					}
				}
			}
		}
		opened.put(player, editable);

		// It's not perfect, but it is the only way of doing this.
		// This sets the title of the inventory based on the person who is opening it.
		// The owner will see an other title then everyone else.
		// This way we can add owner name to the tile for everyone else.
		try
		{
			FIELD_TITLE.setAccessible(true);
			FIELD_TITLE.set(METHOD_GET_INVENTORY.invoke(bp), player.equals(owner) ? Minepacks.getInstance().backpackTitle : titleOther);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		player.openInventory(bp);
	}
	
	public void close(Player p)
	{
		opened.remove(p);
	}

	@Override
	public boolean isOpen()
	{
		return !opened.isEmpty();
	}

	@Override
	public boolean canEdit(@NotNull Player player)
	{
		return opened.containsKey(player) && opened.get(player);
	}

	@Override
	public int getSize()
	{
		return size;
	}
	
	public List<ItemStack> setSize(int newSize)
	{
		for(Entry<Player, Boolean> e : opened.entrySet())
		{
			e.getKey().closeInventory();
		}
		List<ItemStack> removedItems = new ArrayList<>();
		ItemStack[] itemStackArray;
		if(bp.getSize() > newSize)
		{
			int count = 0;
			itemStackArray = new ItemStack[newSize];
			for(ItemStack i : bp.getContents())
			{
				if(i != null)
				{
					if(count < newSize)
					{
						itemStackArray[count] = i;
						count++;
					}
					else
					{
						removedItems.add(i);
					}
				}
			}
		}
		else
		{
			itemStackArray = bp.getContents();
		}
		bp = Bukkit.createInventory(bp.getHolder(), newSize, titleOther);
		for(int i = 0; i < itemStackArray.length; i++)
		{
			bp.setItem(i, itemStackArray[i]);
		}
		setChanged();
		save(); // Make sure the new inventory is saved
		size = newSize;
		for(Entry<Player, Boolean> e : opened.entrySet())
		{
			e.getKey().openInventory(bp);
		}
		return removedItems;
	}

	@Override
	public Inventory getInventory()
	{
		return bp;
	}

	@Override
	public boolean hasChanged()
	{
		return hasChanged;
	}

	@Override
	public void setChanged()
	{
		hasChanged = true;
	}

	@Override
	public void save()
	{
		if(hasChanged())
		{
			Minepacks.getInstance().getDb().saveBackpack(this);
			hasChanged = false;
		}
	}

	@Override
	public void clear()
	{
		bp.clear();
		setChanged();
		save();
	}

	@Override
	public void drop(Location location)
	{
		for(ItemStack i : bp.getContents())
		{
			if(i != null)
			{
				location.getWorld().dropItemNaturally(location, i);
			}
		}
		clear();
	}
}