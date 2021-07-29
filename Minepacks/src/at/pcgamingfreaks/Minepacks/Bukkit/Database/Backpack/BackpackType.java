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

import at.pcgamingfreaks.Minepacks.Bukkit.Database.BackpacksConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.GUI.ButtonConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.GUI.ControlPosition;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.Utils;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;

import java.util.*;

public final class BackpackType
{
	@Getter private final String name;
	@Getter private int pageCount;
	@Getter private ItemConfig defaultShortcutItem;
	@Getter private ItemConfig blockedSlotItem;
	private PageConfiguration[] pageConfigurations;

	public BackpackType()
	{
		name = "fallback";
		pageCount = 1;
		defaultShortcutItem = null;
		blockedSlotItem = null;
		pageConfigurations = new PageConfiguration[] { new PageConfiguration(1) };
		pageConfigurations[0].fillSlots(9);
	}

	public BackpackType(final @NotNull BackpacksConfig config, final @NotNull String key)
	{
		final int maxRows = config.isAllowControlsInRow7() ? 7 : 6;
		this.name = key.substring(key.indexOf('.'));
		String control = config.getConfigE().getString(key + ".ControlLayout", config.getDefaultControlLayout());
		if(!config.getControlLayoutsMap().containsKey(control))
		{
			config.getLogger().warning("Unknown control layout '" + control + "' used in backpack type '" + name + "', falling back to default control layout.");
			control = config.getDefaultControlLayout();
		}
		ButtonConfig[] controlLayout = config.getControlLayoutsMap().get(control);
		ControlPosition controlPosition = Utils.getEnum(config.getConfigE().getString(".ControlPosition", config.getDefaultControlPosition().name()), config.getDefaultControlPosition());
		if(controlPosition.isHorizontal())
		{
			if(controlLayout.length > 9)
			{
				config.getLogger().warning("Control layout '" + control + "' has more buttons than fit into 1 row. Will limit buttons to 9.");
				controlLayout = Arrays.copyOfRange(controlLayout, 0, 9);
			}
		}
		else
		{
			if(controlLayout.length > maxRows)
			{
				config.getLogger().warning("Control layout '" + control + "' has more buttons than fit into 1 row. Will limit buttons to " + maxRows + ".");
				controlLayout = Arrays.copyOfRange(controlLayout, 0, maxRows);
			}
		}

		int slotsPerPage = config.getAutoIntValue(key + ".SlotsPerPage");
		int slots = config.getAutoIntValue(key + ".Slots");
		int rows = config.getAutoIntValue(key + ".Rows");
		int pages = config.getAutoIntValue(key + ".Pages");
		int rowsPerPage = config.getAutoIntValue(key + ".RowsPerPage");
		//region calculate size parameters
		if(rowsPerPage == -1 && slotsPerPage == -1) rowsPerPage = 6;
		else if(rowsPerPage > 6)
		{
			config.getLogger().warning("RowsPerPage value of " + rowsPerPage + " exceeds the maximal possible rows per page of 6 for backpack type '" + name + "'.");
			rowsPerPage = 6;
		}
		int maxUsableSlotsPerPage = 9 * rowsPerPage - (config.isAllowControlsInRow7() ? controlLayout.length > 9 ? controlLayout.length - 9 : 0 : controlLayout.length);

		if(pages == -1 && slots == -1 && rows == -1)
		{
			config.getLogger().warning("Unable to figure out page count for backpack type '" + name + "', falling back to 1.");
			pages = 1;
		}
		if(slotsPerPage > maxUsableSlotsPerPage)
		{
			config.getLogger().warning("SlotsPerPage value of " + slotsPerPage + " exceeds the maximal possible slots per page of " + maxUsableSlotsPerPage + " for backpack type '" + name + "'." +
					                           (maxUsableSlotsPerPage != 6*9 ? " Please keep in mind that the controls eat into your slots per page contingent." : ""));
			slotsPerPage = maxUsableSlotsPerPage;
		}
		else if (slotsPerPage == -1)
		{
			slotsPerPage = maxUsableSlotsPerPage;
		}
		else if (slotsPerPage > 0 && rowsPerPage > 0)
		{
			config.getLogger().warning("Please do not define rows per page and slots per page at the same time. Slots per page override for backpack type '" + name + "'.");
		}
		// find how many slots should be used
		if(slots == -1 && rows > 0)
		{
			slots = rows * 9;
		}
		else if(slots > 0 && rows > 0 && slots != rows * 9)
		{
			config.getLogger().warning("Please do not define rows and slots at the same time. Slots override rows for backpack type '" + name + "'.");
		}
		else if(slots == -1 && rows == -1)
		{ // We have neither an amount of rows nor slots
			slots = pages * slotsPerPage;
		}
		if(pages == -1)
		{
			pages = slots / slotsPerPage;
		}
		//endregion

		assert(pages > 0);
		assert(slots > 0);
		assert(slotsPerPage > 0);

		int minRowsCauseOfUI = controlPosition.isHorizontal() ? 1 : controlLayout.length;

		// Setup
		pageConfigurations = new PageConfiguration[pages];
		PageConfiguration pageConf = new PageConfiguration(Math.max((int)Math.ceil((slotsPerPage + controlLayout.length) / 9.0), minRowsCauseOfUI));
		pageConf.fillMenuSlots(controlPosition, controlLayout);
		pageConf.fillSlots(slotsPerPage);
		for(int i = 0; i < Math.max(pages - 1, 1); i++)
		{
			pageConfigurations[i] = pageConf;
		}
		if(pages > 1)
		{ // Setup last page
			int slotsLeft = slots - (pages - 1) * slotsPerPage;
			if (slotsLeft == slotsPerPage)
			{
				pageConfigurations[pages - 1] = pageConf;
			}
			else
			{
				pageConfigurations[pages - 1] = new PageConfiguration(pageConf.getRows());
				pageConfigurations[pages - 1].fillMenuSlots(controlPosition, controlLayout);
				pageConfigurations[pages - 1].fillSlots(slotsLeft);
			}
		}
	}

