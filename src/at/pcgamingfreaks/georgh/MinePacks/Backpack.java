/*
 *   Copyright (C) 2014 GeorgH93
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
	private HashMap<Player, Boolean> opend = new HashMap<Player, Boolean>();
	private Inventory bp;
	private int size, id;
	private String title;
	private boolean inwork;
	
	public Backpack(OfflinePlayer Owner)
	{
		owner = Owner;
		size = 9;
		id = -1;
		title = String.format(MinePacks.BackpackTitle, Owner.getName());
		bp = Bukkit.createInventory(null, size, title);
		inwork = false;
	}
	
	public Backpack(OfflinePlayer Owner, int Size)
	{
		owner = Owner;
		title = String.format(MinePacks.BackpackTitle, Owner.getName());
		bp = Bukkit.createInventory(null, Size, title);
		size = Size;
		id = -1;
		inwork = false;
	}
	
	public Backpack(OfflinePlayer Owner, ItemStack[] backpack, int ID)
	{
		owner = Owner;
		size = backpack.length;
		title = String.format(MinePacks.BackpackTitle, Owner.getName());
		bp = Bukkit.createInventory(null, size, title);
		bp.setContents(backpack);
		id = ID;
		inwork = false;
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
		opend.put(p, editable);
		p.openInventory(bp);
	}
	
	public void Close(Player p)
	{
		opend.remove(p);
	}
	
	public boolean isOpen()
	{
		if(opend.isEmpty())
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public boolean canEdit(Player p)
	{
		if(opend.containsKey(p))
		{
			return opend.get(p);
		}
		return false;
	}
	
	public boolean inUse()
	{
		return inwork;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public List<ItemStack> setSize(int newSize)
	{
		inwork = true;
		for(Entry<Player, Boolean> e : opend.entrySet())
		{
			e.getKey().closeInventory();
		}
		List<ItemStack> RemovedItems = new ArrayList<ItemStack>();
		ItemStack[] itsa;
		if(bp.getSize() > newSize)
		{
			int count = 0;
			itsa = new ItemStack[newSize];
			for(ItemStack i : bp.getContents())
			{
				if(i != null)
				{
					if(count < newSize)
					{
						itsa[count] = i;
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
			itsa = bp.getContents();
		}
		bp = Bukkit.createInventory(null, newSize, title);
		for(int i = 0; i < itsa.length; i++)
		{
			bp.setItem(i, itsa[i]);
		}
		size = newSize;
		for(Entry<Player, Boolean> e : opend.entrySet())
		{
			e.getKey().openInventory(bp);
		}
		inwork = false;
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