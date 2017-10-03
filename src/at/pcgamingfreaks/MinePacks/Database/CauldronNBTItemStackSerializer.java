/*
 *   Copyright (C) 2014-2017 GeorgH93
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

import at.pcgamingfreaks.Bukkit.ItemStackSerializer.ItemStackSerializer;
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.NMSReflection;
import at.pcgamingfreaks.MinePacks.CauldronReflection;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Method;

public class CauldronNBTItemStackSerializer implements ItemStackSerializer
{
	//region Reflection Variables
	private static final Class<?> CLASS_CRAFT_ITEM_STACK            = NMSReflection.getOBCClass("inventory.CraftItemStack");
	private static final Class<?> CLASS_NBT_TAG_COMPOUND            = CauldronReflection.getNMSClass("NBTTagCompound");
	private static final Class<?> CLASS_NBT_COMPRESSED_STREAM_TOOLS = CauldronReflection.getNMSClass("NBTCompressedStreamTools");
	private static final Class<?> CLASS_NMS_ITEM_STACK              = CauldronReflection.getNMSClass("ItemStack");
	private static final Method METHOD_NBT_TAG_C_SET_INT  = CauldronReflection.getNMSMethod(CLASS_NBT_TAG_COMPOUND, "setInt", String.class, int.class);
	private static final Method METHOD_NBT_COMP_STEAM_A   = CauldronReflection.getNMSMethod(CLASS_NBT_COMPRESSED_STREAM_TOOLS, "a", CLASS_NBT_TAG_COMPOUND, OutputStream.class);
	private static final Method METHOD_NBT_TAG_C_SET2     = CauldronReflection.getNMSMethod(CLASS_NBT_TAG_COMPOUND, "set", String.class, CauldronReflection.getNMSClass("NBTBase"));
	private static final Method METHOD_SAVE               = CauldronReflection.getNMSMethod(CLASS_NMS_ITEM_STACK, "save", CLASS_NBT_TAG_COMPOUND);
	private static final Method METHOD_AS_NMS_COPY        = CauldronReflection.getNMSMethod(CLASS_CRAFT_ITEM_STACK, "asNMSCopy", ItemStack.class);
	private static final Method METHOD_GET_INT            = CauldronReflection.getNMSMethod(CLASS_NBT_TAG_COMPOUND, "getInt", String.class);
	private static final Method METHOD_HAS_KEY_OF_TYPE    = CauldronReflection.getNMSMethod(CLASS_NBT_TAG_COMPOUND, "hasKeyOfType", String.class, int.class);
	private static final Method METHOD_GET_COMPOUND       = CauldronReflection.getNMSMethod(CLASS_NBT_TAG_COMPOUND, "getCompound", String.class);
	private static final Method METHOD_CREATE_STACK       = CauldronReflection.getNMSMethod(CLASS_NMS_ITEM_STACK, "createStack", CLASS_NBT_TAG_COMPOUND);
	private static final Method METHOD_AS_BUKKIT_COPY     = CauldronReflection.getNMSMethod(CLASS_CRAFT_ITEM_STACK, "asBukkitCopy", CLASS_NMS_ITEM_STACK);
	private static final Method METHOD_NBT_COMP_STREAM_A2 = CauldronReflection.getNMSMethod(CLASS_NBT_COMPRESSED_STREAM_TOOLS, "a", InputStream.class);
	//endregion

	/**
	 * Deserialize a serialized byte array to an ItemStack array.
	 *
	 * @param data The data that should get deserialized.
	 * @return The deserialized ItemStack array.
	 */
	@Override
	public ItemStack[] deserialize(byte[] data)
	{
		if(METHOD_NBT_COMP_STREAM_A2 == null || METHOD_GET_INT == null || METHOD_HAS_KEY_OF_TYPE == null || METHOD_AS_BUKKIT_COPY == null || METHOD_GET_COMPOUND == null || METHOD_CREATE_STACK == null)
		{
			System.out.println("It seems like the system wasn't able to find the some of the Bukkit/Minecraft classes and functions.\n" +
					                   "Is the plugin up-to-date and compatible with the used server version?\nBukkit Version: " + Bukkit.getVersion() + " (" + Bukkit.getServer().getName() + ")");
		}
		else if(data != null)
		{
			try
			{
				Object localNBTTagCompound = METHOD_NBT_COMP_STREAM_A2.invoke(null, new ByteArrayInputStream(data));
				int size = (int) METHOD_GET_INT.invoke(localNBTTagCompound, "size");
				ItemStack[] its = new ItemStack[size];
				for(int i = 0; i < size; i++)
				{
					if((boolean) METHOD_HAS_KEY_OF_TYPE.invoke(localNBTTagCompound, String.valueOf(i), 10))
					{
						Object compound = METHOD_GET_COMPOUND.invoke(localNBTTagCompound, String.valueOf(i));
						Object nbtStack = METHOD_CREATE_STACK.invoke(null, compound);
						its[i] = (nbtStack != null) ? (ItemStack) METHOD_AS_BUKKIT_COPY.invoke(null, nbtStack) : null;
					}
				}
				return its;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Serializes a ItemStack array to a byte array.
	 *
	 * @param itemStacks The ItemStacks that should be serialized.
	 * @return Serialized ItemsStacks as byte array. Null if serialization failed.
	 */
	@Override
	public byte[] serialize(ItemStack[] itemStacks)
	{
		byte[] ba = null;
		if(CLASS_NBT_TAG_COMPOUND == null || METHOD_NBT_TAG_C_SET2 == null || METHOD_NBT_TAG_C_SET_INT == null || METHOD_SAVE == null || METHOD_AS_NMS_COPY == null || METHOD_NBT_COMP_STEAM_A == null)
		{
			System.out.println("It seems like the system wasn't able to find the some of the Bukkit/Minecraft classes and functions.\n" +
					                   "Is the plugin up-to-date and compatible with the used server version?\nBukkit Version: " + Bukkit.getVersion() + " (" + Bukkit.getServer().getName() + ")");
		}
		else if(itemStacks != null)
		{
			try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream))
			{
				Object localNBTTagCompound = CLASS_NBT_TAG_COMPOUND.newInstance();
				METHOD_NBT_TAG_C_SET_INT.invoke(localNBTTagCompound, "size", itemStacks.length);
				for(int i = 0; i < itemStacks.length; i++)
				{
					if(itemStacks[i] != null)
					{
						METHOD_NBT_TAG_C_SET2.invoke(localNBTTagCompound, String.valueOf(i), METHOD_SAVE.invoke(METHOD_AS_NMS_COPY.invoke(null, itemStacks[i]), CLASS_NBT_TAG_COMPOUND.newInstance()));
					}
				}
				METHOD_NBT_COMP_STEAM_A.invoke(null, localNBTTagCompound, dataOutputStream);
				dataOutputStream.flush();
				ba = byteArrayOutputStream.toByteArray();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return ba;
	}

	@Override
	public boolean checkIsMCVersionCompatible()
	{
		return isMCVersionCompatible();
	}

	public static boolean isMCVersionCompatible()
	{
		return MCVersion.isNewerOrEqualThan(MCVersion.MC_1_7) && MCVersion.isOlderOrEqualThan(MCVersion.MC_1_7_10) && (Bukkit.getServer().getName().toLowerCase().contains("cauldron") || Bukkit.getServer().getName().toLowerCase().contains("uranium"));
	}
}