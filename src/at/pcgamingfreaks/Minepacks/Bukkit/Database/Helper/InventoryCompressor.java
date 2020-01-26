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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryCompressor
{
	private ItemStack[] targetStack, inputStack;
	private int filled = 0;

	public InventoryCompressor(ItemStack[] stack, int targetSize)
	{
		inputStack = stack;
		targetStack = new ItemStack[targetSize];
	}

	public List<ItemStack> compress()
	{
		List<ItemStack> toMuch = new ArrayList<>(inputStack.length - targetStack.length);
		filled = 0;
		for(ItemStack stack : inputStack)
		{
			if(stack == null || stack.getType() == Material.AIR) continue;
			tryToStack(stack);
			if(stack.getAmount() == 0) continue;
			if(filled == targetStack.length)
			{
				toMuch.add(stack);
			}
			else
			{
				targetStack[filled++] = stack;
			}
		}
		return toMuch;
	}

	private void tryToStack(ItemStack stack)
	{
		if(stack.getAmount() >= stack.getMaxStackSize()) return;
		for(int i = 0; i < filled && stack.getAmount() > 0; i++)
		{
			if(stack.isSimilar(targetStack[i]) && targetStack[i].getAmount() < targetStack[i].getMaxStackSize())
			{ // Same material and none full stack
				int move = targetStack[i].getMaxStackSize() - targetStack[i].getAmount();
				move = Math.min(stack.getAmount(), move);
				targetStack[i].setAmount(targetStack[i].getAmount() + move);
				stack.setAmount(stack.getAmount() - move);
			}
		}
	}

	public ItemStack[] getTargetStack()
	{
		return targetStack;
	}
}