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
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backpack.BackpackType;
import at.pcgamingfreaks.Minepacks.Bukkit.GUI.ButtonAction;
import at.pcgamingfreaks.Minepacks.Bukkit.GUI.ButtonConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.GUI.ControlPosition;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.MagicValues;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Utils;
import at.pcgamingfreaks.Version;
import at.pcgamingfreaks.YamlFileManager;
import at.pcgamingfreaks.YamlFileUpdateMethod;
import at.pcgamingfreaks.yaml.YAML;
import at.pcgamingfreaks.yaml.YamlKeyNotFoundException;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackpacksConfig extends Configuration
{
	private static final int CONFIG_VERSION = 1;
	private static final Pattern ITEM_TEXT_PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<placeholder>[\\w_-]+)}");
	@Getter private static BackpacksConfig instance;

	private final @NotNull Minepacks plugin;
	@Getter private Set<String> validShortcutStyles;
	@Getter private final Map<String, ItemConfig> backpackStylesMap = new HashMap<>();
	@Getter private final Map<String, ButtonConfig> buttonConfigMap = new HashMap<>();
	@Getter private final Map<String, ButtonConfig[]> controlLayoutsMap = new HashMap<>();
	@Getter private ItemConfig defaultBlockedSlotItem;
	@Getter private ControlPosition defaultControlPosition;
	@Getter private String defaultControlLayout;
	@Getter private boolean allowItemShortcut = true;
	@Getter private boolean allowControlsInRow7 = false;
	@Getter private final Map<String, BackpackType> backpackTypes = new HashMap<>();

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
		allowControlsInRow7 = getConfigE().getBoolean("AllowControlsInRow7", false) && MCVersion.isOlderThan(MCVersion.MC_1_14);
		backpackStylesMap.clear();
		buttonConfigMap.clear();
		controlLayoutsMap.clear();
		loadBackpackStyles();
		loadButtonConfigs();
		ButtonConfig btConf = buttonConfigMap.get(getConfigE().getString("Defaults.BlockedSlots", "Blocked"));
		if(btConf != null)
		{
			defaultBlockedSlotItem = btConf.getItem();
		}
		else
		{
			defaultBlockedSlotItem = new ItemConfig("Nothing", "Stone", 1, "Missing", null, -1, null);
		}
		loadControlLayouts();
		defaultControlPosition = Utils.getEnum(getConfigE().getString("Defaults.ControlPosition", "bottom_right"), ControlPosition.BOTTOM_RIGHT);
		defaultControlLayout = getConfigE().getString("Defaults.ControlLayout", "None");
		if(!controlLayoutsMap.containsKey(defaultControlLayout))
		{
			getLogger().warning("Default control layout '" + defaultControlLayout + "' is not defined! Disabling controls by default.");
			controlLayoutsMap.put(defaultControlLayout, new ButtonConfig[0]);
		}

	}

	private void loadBackpackTypes()
	{
		backpackTypes.clear();
		try
		{
			YAML backpacksSection = getConfigE().getSection("Backpacks");
			for(String backpackName : backpacksSection.getNodeKeys())
			{
				if (backpackName.isEmpty()) continue;
				backpackTypes.put(backpackName, new BackpackType(this, "Backpacks." + backpackName));
			}
		}
		catch(YamlKeyNotFoundException e)
		{
			logger.severe("Missing 'Backpacks' config in backpacks.yml!");
		}
		if (backpackTypes.isEmpty())
			backpackTypes.put("1", new BackpackType());
	}

	private void loadControlLayouts()
	{
		getYamlE().getKeysFiltered("ControlLayouts\\.[^.]").forEach(key -> {
			final List<String> buttonNames = getYamlE().getStringList(key, new ArrayList<>(0));
			final String name = key.substring(key.indexOf('.'));
			final ButtonConfig[] controls = new ButtonConfig[buttonNames.size()];
			for(int i = 0; i < buttonNames.size(); i++)
			{
				String buttonName = buttonNames.get(i);
				ButtonConfig buttonConfig = null;
				if(buttonName == null || buttonName.isEmpty())
				{
					getLogger().warning("Empty button name in control layout '" + name + "' at index " + i);
				}
				else
				{
					buttonConfig = buttonConfigMap.get(buttonName);
					if(buttonConfig == null)
					{
						getLogger().warning("Unable to find button '" + buttonName + "' used in control layout '" + name + "'.");
					}
				}
				if(buttonConfig == null)
				{
					buttonConfig = new ButtonConfig(defaultBlockedSlotItem, ButtonAction.NOTHING);
				}
				controls[i] = buttonConfig;
			}
			controlLayoutsMap.put(name, controls);
		});
	}

	private void loadButtonConfigs()
	{
		getYamlE().getKeysFiltered("Buttons\\.[^.]*\\.Material").forEach(materialKey -> {
			final String key = materialKey.substring(0, materialKey.length() - ".Material".length());
			final ItemConfig itemConfig = ItemConfig.fromConfig(this, key, this::translateItemText);
			if(itemConfig != null)
			{
				buttonConfigMap.put(itemConfig.getName(), new ButtonConfig(itemConfig, Utils.getEnum(getConfigE().getString(key + ".Action", "nothing"), ButtonAction.NOTHING)));
			}
		});

	}

	private void loadBackpackStyles()
	{
		backpackStylesMap.clear();
		getYamlE().getKeysFiltered("Items\\.[^.]*\\.Material").forEach(materialKey -> {
			final String key = materialKey.substring(0, materialKey.length() - ".Material".length());
			if(!getConfigE().getBoolean(key + ".Enabled", true)) return;
			final ItemConfig itemConfig = ItemConfig.fromConfig(this, key, this::translateItemText);
			if(itemConfig != null)
			{
				if(itemConfig.getName().equals(MagicValues.BACKPACK_STYLE_NAME_DISABLED)) return;
				backpackStylesMap.put(itemConfig.getName(), itemConfig);
			}
		});
		if(backpackStylesMap.isEmpty())
		{
			logger.warning("There musst be at least one item defined to use the items feature!");
			allowItemShortcut = false;
		}
		else
		{
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

	public int getAutoIntValue(final @NotNull String key)
	{
		int intValue = -1;
		String strValue = getConfigE().getString(key, "auto");
		if(!strValue.equalsIgnoreCase("auto"))
		{
			intValue = Utils.tryParse(strValue, -99);
			if(intValue < -1 || intValue == 0)
			{
				getLogger().warning("Invalid value for " + key + ", falling back to auto!");
				intValue = -1;
			}
		}
		return intValue;
	}

	public @NotNull List<ItemConfig> getBackpackItems()
	{
		return new ArrayList<>(backpackStylesMap.values());
	}
}