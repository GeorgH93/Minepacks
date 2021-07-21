/*
 *   Copyright (C) 2022 GeorgH93
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControlPositionTest
{
	@Test
	void testGetControlIds()
	{
		// Horizontal controls
		assertArrayEquals(new int[]{0, 1, 2, 3, 4}, ControlPosition.TOP_LEFT.getControlIds(5, 6));
		assertArrayEquals(new int[]{4, 5, 6, 7, 8}, ControlPosition.TOP_RIGHT.getControlIds(5, 6));
		assertArrayEquals(new int[]{3, 4, 5}, ControlPosition.TOP_CENTER.getControlIds(3, 6));
		assertArrayEquals(new int[]{45, 46, 47}, ControlPosition.BOTTOM_LEFT.getControlIds(3, 6));
		assertArrayEquals(new int[]{51, 52, 53}, ControlPosition.BOTTOM_RIGHT.getControlIds(3, 6));
		assertArrayEquals(new int[]{48, 49, 50}, ControlPosition.BOTTOM_CENTER.getControlIds(3, 6));

		// Vertical controls
		assertArrayEquals(new int[]{0, 9, 18}, ControlPosition.LEFT_TOP.getControlIds(3, 6));
		assertArrayEquals(new int[]{27, 36, 45}, ControlPosition.LEFT_BOTTOM.getControlIds(3, 6));
		assertArrayEquals(new int[]{9, 18, 27}, ControlPosition.LEFT_CENTER.getControlIds(3, 6));
		assertArrayEquals(new int[]{8, 17, 26}, ControlPosition.RIGHT_TOP.getControlIds(3, 6));
		assertArrayEquals(new int[]{35, 44, 53}, ControlPosition.RIGHT_BOTTOM.getControlIds(3, 6));
		assertArrayEquals(new int[]{17, 26, 35}, ControlPosition.RIGHT_CENTER.getControlIds(3, 6));
	}

	@Test
	void testIsVertical()
	{
		assertFalse(ControlPosition.TOP_LEFT.isVertical());
		assertFalse(ControlPosition.TOP_RIGHT.isVertical());
		assertFalse(ControlPosition.BOTTOM_LEFT.isVertical());
		assertFalse(ControlPosition.BOTTOM_RIGHT.isVertical());
		assertTrue(ControlPosition.LEFT_TOP.isVertical());
		assertTrue(ControlPosition.LEFT_BOTTOM.isVertical());
		assertTrue(ControlPosition.RIGHT_TOP.isVertical());
		assertTrue(ControlPosition.RIGHT_BOTTOM.isVertical());
	}

	@Test
	void testIsHorizontal()
	{
		assertTrue(ControlPosition.TOP_LEFT.isHorizontal());
		assertTrue(ControlPosition.TOP_RIGHT.isHorizontal());
		assertTrue(ControlPosition.BOTTOM_LEFT.isHorizontal());
		assertTrue(ControlPosition.BOTTOM_RIGHT.isHorizontal());
		assertFalse(ControlPosition.LEFT_TOP.isHorizontal());
		assertFalse(ControlPosition.LEFT_BOTTOM.isHorizontal());
		assertFalse(ControlPosition.RIGHT_TOP.isHorizontal());
		assertFalse(ControlPosition.RIGHT_BOTTOM.isHorizontal());
	}
}