/*
 *   Copyright (C) 2014-2016 GeorgH93
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

package at.pcgamingfreaks.MinePacks.Database;

import org.bukkit.plugin.java.JavaPlugin;

public class Language extends at.pcgamingfreaks.Bukkit.Language
{
	private static final int LANG_VERSION = 5;

	public Language(JavaPlugin plugin)
	{
		super(plugin, LANG_VERSION);
	}

	@SuppressWarnings("SpellCheckingInspection")
	@Override
	protected void doUpdate()
	{
		switch(getVersion())
		{
			case 1: lang.set("Language.Ingame.Cooldown", "Please wait till you reopen your backpack.");
			case 2: lang.set("Language.Ingame.InvalidBackpack", lang.getString("Language.Ingame.IvalidBackpack", "Invalid backpack."));
			case 3:
			case 4: lang.set("Language.Ingame.WrongGameMode", "You are not allowed to open your backpack in your current game-mode.");
				break;
		}
	}
}