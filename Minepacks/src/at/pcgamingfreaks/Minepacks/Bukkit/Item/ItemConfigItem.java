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

import at.pcgamingfreaks.Bukkit.MCVersion;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

import java.util.List;

@Getter
public class ItemConfigItem extends ItemConfig
{
	private static final Producer PRODUCER = MCVersion.isOlderThan(MCVersion.MC_1_14) ? new ProducerLegacy() : new ProducerModelId();
	protected final @NotNull Material material;

	public ItemConfigItem(final @NotNull Material material, final @NotNull String displayName, final int value, final @Nullable List<String> lore)
	{
		super(displayName, lore, value);
		this.material = material;
	}

	@Override
	public ItemStack make(int amount)
	{
		return PRODUCER.make(this, amount);
	}

	//region producers
	private interface Producer
	{
		ItemStack make(final @NotNull ItemConfigItem itemConfig, final int amount);
	}

	private static class ProducerLegacy implements Producer
	{
		@Override
		public ItemStack make(final @NotNull ItemConfigItem itemConfig, final int amount)
		{
			ItemStack stack = new ItemStack(itemConfig.material, amount, (short) itemConfig.model);
			ItemMeta meta = stack.getItemMeta();
			assert meta != null;
			meta.setDisplayName(itemConfig.displayName);
			meta.setUnbreakable(true);
			if(itemConfig.lore != null) meta.setLore(itemConfig.lore);
			stack.setItemMeta(meta);
			return stack;
		}
	}

	private static class ProducerModelId implements Producer
	{
		@Override
		public ItemStack make(@NotNull ItemConfigItem itemConfig, int amount)
		{
			ItemStack stack = new ItemStack(itemConfig.material, amount);
			ItemMeta meta = stack.getItemMeta();
			assert meta != null;
			meta.setDisplayName(itemConfig.displayName);
			meta.setCustomModelData(itemConfig.model);
			if(itemConfig.lore != null) meta.setLore(itemConfig.lore);
			stack.setItemMeta(meta);
			return stack;
		}
	}
	//endregion
}