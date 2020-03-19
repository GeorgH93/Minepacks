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

package at.pcgamingfreaks.Minepacks.Bukkit.Item;

import at.pcgamingfreaks.Bukkit.HeadUtils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class ItemConfigHead extends ItemConfig
{
	private static final UUID MINEPACKS_UUID = UUID.nameUUIDFromBytes("Minepacks".getBytes());
	@Getter protected final String value;

	public ItemConfigHead(final @NotNull String displayName, final @NotNull String value, final @Nullable List<String> lore)
	{
		super(displayName, lore);
		this.value = value;
	}

	@Override
	public ItemStack make(final int amount)
	{
		ItemStack stack = HeadUtils.fromBase64(value, displayName, MINEPACKS_UUID);
		if(lore != null)
		{
			ItemMeta meta = stack.getItemMeta();
			meta.setLore(lore);
			stack.setItemMeta(meta);
		}
		return stack;
	}

	@Override
	public Material getMaterial()
	{
		return HeadUtils.HEAD_MATERIAL;
	}
}