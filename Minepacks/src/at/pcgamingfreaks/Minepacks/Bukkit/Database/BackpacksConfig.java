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

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.Configuration;
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.MagicValues;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Version;
import at.pcgamingfreaks.YamlFileManager;
import at.pcgamingfreaks.YamlFileUpdateMethod;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BackpacksConfig extends Configuration
{
	private static final int CONFIG_VERSION = 1;
	private static final Pattern ITEM_TEXT_PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<placeholder>[\\w_-]+)}");
	@Getter private static BackpacksConfig instance;

	private final @NotNull Minepacks plugin;
	@Getter private Set<String> validShortcutStyles;
	@Getter private final Map<String, ItemConfig> backpackStylesMap = new HashMap<>();
	@Getter private boolean allowItemShortcut = true;

	public BackpacksConfig(final @NotNull Minepacks plugin)
	{
		super(plugin, new Version(CONFIG_VERSION), "backpacks.yml");
		this.plugin = plugin;
		instance = this;
	}

	@Override
	protected @Nullable YamlFileUpdateMethod getYamlUpdateMode()
	{
		return YamlFileUpdateMethod.UPGRADE;
	}

	@Override
	protected boolean newConfigCreated()
	{
		if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_14))
		{
			getConfigE().set("Items.MinepacksLegacy.Enabled", false);
			return true;
		}
		return super.newConfigCreated();
	}

	@Override
	protected void doUpgrade(final @NotNull YamlFileManager oldConfig)
	{
		doUpgrade(oldConfig, new HashMap<>(), getYamlE().getKeysFiltered("Items\\..*"));
	}

	public void loadData()
	{
		allowItemShortcut = true;
		backpackStylesMap.clear();
		loadItemConfigs("Items", backpackStylesMap);
		if(backpackStylesMap.isEmpty())
		{
			logger.warning("There musst be at least one item defined to use the items feature!");
			allowItemShortcut = false;
			return;
		}
		validShortcutStyles = backpackStylesMap.keySet();
		if(!backpackStylesMap.containsKey(MagicValues.BACKPACK_STYLE_NAME_DEFAULT))
		{
			String defaultBackpackItemName = getString("Defaults.BackpackItem", "unknown");
			if(!backpackStylesMap.containsKey(defaultBackpackItemName))
			{
				String tmp = validShortcutStyles.iterator().next();
				logger.warning("Unknown default backpack item '" + defaultBackpackItemName + "'. Using '" + tmp + "' instead.");
				defaultBackpackItemName = tmp;
			}
			backpackStylesMap.put(MagicValues.BACKPACK_STYLE_NAME_DEFAULT, new ItemConfig(MagicValues.BACKPACK_STYLE_NAME_DEFAULT, backpackStylesMap.get(defaultBackpackItemName)));
		}
	}

	private void loadItemConfigs(final @NotNull String parentKey, final @NotNull Map<String, ItemConfig> itemConfigs)
	{
		getYamlE().getKeysFiltered(parentKey + "\\.[^.]*\\.Material").forEach(materialKey -> {
			final String key = materialKey.substring(0, materialKey.length() - ".Material".length());
			final ItemConfig itemConfig = ItemConfig.fromConfig(this, key, this::translateItemText);
			if(itemConfig != null)
			{
				if(parentKey.equals("Items") && itemConfig.getName().equals(MagicValues.BACKPACK_STYLE_NAME_DISABLED)) return;
				itemConfigs.put(itemConfig.getName(), itemConfig);
			}
		});
	}

	private @NotNull String translateItemText(@NotNull String text)
	{
		text = ChatColor.translateAlternateColorCodes('&', text);
		Matcher matcher = ITEM_TEXT_PLACEHOLDER_PATTERN.matcher(text);
		StringBuffer buffer = new StringBuffer();
		while(matcher.find())
		{
			String replaced = matcher.group(0);
			final String key = "Items." + matcher.group("placeholder");
			if(plugin.getLanguage().getLangE().isSet("Language." + key))
			{
				replaced = plugin.getLanguage().getTranslated(key);
			}
			matcher.appendReplacement(buffer, replaced);
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	public @NotNull List<ItemConfig> getBackpackItems()
	{
		return new ArrayList<>(backpackStylesMap.values());
	}
}