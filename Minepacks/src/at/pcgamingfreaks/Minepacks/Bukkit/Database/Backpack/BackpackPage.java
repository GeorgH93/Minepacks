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

import at.pcgamingfreaks.Minepacks.Bukkit.Database.MinepacksPlayerData;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;

public class BackpackPage extends Backpack
{
	@Getter private BackpackMultiPage multiPageOwner;

	public BackpackPage(MinepacksPlayerData owner)
	{
		super(owner);
	}

	public BackpackPage(MinepacksPlayerData owner, int size)
	{
		super(owner, size);
	}

	public BackpackPage(MinepacksPlayerData owner, ItemStack[] backpack)
	{
		super(owner, backpack);
	}

	@Override
	public boolean isBackpackPage()
	{
		return true;
	}
}