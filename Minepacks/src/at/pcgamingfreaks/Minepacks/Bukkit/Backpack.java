/*
 *   Copyright (C) 2024 GeorgH93
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
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Util.InventoryUtils;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.InventoryCompressor;
import at.pcgamingfreaks.Util.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Backpack implements at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack
{
	@Setter(AccessLevel.PACKAGE) private static ShrinkApproach shrinkApproach = ShrinkApproach.COMPRESS;
	@Setter(AccessLevel.PACKAGE) private static Message messageBackpackShrunk = new Message("Backpack shrunk!");
	private static Object titleOwnGlobal;
	private static String titleFormat, titleOtherFormat;
	private static boolean useDynTitle;
	private final Object titleOwn;
	private final String titleOther;
	@Getter private final UUID ownerId;
	private final Map<Player, Boolean> opened = new ConcurrentHashMap<>(); //Thanks Minecraft 1.14
	private Inventory bp;
	@Getter private int size;
	@Getter @Setter private int ownerDatabaseId;
	private boolean hasChanged;

	public static void setTitle(final @NotNull String title, final @NotNull String titleOther)
	{
		titleOwnGlobal = title.contains("%s") ? null : InventoryUtils.prepareTitleForOpenInventoryWithCustomTitle(title);
		titleFormat = title;
		titleOtherFormat = titleOther;
		useDynTitle = !title.equals(titleOther);
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
		this.ownerId = owner.getUniqueId();
		titleOther = StringUtils.limitLength(String.format(titleOtherFormat, owner.getName()), 32);
		bp = Bukkit.createInventory(this, size, titleOther);
		this.size = size;
		ownerDatabaseId = ID;

		if (titleOwnGlobal != null) titleOwn = titleOwnGlobal;
		else titleOwn = InventoryUtils.prepareTitleForOpenInventoryWithCustomTitle(String.format(titleFormat, owner.getName()));
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
					Minepacks.getScheduler().runNextTick(task -> {
						if(owner.isOnline())
						{
							Player player = owner.getPlayer();
							assert player != null;
							Minepacks.getScheduler().runAtEntity(player, task1 -> {
								Map<Integer, ItemStack> left = player.getInventory().addItem(toMuch.toArray(new ItemStack[0]));
								left.forEach((id, stack) -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
								this.setChanged();
							});
						}
					});
				}
				else throw new RuntimeException("Backpack to big for MC 1.14 and up!");
			}
		}
		bp.setContents(backpack);
	}

	@Override
	@Deprecated
	public @NotNull OfflinePlayer getOwner()
	{
		return Bukkit.getServer().getOfflinePlayer(ownerId);
	}

	@Override
	public @Nullable Player getOwnerPlayer()
	{
		return Bukkit.getServer().getPlayer(ownerId);
	}

	public void checkResize()
	{
		Player owner = Bukkit.getServer().getPlayer(this.ownerId);
		if(owner != null)
		{
			if(owner.hasPermission(Permissions.USE))
			{
				int size = Minepacks.getInstance().getBackpackPermSize(owner);
				if(size != bp.getSize())
				{
					boolean dropped = false;
					List<ItemStack> items = setSize(size);
					for(ItemStack i : items)
					{
						if(i != null)
						{
							owner.getWorld().dropItemNaturally(owner.getLocation(), i);
							dropped = true;
						}
					}
					if (dropped)
					{
						messageBackpackShrunk.send(owner);
					}
				}
			}
		}
	}

	@Override
	public void open(final @NotNull Player player, final boolean editable)
	{
		checkResize();
		opened.put(player, editable);
		if(useDynTitle && ownerId.equals(player.getUniqueId())) InventoryUtils.openInventoryWithCustomTitlePrepared(player, bp, titleOwn);
		else player.openInventory(bp);
	}

	@Override
	public void open(final @NotNull Player player, final boolean editable, final @Nullable String title)
	{
		if(title == null)
		{
			open(player, editable);
			return;
		}
		checkResize();
		opened.put(player, editable);
		InventoryUtils.openInventoryWithCustomTitle(player, bp, title);
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

	public @NotNull List<ItemStack> setSize(int newSize)
	{
		opened.forEach((key, value) -> key.closeInventory()); // Close all open views of the inventory
		List<ItemStack> removedItems;
		ItemStack[] itemStackArray;
		if(bp.getSize() > newSize)
		{
			InventoryCompressor compressor = new InventoryCompressor(bp.getContents(), newSize);
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

	public void forceSave()
	{
		hasChanged = true;
		save();
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
	public void drop(final @NotNull Location location)
	{
		InventoryUtils.dropInventory(bp, location);
		setChanged();
		save();
	}
}