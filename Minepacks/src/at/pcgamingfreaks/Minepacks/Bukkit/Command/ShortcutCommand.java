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

package at.pcgamingfreaks.Minepacks.Bukkit.Command;

import at.pcgamingfreaks.Bukkit.GUI.GuiBuilder;
import at.pcgamingfreaks.Bukkit.GUI.GuiButton;
import at.pcgamingfreaks.Bukkit.GUI.IGui;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.MinepacksPlayerData;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.ItemShortcut;
import at.pcgamingfreaks.Minepacks.Bukkit.MagicValues;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import at.pcgamingfreaks.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ShortcutCommand extends MinepacksCommand
{
	private final String[] setSwitch;
	private final String descriptionChose;
	private final Message messageItemGiven, messageItemGivenOther, messageShortcutSet, messageUnknownShortcutStyle, messageUnknownPlayer;
	private final ItemShortcut itemShortcut;
	private final Set<String> validShortcutStyles;
	private final IGui gui;
	private final boolean playerChoice, allowPlayerDisable;

	public ShortcutCommand(final @NotNull Minepacks plugin, final @NotNull ItemShortcut itemShortcut)
	{
		super(plugin, "shortcut", plugin.getLanguage().getTranslated("Commands.Description.Shortcut"), Permissions.USE, false, plugin.getLanguage().getCommandAliases("Shortcut"));
		this.itemShortcut = itemShortcut;
		descriptionChose = plugin.getLanguage().getTranslated("Commands.Description.ShortcutChose");
		messageItemGiven = plugin.getLanguage().getMessage("Ingame.Shortcut.Given");
		messageItemGivenOther = plugin.getLanguage().getMessage("Ingame.Shortcut.GivenOther");
		messageShortcutSet = plugin.getLanguage().getMessage("Ingame.Shortcut.Set");
		messageUnknownPlayer = plugin.getLanguage().getMessage("Ingame.Shortcut.UnknownPlayer").replaceAll("\\{Name}", "%s");
		messageUnknownShortcutStyle = plugin.getLanguage().getMessage("Ingame.Shortcut.UnknownShortcutStyle").replaceAll("\\{ShortcutStyle}", "%s");

		setSwitch = plugin.getLanguage().getSwitch("Set", "set");

		playerChoice = plugin.getConfiguration().isItemShortcutPlayerChoiceEnabled();
		allowPlayerDisable = plugin.getConfiguration().isItemShortcutPlayerDisableItemEnabled();

		/*if[STANDALONE]
		plugin.getServer().getPluginManager().registerEvents(new at.pcgamingfreaks.Bukkit.GUI.GuiListener(), plugin);
		end[STANDALONE]*/

		if(playerChoice)
		{
			validShortcutStyles = plugin.getBackpacksConfig().getBackpackItems().stream().map(ItemConfig::getName).collect(Collectors.toSet());
			if(allowPlayerDisable) validShortcutStyles.add(MagicValues.BACKPACK_STYLE_NAME_DISABLED);

			gui = buildGui(plugin);
		}
		else
		{
			validShortcutStyles = null;
			gui = null;
		}
	}

	private @NotNull IGui buildGui(final @NotNull Minepacks plugin)
	{
		final String setCommandBase = plugin.getLanguage().getCommandAliases("Backpack", "backpack")[0] + ' ' + plugin.getLanguage().getCommandAliases("Shortcut", "shortcut")[0] + ' ' + setSwitch[0] + ' ';
		final List<ItemConfig> backpackItems = plugin.getBackpacksConfig().getBackpackItems();
		final int buttonCount = backpackItems.size() + (allowPlayerDisable ? 1: 0);
		final int buttonCountAligned = ((buttonCount / 9) + 1) * 9; // Aligns it to a multiple of 9
		final GuiBuilder guiBuilder = new GuiBuilder(plugin.getLanguage().getTranslated("Ingame.Shortcut.GUI.Title"));
		//region add item buttons
		for(ItemConfig itemConfig : backpackItems)
		{
			if(itemConfig.getName().equals(MagicValues.BACKPACK_STYLE_NAME_DEFAULT)) continue;
			GuiButton button = new GuiButton(itemConfig.make(1), (player, clickType, cursor) -> { player.performCommand(setCommandBase + itemConfig.getName()); player.closeInventory(); });
			guiBuilder.addButton(button);
		}
		//endregion
		//region add empty slots to place special buttons at the end of the line
		for(int i = buttonCount; i < buttonCountAligned; i++)
		{
			guiBuilder.addButton(GuiButton.EMPTY_BUTTON);
		}
		//endregion
		//region set default button
		ItemConfig defaultItem = plugin.getBackpacksConfig().getBackpackStylesMap().get(MagicValues.BACKPACK_STYLE_NAME_DEFAULT);
		ItemStack item = new ItemConfig(defaultItem.getName(), defaultItem.getMaterial().name(), defaultItem.getAmount(), plugin.getLanguage().getTranslated("Ingame.Shortcut.GUI.TitleDefault"), plugin.getLanguage().getTranslatedList("Ingame.Shortcut.GUI.DescriptionDefault"), defaultItem.getModel(), defaultItem.getValue()).make(1);
		guiBuilder.addButton(new GuiButton(item, (player, clickType, cursor) -> { player.performCommand(setCommandBase + MagicValues.BACKPACK_STYLE_NAME_DEFAULT); player.closeInventory(); }));
		//endregion
		//region set disable button
		if(allowPlayerDisable)
		{
			item = new ItemConfig(MagicValues.BACKPACK_STYLE_NAME_DISABLED, "BARRIER", 1, plugin.getLanguage().getTranslated("Ingame.Shortcut.GUI.TitleNone"), plugin.getLanguage().getTranslatedList("Ingame.Shortcut.GUI.DescriptionNone"), -1, null).make(1);
			guiBuilder.addButton(new GuiButton(item, (player, clickType, cursor) -> { player.performCommand(setCommandBase + MagicValues.BACKPACK_STYLE_NAME_DISABLED); player.closeInventory(); }));
		}
		//endregion
		return guiBuilder.build();
	}

	@Override
	public void execute(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		if(!(sender instanceof Player))
		{
			if(args.length == 1) giveItemOther(sender, args[0]);
			else showHelp(sender, mainCommandAlias);
		}
		else
		{
			if(args.length >= 1)
			{
				if(playerChoice && sender.hasPermission(Permissions.CHOSE_DESIGN) && StringUtils.arrayContainsIgnoreCase(setSwitch, args[0]))
				{
					if(args.length == 1) gui.show((Player) sender);
					else
					{
						if(validShortcutStyles.contains(args[1])) //TODO make test case insensitive
						{
							((Minepacks) plugin).getMinepacksPlayer((OfflinePlayer) sender).notifyOnLoad(player -> {
								MinepacksPlayerData playerData = ((MinepacksPlayerData) player);
								ItemConfig itemConfig = ((Minepacks) plugin).getBackpacksConfig().getBackpackStylesMap().get(args[1]);
								ItemConfig oldItemConfig = playerData.getBackpackStyle();
								if(itemConfig == oldItemConfig) return; // == is fine here
								playerData.setBackpackStyle(itemConfig);
								if(oldItemConfig != null)
								{ // Remove old item
									playerData.setLastBackpackSlot(itemShortcut.removeBackpackItem((Player) sender, oldItemConfig));
								}
								itemShortcut.addItem(playerData, ((Player) sender));
								messageShortcutSet.send(sender); //TODO add more information
							});
						}
						else
						{
							messageUnknownShortcutStyle.send(sender, args[1]);
						}
					}
				}
				else if(sender.hasPermission(Permissions.OTHERS)) giveItemOther(sender, args[1]);
				else showHelp(sender, mainCommandAlias);
			}
			else
			{
				itemShortcut.addItem((Player) sender);
				messageItemGiven.send(sender);
			}
		}
	}

	private void giveItemOther(final @NotNull CommandSender sender, final @NotNull String target)
	{
		Player player = Bukkit.getPlayer(target);
		if(player != null)
		{
			itemShortcut.addItem(player);
			messageItemGivenOther.send(sender);
		}
		else messageUnknownPlayer.send(sender, target);
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		if(args.length > 0)
		{
			List<String> help = new ArrayList<>();
			if(playerChoice && sender.hasPermission(Permissions.CHOSE_DESIGN))
			{
				final String arg = args[args.length - 1].toLowerCase(Locale.ENGLISH);
				if(args.length == 1) help.addAll(Arrays.stream(setSwitch).filter(s -> s.toLowerCase(Locale.ENGLISH).startsWith(arg)).collect(Collectors.toList()));
				else if(args.length == 2) return validShortcutStyles.stream().filter(style -> style.toLowerCase(Locale.ENGLISH).startsWith(arg)).collect(Collectors.toList());
			}
			if(args.length == 1 && sender.hasPermission(Permissions.OTHERS)) help.addAll(Utils.getPlayerNamesStartingWith(args[args.length - 1], sender));
			return help;
		}
		return null;
	}

	@Override
	public @Nullable List<HelpData> getHelp(final @NotNull CommandSender sender)
	{
		List<HelpData> help = new ArrayList<>(2);
		if(playerChoice && sender.hasPermission(Permissions.CHOSE_DESIGN))
		{
			help.add(new HelpData(getTranslatedName(), "set", descriptionChose));
		}
		//noinspection ConstantConditions
		help.addAll(super.getHelp(sender));
		return help;
	}
}