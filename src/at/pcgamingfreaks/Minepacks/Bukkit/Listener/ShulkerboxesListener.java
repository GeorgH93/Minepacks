/*
 *   Copyright (C) 2016-2017 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Listener;

import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

abstract class ShulkerboxesListener
{
	protected static final Set<Material> SHULKER_BOX_MATERIALS = new HashSet<>();

	protected final Minepacks plugin;

	static
	{
		SHULKER_BOX_MATERIALS.add(Material.BLACK_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.BLUE_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.SILVER_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.BROWN_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.CYAN_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.GREEN_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.GRAY_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.LIGHT_BLUE_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.LIME_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.MAGENTA_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.ORANGE_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.PINK_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.PURPLE_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.RED_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.WHITE_SHULKER_BOX);
		SHULKER_BOX_MATERIALS.add(Material.YELLOW_SHULKER_BOX);
	}

	protected ShulkerboxesListener(Minepacks plugin)
	{
		this.plugin = plugin;
	}
}