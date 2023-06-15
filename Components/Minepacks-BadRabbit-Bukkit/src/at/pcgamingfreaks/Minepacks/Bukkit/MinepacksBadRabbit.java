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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit;

import at.pcgamingfreaks.BadRabbit.Bukkit.BadRabbit;
import at.pcgamingfreaks.Minepacks.MagicValues;
import at.pcgamingfreaks.Version;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Uses BadRabbit to initiate the plugin in normal or standalone mode depending on the users' environment.
 */
@SuppressWarnings("unused")
public class MinepacksBadRabbit extends BadRabbit
{
	@Override
	protected @NotNull JavaPlugin createInstance() throws Exception
	{
		Plugin pcgfPluginLib = Bukkit.getPluginManager().getPlugin("PCGF_PluginLib");
		boolean standalone = true;
		if(pcgfPluginLib != null)
		{
			if(new Version(pcgfPluginLib.getDescription().getVersion()).olderThan(new Version(MagicValues.MIN_PCGF_PLUGIN_LIB_VERSION)))
			{
				getLogger().info("PCGF-PluginLib to old! Switching to standalone mode!");
			}
			else
			{
				getLogger().info("PCGF-PluginLib installed. Switching to normal mode!");
				standalone = false;
			}
		}
		else
		{
			getLogger().info("PCGF-PluginLib not installed. Switching to standalone mode!");
		}
		if(standalone)
		{
			Class<?> standaloneClass = Class.forName("at.pcgamingfreaks.MinepacksStandalone.Bukkit.Minepacks");
			return (JavaPlugin) standaloneClass.newInstance();
		}
		else
		{
			Class<?> normalClass = Class.forName("at.pcgamingfreaks.Minepacks.Bukkit.Minepacks");
			return (JavaPlugin) normalClass.newInstance();
		}
	}
}