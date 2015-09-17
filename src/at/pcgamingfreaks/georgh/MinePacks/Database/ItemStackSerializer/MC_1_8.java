/*
 * Copyright (C) 2014-2015 GeorgH93
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.georgh.MinePacks.Database.ItemStackSerializer;

import at.pcgamingfreaks.Bukkit.Reflection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.Object;
import java.lang.reflect.Method;

public class MC_1_8 extends Base
{
	Class NBTTagCompound = Reflection.getNMSClass("NBTTagCompound"), NBTCompressedStreamTools = Reflection.getNMSClass("NBTCompressedStreamTools");
	Class CraftItemStack = Reflection.getOBCClass("inventory.CraftItemStack"), NMSItemStack = Reflection.getNMSClass("ItemStack");

	Method setInt = Reflection.getMethod(NBTTagCompound, "setInt", String.class, int.class), a = Reflection.getMethod(NBTCompressedStreamTools, "a", NBTTagCompound, OutputStream.class);
	Method set = Reflection.getMethod(NBTTagCompound, "set", String.class, Reflection.getNMSClass("NBTBase")), save = Reflection.getMethod(NMSItemStack, "save", NBTTagCompound);
	Method asNMSCopy = Reflection.getMethod(CraftItemStack, "asNMSCopy", ItemStack.class), getInt = Reflection.getMethod(NBTTagCompound, "getInt", String.class);
	Method hasKeyOfType = Reflection.getMethod(NBTTagCompound, "hasKeyOfType", String.class, int.class), getCompound = Reflection.getMethod(NBTTagCompound, "getCompound", String.class);
	Method createStack = Reflection.getMethod(NMSItemStack, "createStack", NBTTagCompound), asBukkitCopy = Reflection.getMethod(CraftItemStack, "asBukkitCopy", NMSItemStack);
	Method ain = Reflection.getMethod(NBTCompressedStreamTools, "a", InputStream.class);

	public byte[] toByteArray(Inventory inv)
	{
		byte[] ba = null;
		try
		{
			Object localNBTTagCompound = NBTTagCompound.newInstance();
			setInt.invoke(localNBTTagCompound, "size", inv.getSize());
		    for (int i = 0; i < inv.getSize(); i++)
		    {
		        if (inv.getItem(i) != null)
		        {
			        set.invoke(localNBTTagCompound, String.valueOf(i), save.invoke(asNMSCopy.invoke(null, inv.getItem(i)), NBTTagCompound.newInstance()));
		        }
		    }
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	DataOutputStream w = new DataOutputStream(baos);
			a.invoke(null, localNBTTagCompound, w);
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
				Object localNBTTagCompound = ain.invoke(null, new ByteArrayInputStream(data));
			    int i = (int)getInt.invoke(localNBTTagCompound, "size");
			    ItemStack[] its = new ItemStack[i];
		        for (int k = 0; k < i; k++)
		        {
		            if ((boolean)hasKeyOfType.invoke(localNBTTagCompound, String.valueOf(k), 10))
		            {
		            	its[k] = (ItemStack)asBukkitCopy.invoke(null, createStack.invoke(null, getCompound.invoke(localNBTTagCompound, String.valueOf(k))));
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