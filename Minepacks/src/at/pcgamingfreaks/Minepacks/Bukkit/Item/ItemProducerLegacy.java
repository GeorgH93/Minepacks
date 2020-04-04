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

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public final class ItemProducerLegacy implements IItemProducer
{
	public static final ItemProducerLegacy INSTANCE = new ItemProducerLegacy();

	private ItemProducerLegacy()
	{}

	@Override
	public @NotNull ItemStack make(@NotNull ItemConfig config, int amount)
	{
		ItemStack stack = new ItemStack(config.getMaterial(), amount, (short) config.getModel());
		ItemMeta meta = stack.getItemMeta();
		assert meta != null;
		meta.setDisplayName(config.getDisplayName());
		meta.setUnbreakable(true);
		if(config.getLore() != null) meta.setLore(config.getLore());
		stack.setItemMeta(meta);
		return stack;
	}
}