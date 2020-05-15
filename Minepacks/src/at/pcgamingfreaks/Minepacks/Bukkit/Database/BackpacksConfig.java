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
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.YamlFileManager;

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
	private static final Pattern ITEM_TEXT_PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<placeholder>[\\w-.]+)}");
	@Getter private static BackpacksConfig instance;

	private final @NotNull Minepacks plugin;
	private final Map<String, ItemConfig> itemConfigs = new HashMap<>();
	@Getter private final Set<String> validShortcutStyles = new HashSet<>();
	@Getter private String defaultBackpackItem = "";
	@Getter private boolean allowItemShortcut = true;

	public BackpacksConfig(final @NotNull Minepacks plugin)
	{
		super(plugin, CONFIG_VERSION, CONFIG_VERSION, "backpacks.yml");
		this.plugin = plugin;
		instance = this;
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
		itemConfigs.clear();
		loadItemConfigs("Items");
		if(itemConfigs.isEmpty())
		{
			logger.warning("There musst be at least one item defined to use the items feature!");
			allowItemShortcut = false;
		}
		validShortcutStyles.addAll(getBackpackItems().stream().map(ItemConfig::getName).collect(Collectors.toList()));
		defaultBackpackItem = getString("Defaults.BackpackItem", "unknown");
		if(!validShortcutStyles.contains(defaultBackpackItem))
		{
			String tmp = validShortcutStyles.iterator().next();
			logger.warning("Unknown default backpack item '" + defaultBackpackItem + "'. Using '" + tmp + "' instead.");
			defaultBackpackItem = tmp;
		}
	}

	private void loadItemConfigs(final @NotNull String parentKey)
	{
		getYamlE().getKeysFiltered(parentKey + "\\.[^.]*\\.Material").forEach(materialKey -> {
			final String key = materialKey.substring(0, materialKey.length() - ".Material".length());
			try
			{
				if(!getConfigE().getBoolean(key + "Enabled", true)) return;
				final List<String> lore = getConfigE().getStringList(key + ".Lore", new ArrayList<>(0));
				final List<String> loreFinal;
				if(lore.size() == 0) loreFinal = null;
				else
				{
					loreFinal = new ArrayList<>(lore.size());
					lore.forEach(loreEntry -> loreFinal.add(translateItemData(loreEntry)));
				}
				final String displayName = translateItemData(getConfigE().getString(key + ".DisplayName", "&kBackpack"));
				final String material = getYamlE().getString(key + ".Material");
				final int model = getYamlE().getInt(key + ".Model", 0);
				final int amount = getYamlE().getInt(key + ".Amount", 1);
				itemConfigs.put(key, new ItemConfig(key.substring(parentKey.length() + 1), material, amount, displayName, loreFinal, model, getConfigE().getString(key + ".HeadValue", null)));
			}
			catch(Exception e)
			{
				plugin.getLogger().warning("Failed to load item definition for '" + key + "'! Error: " + e.getMessage());
			}
		});
	}

	private @NotNull String translateItemData(@NotNull String text)
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

	public @Nullable ItemConfig getItemConfig(final @NotNull String name)
	{
		return itemConfigs.get(name);
	}

	public @NotNull List<ItemConfig> getBackpackItems()
	{
		return itemConfigs.entrySet().stream().filter(entry -> entry.getKey().startsWith("Items.")).map(Map.Entry::getValue).collect(Collectors.toList());
	}
}