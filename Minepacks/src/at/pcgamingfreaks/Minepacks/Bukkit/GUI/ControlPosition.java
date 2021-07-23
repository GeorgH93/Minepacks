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

package at.pcgamingfreaks.Minepacks.Bukkit.GUI;

import lombok.Getter;

public enum ControlPosition
{
	// Horizontal controls
	TOP_LEFT(true),
	TOP_RIGHT(true),
	TOP_CENTER(true),
	BOTTOM_LEFT(true),
	BOTTOM_RIGHT(true),
	BOTTOM_CENTER(true),

	// Vertical controls
	LEFT_TOP(false),
	LEFT_BOTTOM(false),
	LEFT_CENTER(false),
	RIGHT_TOP(false),
	RIGHT_BOTTOM(false),
	RIGHT_CENTER(false);

	@Getter private final boolean vertical, horizontal;

	ControlPosition(boolean horizontal)
	{
		this.horizontal = horizontal;
		this.vertical = !horizontal;
	}

	public int[] getControlIds(int controlCount, int rowCount)
	{
		assert(controlCount <= 9);
		int startId = 0, stride = isVertical() ? 9 : 1;
		switch(this)
		{
			// Horizontal controls
			case TOP_RIGHT: startId = 9 - controlCount; break;
			case BOTTOM_LEFT: startId = (rowCount - 1) * 9; break;
			case BOTTOM_RIGHT: startId = (rowCount * 9) - controlCount; break;
			case BOTTOM_CENTER: startId = ((rowCount - 1) * 9); // Fallthrough to not duplicate same calculation
			case TOP_CENTER: startId += (9 - controlCount) / 2; break;
			// Vertical controls
			case RIGHT_TOP: startId = 8; break;
			case RIGHT_BOTTOM: startId = 8;
			case LEFT_BOTTOM: startId += (rowCount - controlCount) * 9; break;
			case RIGHT_CENTER: startId = 8; // Fallthrough to not duplicate same calculation
			case LEFT_CENTER: startId += ((rowCount - controlCount) / 2) * 9; break;
		}

		int[] ids = new int[controlCount];
		for(int i = 0, id = startId; i < controlCount; i++, id += stride)
		{
			ids[i] = id;
		}
		return ids;
	}
}