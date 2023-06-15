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

package at.pcgamingfreaks.Minepacks.Paper;

import at.pcgamingfreaks.Minepacks.MagicValues;
import at.pcgamingfreaks.PCGF_PluginLibVersionDetection;
import at.pcgamingfreaks.Version;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;

import java.lang.reflect.Field;

@SuppressWarnings({ "UnstableApiUsage", "unused" })
public class MinepacksBootstrap implements PluginBootstrap
{
	private static final String MAIN_CLASS_NORMAL = "at.pcgamingfreaks.Minepacks.Bukkit.Minepacks";
	private static final String MAIN_CLASS_STANDALONE = "at.pcgamingfreaks.MinepacksStandalone.Bukkit.Minepacks";

	public void bootstrap(@NotNull PluginProviderContext context)
	{
	}

	@Override
	public void bootstrap(@NotNull BootstrapContext bootstrapContext)
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
			try
			{
				context.getLogger().error("Failed to patch main class in PluginMeta! Falling back to Standalone mode!", e);
			}
			catch(Throwable ignored)
			{
				System.out.println("[Minepacks] Failed to patch main class in PluginMeta! Falling back to Standalone mode!");
				e.printStackTrace();
			}
		}
		return false;
	}

	private boolean checkPcgfPluginLib(final @NotNull PluginProviderContext context)
	{
		String version = PCGF_PluginLibVersionDetection.getVersionBukkit();
		if (version != null)
		{
			if (new Version(version).olderThan(new Version(MagicValues.MIN_PCGF_PLUGIN_LIB_VERSION)))
			{
				logInfo("PCGF-PluginLib to old! Switching to standalone mode!", context);
			}
			else
			{
				logInfo("PCGF-PluginLib installed. Switching to normal mode!", context);
				return true;
			}
		}
		else
		{
			logInfo("PCGF-PluginLib not installed. Switching to standalone mode!", context);
		}
		return false;
	}

	//TODO remove this stupid code once the paper API stabilizes to a point where once can expect at least the logger class ot not randomly change
	private void logInfo(final @NotNull String message, final @NotNull PluginProviderContext context)
	{
		try
		{
			context.getLogger().info(message);
		}
		catch(Throwable t)
		{
			System.out.println("[Minepacks] Failed to log message: " + message);
		}
	}
}