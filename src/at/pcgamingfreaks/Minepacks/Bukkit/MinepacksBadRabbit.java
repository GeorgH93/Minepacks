/*
 *   Copyright (C) 2019 GeorgH93
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

import at.pcgamingfreaks.BadRabbit.Bukkit.BadRabbit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Uses BadRabbit to initiate the plugin in normal or standalone mode depending on the users environment.
 */
@SuppressWarnings("unused")
public class MinepacksBadRabbit extends BadRabbit
{
	@Override
	protected @NotNull JavaPlugin createInstance() throws Exception
	{
		JavaPlugin newPluginInstance;
		if(Bukkit.getPluginManager().getPlugin("PCGF_PluginLib") == null)
		{
			getLogger().info("PCGF-PluginLib not installed. Switching to standalone mode!");
			Class<?> standaloneClass = Class.forName("at.pcgamingfreaks.MinepacksStandalone.Bukkit.Minepacks");
			newPluginInstance = (JavaPlugin) standaloneClass.newInstance();
		}
		else
		{
			getLogger().info("PCGF-PluginLib installed. Switching to normal mode!");
			newPluginInstance = new Minepacks();
		}
		return newPluginInstance;
	}
}