	public Set<Integer> getUsableSlots()
	{
		return getUsableSlots(0);
	}

	public Set<Integer> getUsableSlots(int pageIndex)
	{
		return pageConfigurations[pageIndex].getUsableSlots();
	}

	public Set<Integer> getBlockedSlots(int pageIndex)
	{
		return pageConfigurations[pageIndex].getBlockedSlots();
	}

	public Map<Integer, ButtonConfig> getMenuSlots(int pageIndex)
	{
		return pageConfigurations[pageIndex].getMenuSlots();
	}

	public int getRows(int pageIndex)
	{
		return pageConfigurations[pageIndex].getRows();
	}

	public String getPermission()
	{
		return "backpack.size." + name;
	}

	@Getter
	private static class PageConfiguration
	{
		private final Set<Integer> usableSlots;
		private final Set<Integer> blockedSlots;
		private final Map<Integer, ButtonConfig> menuSlots;
		private final int rows;

		PageConfiguration(int rows)
		{
			usableSlots = new HashSet<>();
			blockedSlots = new HashSet<>();
			menuSlots = new HashMap<>();
			this.rows = rows;
		}

		private void fillMenuSlots(ControlPosition position, ButtonConfig[] controls)
		{
			int[] slotIds = position.getControlIds(controls.length, rows);
			for(int i = 0; i < slotIds.length; i++)
			{
				menuSlots.put(slotIds[i], controls[i]);
			}
		}

		private void fillSlots(int slots)
		{
			int filledSlots = 0;
			for(int i = 0; i < rows * 9; i++)
			{
				if (menuSlots.get(i) != null)
				{
					if (filledSlots < slots)
					{
						usableSlots.add(i);
						filledSlots++;
					}
					else
					{
						blockedSlots.add(i);
					}
				}
			}
		}
	}
}