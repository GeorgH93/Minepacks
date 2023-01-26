/*
 *   Copyright (C) 2023 GeorgH93
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

package at.pcgamingfreaks.Minepacks;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Properties;

public class MagicValues
{
	public static final int LANG_VERSION;
	public static final int CONFIG_VERSION;
	public static final String MIN_PCGF_PLUGIN_LIB_VERSION;
	public static final String MIN_MC_VERSION_FOR_UPDATES = "1.8";

	static
	{
		String pcgfPluginLibVersion = "99999", langVersion = "0", configVersion = "0";

		try(InputStream propertiesStream = MagicValues.class.getClassLoader().getResourceAsStream("Minepacks.properties"))
		{
			Properties properties = new Properties();
			properties.load(propertiesStream);

			pcgfPluginLibVersion = properties.getProperty("PCGFPluginLibVersion");
			langVersion = properties.getProperty("LanguageFileVersion");
			configVersion = properties.getProperty("ConfigFileVersion");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		MIN_PCGF_PLUGIN_LIB_VERSION = pcgfPluginLibVersion;
		// Try to parse the version strings, fall back to a known min version
		LANG_VERSION = tryParse(langVersion, 20);
		CONFIG_VERSION = tryParse(configVersion, 33);
	}

	private static int tryParse(@NotNull String string, int fallbackValue)
	{
		try
		{
			return Integer.parseInt(string);
		}
		catch (NumberFormatException ignored)
		{
			System.out.println("Failed to parse integer '" + string + "'! Falling back to: " + fallbackValue);
		}
		return fallbackValue;
	}

	private MagicValues() { /* You should not create an instance of this utility class! */ }
}
