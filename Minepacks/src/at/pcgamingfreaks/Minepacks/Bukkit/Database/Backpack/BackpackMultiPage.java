/*
 *   Copyright (C) 2021 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Backpack;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.MinepacksPlayerData;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BackpackMultiPage implements at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.BackpackMultiPage
{
	@Getter List<BackpackPage> pages = new ArrayList<>();
	@Getter	MinepacksPlayerData owner;
	@Getter int size;
	private final Map<Player, Boolean> opened = new ConcurrentHashMap<>();

	@Override
	public boolean isOpen()
	{
		return !opened.isEmpty();
	}

	@Override
	public boolean canEdit(@NotNull Player player)
	{
		return opened.getOrDefault(player, false);
	}

	@Override
	public int getPageCount()
	{
		return pages.size();
	}

	@Override
	public boolean hasChanged()
	{
		for(BackpackPage page : pages)
		{
			if(page.hasChanged()) return true;
		}
		return false;
	}

	@Override
	public void setChanged()
	{
		pages.forEach(Backpack::setChanged);
	}

	@Override
	public void save()
	{
		pages.forEach(Backpack::save);
	}

	@Override
	public void clear()
	{
		pages.forEach(Backpack::clear);
	}

	@Override
	public void drop(Location location)
	{
		pages.forEach(page -> page.drop(location));
	}

	@Override
	public @Nullable ItemStack addItem(ItemStack stack)
	{
		ItemStack left = stack;
		for(BackpackPage page : pages)
		{
			left = page.addItem(left);
			if(left == null) return null;
		}
		return left;
	}

	@Override
	public @NotNull Map<Integer, ItemStack> addItems(ItemStack... itemStacks)
	{
		Map<Integer, ItemStack> result = null;
		for(BackpackPage page : pages)
		{
			if(result == null)
			{
				result = page.addItems(itemStacks);
			}
			else if(result.isEmpty()) break;
			else
			{
				ItemStack[] items = new ItemStack[result.size()];
				Map<Integer, Integer> idMap = new HashMap<>();
				int i = 0;
				for(Map.Entry<Integer, ItemStack> entry : result.entrySet())
				{
					items[i] = entry.getValue();
					idMap.put(entry.getKey(), i);
					i++;
				}
				Map<Integer, ItemStack> tmpResult = page.addItems(items);
				Iterator<Map.Entry<Integer, ItemStack>> resultIter = result.entrySet().iterator();
				while(resultIter.hasNext())
				{
					Map.Entry<Integer, ItemStack> entry = resultIter.next();
					ItemStack left = tmpResult.get(idMap.get(entry.getKey()));
					if(left == null) resultIter.remove();
					else entry.setValue(left);
				}
			}
		}
		//noinspection ConstantConditions
		return result;
	}

	@Override
	public @Nullable ItemStack addItem(ItemStack stack, int page)
	{
		return getPage(page).addItem(stack);
	}

	@Override
	public @NotNull Map<Integer, ItemStack> addItems(int page, ItemStack... itemStacks)
	{
		return getPage(page).addItems(itemStacks);
	}

	@Override
	public boolean isBackpackPage()
	{
		return false;
	}

	@Override
	public @Nullable at.pcgamingfreaks.Minepacks.Bukkit.API.BackpackMultiPage getMultiPageOwner()
	{
		return this;
	}

	@Override
	public boolean isMultiPageBackpack()
	{
		return true;
	}

	@Override
	public @NotNull Set<? extends Integer> getSpecialSlots()
	{
		return new HashSet<>();
	}

	@Override
	public @NotNull Backpack getPage(int page)
	{
		return pages.get(page);
	}

	@Override
	public void open(final @NotNull Player player, final boolean editable)
	{
		opened.put(player, editable);
		pages.get(0).open(player, editable);
	}

	@Override
	public void open(final @NotNull Player player, final boolean editable, final @Nullable String title)
	{
		opened.put(player, editable);
		pages.get(0).open(player, editable, title);
	}

	@Override
	public void open(@NotNull Player player, boolean editable, @Nullable Message title)
	{
		opened.put(player, editable);
		pages.get(0).open(player, editable, title);
	}

	@Override
	public void open(@NotNull Player player, boolean editable, int page)
	{
		opened.put(player, editable);
		getPage(page).open(player, editable);
	}

	@Override
	public @NotNull Inventory getInventory()
	{
		return pages.get(0).getInventory();
	}
}