/*
 *   Copyright (C) 2016-2018 GeorgH93
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

import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.OldFileUpdater;
import at.pcgamingfreaks.YamlFileManager;

import org.bukkit.plugin.java.JavaPlugin;

public class Language extends at.pcgamingfreaks.Bukkit.Language
{
	private static final int LANG_VERSION = 11, UPGRADE_THRESHOLD = 11;

	public Language(JavaPlugin plugin)
	{
		super(plugin, LANG_VERSION, UPGRADE_THRESHOLD);
	}

	@Override
	protected void doUpdate() {}

	@Override
	protected void doUpgrade(YamlFileManager oldLang)
	{
		if(oldLang.getVersion() < 10) // Pre v2.0 versions
		{
			OldFileUpdater.updateLanguage(oldLang.getYaml(), getYaml(), plugin.getLogger());
		}
		else
		{
			super.doUpgrade(oldLang);
		}
	}
}