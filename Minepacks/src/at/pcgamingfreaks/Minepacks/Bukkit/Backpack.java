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

package at.pcgamingfreaks.Minepacks.Bukkit;

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.NMSReflection;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.InventoryCompressor;
import at.pcgamingfreaks.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Backpack implements at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack
{
	private final static Method METHOD_GET_INVENTORY = NMSReflection.getOBCMethod("inventory.CraftInventory", "getInventory");
	private final static Method METHOD_CRAFT_CHAT_MESSAGE_FROM_STRING = MCVersion.isAny(MCVersion.MC_1_13) ? NMSReflection.getOBCMethod("util.CraftChatMessage", "wrapOrNull", String.class) : null;
	private final static Field FIELD_TITLE = NMSReflection.getOBCField("inventory.CraftInventoryCustom$MinecraftInventory", "title");
	@Setter(AccessLevel.PACKAGE) private static ShrinkApproach shrinkApproach = ShrinkApproach.COMPRESS;
	private static Object titleOwn;
	private static String titleOtherFormat, titleOther;
	private final OfflinePlayer owner;
	private final Object titleOtherOBC;
	private final Map<Player, Boolean> opened = new ConcurrentHashMap<>(); //Thanks Minecraft 1.14
	private Inventory bp;
	private int size, ownerID;
	private boolean hasChanged;

	public static void setTitle(final @NotNull String title, final @NotNull String titleOther)
	{
		titleOwn = prepareTitle(title);
		titleOtherFormat = titleOther;
	}

	private static Object prepareTitle(final @NotNull String title)
	{
		if(MCVersion.isAny(MCVersion.MC_1_13))
		{
			try
			{
				//noinspection ConstantConditions
				return METHOD_CRAFT_CHAT_MESSAGE_FROM_STRING.invoke(null, title);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			return StringUtils.limitLength(title, 32);
		}
		return null;
	}

	public Backpack(OfflinePlayer owner)
	{
		this(owner, 9);
	}
	
	public Backpack(OfflinePlayer owner, int size)
	{
		this(owner, size, -1);
	}

	public Backpack(OfflinePlayer owner, int size, int ID)
	{
		if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_14) && size > 54)
		{
			size = 54;
			Minepacks.getInstance().getLogger().warning("Backpacks with more than 6 rows are no longer supported on Minecraft 1.14 and up!");
		}
		this.owner = owner;
		titleOther = StringUtils.limitLength(String.format(titleOtherFormat, owner.getName()), 32);
		bp = Bukkit.createInventory(this, size, titleOther);
		Object titleOtherOBC = null;
		try
		{
			titleOtherOBC = FIELD_TITLE.get(METHOD_GET_INVENTORY.invoke(bp));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		this.titleOtherOBC = titleOtherOBC;
		this.size = size;
		ownerID = ID;
	}
	
	public Backpack(final OfflinePlayer owner, ItemStack[] backpack, final int ID)
	{
		this(owner, backpack.length, ID);
		if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_14) && backpack.length > 54)
		{ // Try to optimize space usage to compress items into only 6 rows
			InventoryCompressor compressor = new InventoryCompressor(backpack, 54);
			final List<ItemStack> toMuch = compressor.compress();
			backpack = compressor.getTargetStacks();
			if(!toMuch.isEmpty())
			{
				Minepacks.getInstance().getLogger().warning(owner.getName() + "'s backpack has to many items.");
				if(owner.isOnline())
				{
					Bukkit.getScheduler().runTask(Minepacks.getInstance(), () -> {
						if(owner.isOnline())
						{
							Player player = owner.getPlayer();
							assert player != null;
							Map<Integer, ItemStack> left = player.getInventory().addItem(toMuch.toArray(new ItemStack[0]));
							left.forEach((id, stack) -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
							this.setChanged();
						}
					});
				}
				else throw new RuntimeException("Backpack to big for MC 1.14 and up!");
			}
		}
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
		open(player, editable, null);
	}

	@Override
	public void open(@NotNull Player player, boolean editable, final @Nullable String title)
	{
		if(owner.isOnline())
		{
			Player owner = this.owner.getPlayer();
			if(owner != null && owner.hasPermission(Permissions.USE))
			{
				int size = Minepacks.getInstance().getBackpackPermSize(owner);
				if(size != bp.getSize())
				{
					List<ItemStack> items = setSize(size);
					for(ItemStack i : items)
					{
						if(i != null)
						{
							owner.getWorld().dropItemNaturally(owner.getLocation(), i);
						}
					}
				}
			}
		}
		opened.put(player, editable);

		//region Set backpack title
		// It's not perfect, but it is the only way of doing this.
		// This sets the title of the inventory based on the person who is opening it.
		// The owner will see an other title, then everyone else.
		// This way we can add owner name to the tile for everyone else.
		final Object usedTitle = (title == null) ? (player.equals(owner) ? titleOwn : titleOtherOBC) : prepareTitle(title);
		if(usedTitle != null && FIELD_TITLE != null && METHOD_GET_INVENTORY != null)
		{
			try
			{
				FIELD_TITLE.set(METHOD_GET_INVENTORY.invoke(bp), usedTitle);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		//endregion

		player.openInventory(bp);
	}

	public void close(Player p)
	{
		opened.remove(p);
	}

	public void closeAll()
	{
		opened.forEach((key, value) -> key.closeInventory());
		opened.clear();
		save();
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
	
	public @NotNull List<ItemStack> setSize(int newSize)
	{
		opened.forEach((key, value) -> key.closeInventory()); // Close all open views of the inventory
		List<ItemStack> removedItems;
		ItemStack[] itemStackArray;
		if(bp.getSize() > newSize)
		{
			InventoryCompressor compressor = new InventoryCompressor(bp.getContents());
			switch(shrinkApproach)
			{
				case FAST: compressor.fast(); break;
				case COMPRESS: compressor.compress(); break;
				case SORT: compressor.sort(); break;
			}
			itemStackArray = compressor.getTargetStacks();
			removedItems = compressor.getToMuch();
		}
		else
		{
			itemStackArray = bp.getContents();
			removedItems = new ArrayList<>(0);
		}
		bp = Bukkit.createInventory(bp.getHolder(), newSize, titleOther);
		for(int i = 0; i < itemStackArray.length; i++)
		{
			bp.setItem(i, itemStackArray[i]);
		}
		setChanged();
		save(); // Make sure the new inventory is saved
		size = newSize;
		opened.forEach((key, value) -> key.openInventory(bp));
		return removedItems;
	}

	@Override
	public @NotNull Inventory getInventory()
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
			Minepacks.getInstance().getDatabase().saveBackpack(this);
			hasChanged = false;
		}
	}

	public void backup()
	{
		Minepacks.getInstance().getDatabase().backup(this);
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