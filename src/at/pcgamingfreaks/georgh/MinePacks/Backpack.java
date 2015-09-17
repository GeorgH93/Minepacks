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

package at.pcgamingfreaks.georgh.MinePacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Backpack
{
	private OfflinePlayer owner;
	private HashMap<Player, Boolean> opened = new HashMap<>();
	private Inventory bp;
	private int size, id;
	private String title;
	private boolean inWork;
	
	public Backpack(OfflinePlayer owner)
	{
		this(owner, 9);
	}
	
	public Backpack(OfflinePlayer owner, int size)
	{
		this(owner, size, -1);
	}

	public Backpack(OfflinePlayer Owner, int Size, int ID)
	{
		owner = Owner;
		title = String.format(MinePacks.BackpackTitle, Owner.getName());
		bp = Bukkit.createInventory(null, Size, title);
		size = Size;
		id = ID;
		inWork = false;
	}
	
	public Backpack(OfflinePlayer owner, ItemStack[] backpack, int ID)
	{
		this(owner, backpack.length, ID);
		bp.setContents(backpack);
	}
	
	public int getID()
	{
		return id;
	}
	
	public void setID(int ID)
	{
		id = ID;
	}
	
	public OfflinePlayer getOwner()
	{
		return owner;
	}
	
	public void Open(Player p, boolean editable)
	{
		opened.put(p, editable);
		p.openInventory(bp);
	}
	
	public void Close(Player p)
	{
		opened.remove(p);
	}
	
	public boolean isOpen()
	{
		return !opened.isEmpty();
	}
	
	public boolean canEdit(Player p)
	{
		return opened.containsKey(p) && opened.get(p);
	}
	
	public boolean inUse()
	{
		return inWork;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public List<ItemStack> setSize(int newSize)
	{
		inWork = true;
		for(Entry<Player, Boolean> e : opened.entrySet())
		{
			e.getKey().closeInventory();
		}
		List<ItemStack> RemovedItems = new ArrayList<>();
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
						RemovedItems.add(i);
					}
				}
			}
		}
		else
		{
			itemStackArray = bp.getContents();
		}
		bp = Bukkit.createInventory(null, newSize, title);
		for(int i = 0; i < itemStackArray.length; i++)
		{
			bp.setItem(i, itemStackArray[i]);
		}
		size = newSize;
		for(Entry<Player, Boolean> e : opened.entrySet())
		{
			e.getKey().openInventory(bp);
		}
		inWork = false;
		return RemovedItems;
	}
	
	public Inventory getBackpack()
	{
		return bp;
	}
	
	public String getTitle()
	{
		return title;
	}
}