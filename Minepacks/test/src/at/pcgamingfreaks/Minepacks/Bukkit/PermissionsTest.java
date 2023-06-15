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

import at.pcgamingfreaks.yaml.YAML;
import at.pcgamingfreaks.yaml.YamlInvalidContentException;
import at.pcgamingfreaks.yaml.YamlKeyNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PermissionsTest
{
	private static Collection<String> permissions;

	@BeforeAll
	public static void setup() throws IllegalAccessException
	{ // Collect all permissions defined in the Permissions class
		permissions = new HashSet<>();
		for(Field declaredField : Permissions.class.getDeclaredFields())
		{
			if(declaredField.getName().equals("BASE") || declaredField.getName().equals("SIZE_BASE")) continue;
			permissions.add((String) declaredField.get(null));
		}
	}

	private int countKeysStartingWith(Collection<String> keys, String startsWith)
	{
		int count = 0;
		for(String key : keys)
		{
			if(key.startsWith(startsWith)) count++;
		}
		return count;
	}

	@Test
	public void testPermissionsInPluginYaml() throws IOException, YamlInvalidContentException, YamlKeyNotFoundException
	{
		YAML pluginYaml = new YAML(new File("resources/plugin.yml"));
		YAML permissionsYaml = pluginYaml.getSection("permissions");
		// Check if all permissions are defined in the plugin.yml
		for(String permission : permissions)
		{
			assertTrue(permissionsYaml.isSet(permission + ".description"), "The plugin.yml should contain the permission " + permission);
		}
		// Check if all the permissions defined in the plugin.yml are also defined in the Permissions class
		Collection<String> keys = permissionsYaml.getKeys(true);
		for(String key : keys)
		{
			if(!key.endsWith("description")) continue;
			String perm = key.substring(0, key.length() - 12);
			if(perm.contains(".size.")) continue; // Ignore size permissions
			if(countKeysStartingWith(keys, perm + ".children") > 1) continue; // Skip all the permissions that are just for permission grouping
			assertTrue(permissions.contains(perm), "The plugin.yml should not contain the permission " + perm);
		}
	}
}