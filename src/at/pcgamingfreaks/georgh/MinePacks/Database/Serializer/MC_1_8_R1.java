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

package at.pcgamingfreaks.georgh.MinePacks.Database.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

import net.minecraft.server.v1_8_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R1.NBTTagCompound;

import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MC_1_8_R1 extends Base
{
	public byte[] toByteArray(Inventory inv)
	{
		byte[] ba = null;
		NBTTagCompound localNBTTagCompound = new NBTTagCompound();
	    localNBTTagCompound.setInt("size", inv.getSize());
	    for (int i = 0; i < inv.getSize(); i++)
	    {
	    	if (inv.getItem(i) != null)
	    	{
	    		localNBTTagCompound.set(String.valueOf(i), CraftItemStack.asNMSCopy(inv.getItem(i)).save(new NBTTagCompound()));
	    	}
	    }
	    try
	    {
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	DataOutputStream w = new DataOutputStream(baos);
	    	NBTCompressedStreamTools.a(localNBTTagCompound, (OutputStream)w);
	    	w.flush();
	    	ba = baos.toByteArray();
	    	w.close();
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
		return ba;
	}
	
	public ItemStack[] toItemStack(byte[] data)
	{
		try
		{
			if (data != null)
		    {
				NBTTagCompound localNBTTagCompound = NBTCompressedStreamTools.a(new ByteArrayInputStream(data));
		        int i = localNBTTagCompound.getInt("size");
			    ItemStack[] its = new ItemStack[i];
		        for (int k = 0; k < i; k++)
		        {
		            if (localNBTTagCompound.hasKeyOfType(String.valueOf(k), 10))
		            {
		            	its[k] = CraftItemStack.asBukkitCopy(net.minecraft.server.v1_8_R1.ItemStack.createStack(localNBTTagCompound.getCompound(String.valueOf(k))));
		            }
		        }
		        return its;
		    }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}