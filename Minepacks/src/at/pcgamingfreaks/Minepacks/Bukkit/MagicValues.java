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

package at.pcgamingfreaks.Minepacks.Bukkit;

import java.io.InputStream;
import java.util.Properties;

public class MagicValues
{
	public static final String BACKPACK_STYLE_NAME_DEFAULT = "default";
	public static final String BACKPACK_STYLE_NAME_DISABLED = "none";
	public static final String MIN_PCGF_PLUGIN_LIB_VERSION;
	public static final String LANG_VERSION;
	public static final String CONFIG_VERSION;

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
		LANG_VERSION = langVersion;
		CONFIG_VERSION = configVersion;
	}

	private MagicValues() { /* You should not create an instance of this utility class! */ }
}
