/*
 *   Copyright (C) 2020 GeorgH93
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

import at.pcgamingfreaks.Bukkit.Configuration;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.YamlFileManager;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BackpacksConfig extends Configuration
{
	private static final int CONFIG_VERSION = 1;
	private Map<String, ItemConfig> itemConfigs = new HashMap<>();

	public BackpacksConfig(final @NotNull JavaPlugin plugin)
	{
		super(plugin, CONFIG_VERSION, CONFIG_VERSION, "backpacks.yml");
		loadItemConfigs();
	}

	@Override
	protected void doUpgrade(@NotNull YamlFileManager oldConfig)
	{
		doUpgrade(oldConfig, new HashMap<>(), getYamlE().getKeysFiltered("Items\\..*"));
	}

	private void loadItemConfigs()
	{
		getYamlE().getKeysFiltered("Items\\.[^.]*\\.Material").forEach(materialKey -> {
			final String key = materialKey.substring(0, materialKey.length() - ".Material".length());
			try{
				final List<String> lore = getConfigE().getStringList(key + ".Lore", new ArrayList<>(0));
				final List<String> loreFinal;
				if(lore.size() == 0) loreFinal = null;
				else
				{
					loreFinal = new ArrayList<>(lore.size());
					lore.forEach(loreEntry -> loreFinal.add(ChatColor.translateAlternateColorCodes('&', loreEntry)));
				}
				final String displayName = ChatColor.translateAlternateColorCodes('&', getConfigE().getString(key + ".DisplayName", "&eBackpack"));
				final String material = getYamlE().getString(key + ".Material");
				final int model = getYamlE().getInt(key + ".Model");
				itemConfigs.put(key, new ItemConfig(material, displayName, loreFinal, model, getConfigE().getString(key + ".HeadValue", null)));
			}
			catch(Exception e)
			{
				plugin.getLogger().warning("Failed to load item definition for '" + key + "'! Error: " + e.getMessage());
			}
		});
	}

	public @Nullable ItemConfig getItemConfig(final @NotNull String name)
	{
		return itemConfigs.get(name);
	}
}