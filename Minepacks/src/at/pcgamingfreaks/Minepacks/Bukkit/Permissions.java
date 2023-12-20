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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Permissions
{
	public static final String BASE = "backpack.";
	public static final String USE = BASE + "use";
	public static final String SORT = BASE + "sort";
	public static final String CLEAN = BASE + "clean";
	public static final String CLEAN_OTHER = BASE + "clean.other";
	public static final String FULL_PICKUP = BASE + "fullpickup";
	public static final String PICKUP_TOGGLE = BASE + "fullpickup.toggle";
	public static final String OTHERS = BASE + "others";
	public static final String OTHERS_EDIT = BASE + "others.edit";
	public static final String KEEP_ON_DEATH = BASE + "keepOnDeath";
	public static final String NO_COOLDOWN = BASE + "noCooldown";
	public static final String IGNORE_GAME_MODE = BASE + "ignoreGameMode";
	public static final String IGNORE_WORLD_BLACKLIST = BASE + "ignoreWorldBlacklist";
	public static final String UPDATE = BASE + "update";
	public static final String RELOAD = BASE + "reload";
	public static final String MIGRATE = BASE + "migrate";
	public static final String BACKUP = BASE + "backup";
	public static final String RESTORE = BASE + "restore";
	public static final String VERSION = BASE + "version";

	public static final String INVENTORY_CLEAR = "clearInventory";
	public static final String INVENTORY_CLEAR_OTHER = "clearInventory.other";

	@SneakyThrows
	public static List<String> getPermissions()
	{
		Field[] fields = Permissions.class.getDeclaredFields();
		List<String> permissions = new ArrayList<>(fields.length);
		for(Field field : fields)
		{
			if (field.getType().equals(String.class) && Modifier.isStatic(field.getModifiers()))
			{
				String val = ((String) field.get(null));
				if (!val.endsWith("."))
				{
					permissions.add(val);
				}
			}
		}
		for (int i = 1; i < 10; i++)
		{
			permissions.add("backpack.size." + i);
		}
		return permissions;
	}

	private Permissions() {}
}