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

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import at.pcgamingfreaks.Config.YamlFileManager;
import at.pcgamingfreaks.Config.YamlFileUpdateMethod;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.MagicValues;
import at.pcgamingfreaks.Version;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Language extends at.pcgamingfreaks.Bukkit.Config.Language
{
	private static final Version LANG_VERSION = new Version(MagicValues.LANG_VERSION);

	public Language(final @NotNull Minepacks plugin)
	{
		super(plugin, LANG_VERSION);
	}

	@Override
	public @Nullable YamlFileUpdateMethod getYamlUpdateMode()
	{
		YamlFileUpdateMethod mode = super.getYamlUpdateMode();
		if(mode == YamlFileUpdateMethod.UPDATE) mode = YamlFileUpdateMethod.UPGRADE;
		return mode;
	}

	@Override
	protected void doUpgrade(final @NotNull YamlFileManager oldLang)
	{
		if(oldLang.getVersion().olderThan(new Version(10))) // Pre v2.0 versions
		{
			logger.warning("Upgrading from v1.x language files is not supported!");
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

	public @NotNull String[] getSwitch(final @NotNull String key, final @NotNull String defaultSwitch)
	{
		List<String> switches = getLangE().getStringList("Command.Switches." + key, new ArrayList<>(1));
		if(!switches.contains(defaultSwitch)) switches.add(defaultSwitch);
		return switches.toArray(new String[0]);
	}

	public @NotNull List<String> getTranslatedList(final @NotNull String key)
	{
		return getLangE().getStringList("Language." + key, new ArrayList<>(0)).stream().map(Language::translateColorCodes).collect(Collectors.toList());
	}

	private static String translateColorCodes(final @NotNull String text)
	{
		return ChatColor.translateAlternateColorCodes('&', text);
	}
}
