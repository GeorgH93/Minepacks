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
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Config.YamlFileManager;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.OldFileUpdater;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.MagicValues;
import at.pcgamingfreaks.Version;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Language extends at.pcgamingfreaks.Bukkit.Config.Language
{
	private static final Version LANG_VERSION = new Version(MagicValues.LANG_VERSION);

	public Language(Minepacks plugin)
	{
		super(plugin, LANG_VERSION);
	}

	@Override
	protected void doUpgrade(@NotNull YamlFileManager oldLang)
	{
		if(oldLang.getVersion().olderThan(new Version(10))) // Pre v2.0 versions
		{
			OldFileUpdater.updateLanguage(oldLang.getYamlE(), getYaml(), getLogger());
		}
		else
		{
			super.doUpgrade(oldLang);
		}
	}

	public String[] getCommandAliases(final String command)
	{
		return getCommandAliases(command, new String[0]);
	}

	public String[] getCommandAliases(final String command, final @NotNull String... defaults)
	{
		List<String> aliases = getLangE().getStringList("Command." + command, new ArrayList<>(0));
		return (aliases.size() > 0) ? aliases.toArray(new String[0]) : defaults;
	}
}