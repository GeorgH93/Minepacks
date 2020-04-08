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

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ItemProducerHead implements IItemProducer
{
	public static final ItemProducerHead INSTANCE = new ItemProducerHead();
	private static final UUID MINEPACKS_UUID = UUID.nameUUIDFromBytes("Minepacks".getBytes());

	private ItemProducerHead()
	{}

	@Override
	public @NotNull ItemStack make(@NotNull ItemConfig config, int amount)
	{
		//noinspection ConstantConditions
		ItemStack stack = HeadUtils.fromBase64(config.getValue(), config.getDisplayName(), MINEPACKS_UUID, amount);
		ItemMeta meta = stack.getItemMeta();
		if(meta != null)
		{
			boolean metaSet = false;
			if(config.getLore() != null && !config.getLore().isEmpty())
			{
				meta.setLore(config.getLore());
				metaSet = true;
			}
			if(config.getModel() >= 0)
			{
				meta.setCustomModelData(config.getModel());
				metaSet = true;
			}
			if(metaSet) stack.setItemMeta(meta);
		}
		return stack;
	}
}