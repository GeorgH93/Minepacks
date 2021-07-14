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

package at.pcgamingfreaks.Minepacks.Bukkit.Item;

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.Util.HeadUtils;
import at.pcgamingfreaks.Config.IConfig;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Getter
public final class ItemConfig
{
	private final @NotNull String name;
	private final @NotNull Material material;
	private final @NotNull String displayName;
	private final @Nullable List<String> lore;
	private final int amount, model;
	private final @Nullable String value;
	private final @NotNull IItemProducer producer;
	@Getter @Setter private @Nullable Object databaseKey;

	public static ItemConfig fromConfig(final @NotNull IConfig config, final @NotNull String key, final @Nullable Function<String, String> translatePlaceholdersFunction)
	{
		try
		{
			int nameStartAt = key.lastIndexOf('.');
			final String name = key.substring(Math.max(0, nameStartAt));
			final List<String> lore = config.getConfigE().getStringList(key + ".Lore", new ArrayList<>(0));
			final List<String> loreFinal;
			if(lore.size() == 0) loreFinal = null;
			else
			{
				loreFinal = new ArrayList<>(lore.size());
				lore.forEach(loreEntry -> loreFinal.add(translatePlaceholdersFunction != null ? translatePlaceholdersFunction.apply(loreEntry) : loreEntry));
			}
			String displayName = config.getConfigE().getString(key + ".DisplayName", "&kBackpack");
			if(translatePlaceholdersFunction != null)
			{
				displayName = translatePlaceholdersFunction.apply(displayName);
			}
			final String material = config.getConfigE().getString(key + ".Material");
			final int model = config.getConfigE().getInt(key + ".Model", 0);
			final int amount = config.getConfigE().getInt(key + ".Amount", 1);
			return new ItemConfig(name, material, amount, displayName, loreFinal, model, config.getConfigE().getString(key + ".HeadValue", null));
		}
		catch(Exception e)
		{
			config.getLogger().warning("Failed to load item definition for '" + key + "'! Error: " + e.getMessage());
		}
		return null;
	}

	public ItemConfig(final @NotNull String name, final @NotNull String material, final int amount, final @NotNull String displayName, final @Nullable List<String> lore, int model, final @Nullable String value) throws IllegalArgumentException
	{
		assert model < 0;
		this.name = name;
		this.amount = amount;
		if(material.equalsIgnoreCase("player_head"))
		{
			this.material = HeadUtils.HEAD_MATERIAL;
			model = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_14) ? model : -1;
			producer = (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_8) && value != null) ?  ItemProducerHead.INSTANCE : ItemProducerLegacy.INSTANCE;
		}
		else
		{
			this.material = getMaterialFromString(material);
			producer = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_14) ?  ItemProducerModelId.INSTANCE : ItemProducerLegacy.INSTANCE;
		}
		this.displayName = displayName;
		this.lore = lore;
		this.model = model;
		this.value = value;
	}

	public ItemConfig(final @NotNull String name, final @NotNull ItemConfig item)
	{
		this.name = name;
		this.material = item.material;
		this.displayName = item.displayName;
		this.lore = item.lore;
		this.amount = item.amount;
		this.model = item.model;
		this.value = item.value;
		this.producer = item.producer;
	}

	public @NotNull ItemStack make() { return producer.make(this); }

	public @NotNull ItemStack make(final int amount)
	{
		return producer.make(this, amount);
	}

	private static @NotNull Material getMaterialFromString(String name) throws IllegalArgumentException
	{
		name = name.toUpperCase(Locale.ENGLISH);
		Material mat = Material.getMaterial(name);
		if(mat == null && MCVersion.isNewerOrEqualThan(MCVersion.MC_1_13)) mat = Material.getMaterial(name, true);
		//TODO from id
		if(mat == null) throw new IllegalArgumentException("Unable to find material: " + name);
		return mat;
	}
}