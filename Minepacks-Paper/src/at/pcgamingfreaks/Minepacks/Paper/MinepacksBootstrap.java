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

package at.pcgamingfreaks.Minepacks.Paper;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;

import java.lang.reflect.Field;
import java.util.logging.Level;

@SuppressWarnings({ "UnstableApiUsage", "unused" })
public class MinepacksBootstrap implements PluginBootstrap
{
	private static final String MAIN_CLASS_NORMAL = "at.pcgamingfreaks.Minepacks.Bukkit.Minepacks";
	private static final String MAIN_CLASS_STANDALONE = "at.pcgamingfreaks.MinepacksStandalone.Bukkit.Minepacks";

	@Override
	public void bootstrap(@NotNull PluginProviderContext context)
	{
	}

	@Override
	public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context)
	{
		try
		{
			if(checkPcgfPluginLib(context) && patchPluginMeta(context))
			{
				Class<?> normalClass = Class.forName(MAIN_CLASS_NORMAL);
				return (JavaPlugin) normalClass.newInstance();
			}
			else
			{
				Class<?> standaloneClass = Class.forName(MAIN_CLASS_STANDALONE);
				return (JavaPlugin) standaloneClass.newInstance();
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException("Failed to create Minepacks plugin instance!", e);
		}
	}

	private boolean patchPluginMeta(final @NotNull PluginProviderContext context)
	{
		try
		{
			Class<?> pluginMetaClass = context.getConfiguration().getClass();
			Field mainField = pluginMetaClass.getDeclaredField("main");
			mainField.setAccessible(true);
			mainField.set(context.getConfiguration(), MAIN_CLASS_NORMAL);
			return true;
		}
		catch(Exception e)
		{
			context.getLogger().log(Level.SEVERE, "Failed to patch main class in PluginMeta! Falling back to Standalone mode!", e);
		}
		return false;
	}

	private boolean checkPcgfPluginLib(final @NotNull PluginProviderContext context)
	{
		try
		{
			Class.forName("at.pcgamingfreaks.PluginLib.Bukkit.PluginLib");
			//if (new Version(pcgfPluginLib.getDescription().getVersion()).olderThan(new Version(MagicValues.MIN_PCGF_PLUGIN_LIB_VERSION)))
			if (true) // TODO check version
			{
				context.getLogger().info("PCGF-PluginLib installed. Switching to normal mode!");
			}
			else
			{
				context.getLogger().info("PCGF-PluginLib to old! Switching to standalone mode!");
			}
			return true;
		}
		catch(ClassNotFoundException ignored)
		{
			context.getLogger().info("PCGF-PluginLib not installed. Switching to standalone mode!");
		}
		return false;
	}
}