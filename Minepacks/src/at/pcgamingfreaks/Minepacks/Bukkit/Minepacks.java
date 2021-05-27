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

package at.pcgamingfreaks.Minepacks.Bukkit;

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.ManagedUpdater;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlayer;
import at.pcgamingfreaks.Minepacks.Bukkit.API.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Command.CommandManager;
import at.pcgamingfreaks.Minepacks.Bukkit.Command.InventoryClearCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Command.ShortcutCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.*;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.BackpackExtended;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksCommandManager;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksPlayerExtended;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksPluginExtended;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.*;
import at.pcgamingfreaks.Minepacks.Bukkit.SpecialInfoWorker.NoDatabaseWorker;
import at.pcgamingfreaks.Minepacks.Bukkit.SpecialInfoWorker.NoUpgradesFromV1;
import at.pcgamingfreaks.StringUtils;
import at.pcgamingfreaks.Updater.UpdateResponseCallback;
import at.pcgamingfreaks.Version;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class Minepacks extends JavaPlugin implements MinepacksPluginExtended
{
	@Getter private static Minepacks instance = null;

	private ManagedUpdater updater = null;
	@Getter private Config configuration;
	@Getter private BackpacksConfig backpacksConfig;
	@Getter private Language language;
	@Getter private Database database;

	public Message messageNoPermission, messageWorldDisabled, messageNotFromConsole, messageNotANumber;

	private int maxSize;
	@Getter private Set<String> worldBlacklist;
	private WorldBlacklistMode worldBlacklistMode;
	private ItemsCollector collector;
	private CommandManager commandManager;
	private InventoryClearCommand inventoryClearCommand;
	private Collection<GameMode> gameModes;
	@Getter private CooldownHandler cooldownManager = null;
	@Getter private ItemFilter itemFilter = null;
	private Sound openSound = null;
	private ItemShortcut shortcut = null;

	@Override
	public boolean isRunningInStandaloneMode()
	{
		/*if[STANDALONE]
		return true;
		else[STANDALONE]*/
		return false;
		/*end[STANDALONE]*/
	}

	@Override
	public void onEnable()
	{
		if(!checkPCGF_PluginLib()) return;

		updater = new ManagedUpdater(this);
		instance = this;
		try
		{
			configuration = new Config(this);
		}
		catch(IllegalStateException e)
		{
			if(e.getMessage().equals("Upgrading from Minepacks v1.x is not supported!"))
			{
				getLogger().severe(e.getMessage());
				new NoUpgradesFromV1(this);
				return;
			}
		}
		updater.setChannel(configuration.getUpdateChannel());
		if(configuration.useUpdater()) updater.update();

		if(!checkMcVersion()) return;

		backpacksConfig = new BackpacksConfig(this);
		language = new Language(this);
		load();

		getLogger().info(StringUtils.getPluginEnabledMessage(getDescription().getName()));
	}

	private boolean checkMcVersion()
	{
		if(MCVersion.is(MCVersion.UNKNOWN) || MCVersion.isOlderThan(MCVersion.MC_1_8) || MCVersion.isNewerThan(MCVersion.MC_NMS_1_16_R3))
		{
			getLogger().warning(ConsoleColor.RED + "################################" + ConsoleColor.RESET);
			getLogger().warning(ConsoleColor.RED + String.format("Your server version (%1$s) is currently not compatible with your current version (%2$s) of the plugin. " +
			                                                     "Please check for updates!", Bukkit.getVersion(), getDescription().getVersion()) + ConsoleColor.RESET);
			getLogger().warning(ConsoleColor.RED + "################################" + ConsoleColor.RESET);
			Utils.blockThread(5);
			this.setEnabled(false);
			return false;
		}
		return true;
	}

	private boolean checkPCGF_PluginLib()
	{
		// Check if running as standalone edition
		/*if[STANDALONE]
		getLogger().info("Starting Minepacks in standalone mode!");
		if(getServer().getPluginManager().isPluginEnabled("PCGF_PluginLib"))
		{
			getLogger().info("You do have the PCGF_PluginLib installed. You may consider switching to the default version of the plugin to reduce memory load and unlock additional features.");
		}
		else[STANDALONE]*/
		// Not standalone so we should check the version of the PluginLib
		if(at.pcgamingfreaks.PluginLib.Bukkit.PluginLib.getInstance().getVersion().olderThan(new Version(MagicValues.MIN_PCGF_PLUGIN_LIB_VERSION)))
		{
			getLogger().warning("You are using an outdated version of the PCGF PluginLib! Please update it!");
			setEnabled(false);
			return false;
		}
		/*end[STANDALONE]*/
		return true;
	}

	@Override
	public void onDisable()
	{
		if(configuration == null) return;
		if(configuration.useUpdater()) updater.update();
		unload();
		updater.waitForAsyncOperation(); // Wait for an update to finish
		getLogger().info(StringUtils.getPluginDisabledMessage(getDescription().getName()));
		instance = null;
	}

	public void update(final @Nullable UpdateResponseCallback updateResponseCallback)
	{
		updater.update(updateResponseCallback);
	}

	private void load()
	{
		updater.setChannel(configuration.getUpdateChannel());
		language.load(configuration);
		backpacksConfig.loadData();
		database = new Database(this);
		if(!database.available())
		{
			new NoDatabaseWorker(this);
			return;
		}
		maxSize = configuration.getBackpackMaxSize();
		at.pcgamingfreaks.Minepacks.Bukkit.Backpack.setShrinkApproach(configuration.getShrinkApproach());
		at.pcgamingfreaks.Minepacks.Bukkit.Backpack.setTitle(configuration.getBPTitle(), configuration.getBPTitleOther());
		messageNotFromConsole = language.getMessage("NotFromConsole");
		messageNoPermission   = language.getMessage("Ingame.NoPermission");
		messageWorldDisabled  = language.getMessage("Ingame.WorldDisabled");
		messageNotANumber     = language.getMessage("Ingame.NaN");

		commandManager = new CommandManager(this);
		if(configuration.isInventoryManagementClearCommandEnabled()) inventoryClearCommand = new InventoryClearCommand(this);

		//region register events
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new BackpackEventListener(this), this);
		if(configuration.getDropOnDeath()) pluginManager.registerEvents(new DropOnDeath(this), this);
		if(configuration.isItemFilterEnabled())
		{
			itemFilter = new ItemFilter(this);
			pluginManager.registerEvents(itemFilter, this);
		}
		if(configuration.isShulkerboxesDisable()) pluginManager.registerEvents(new DisableShulkerboxes(this), this);
		if(configuration.isItemShortcutEnabled() && backpacksConfig.isAllowItemShortcut())
		{
			try
			{
				ItemShortcut itemShortcut = new ItemShortcut(this);
				commandManager.registerSubCommand(new ShortcutCommand(this, itemShortcut));
				pluginManager.registerEvents(itemShortcut, this);
				shortcut = itemShortcut;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else shortcut = null;
		if(configuration.isWorldWhitelistMode()) pluginManager.registerEvents(new WorldBlacklistUpdater(this), this);
		if(configuration.getCommandCooldown() > 0)
		{
			cooldownManager = new CooldownHandler(this);
			if(configuration.isCommandCooldownSyncEnabled()) pluginManager.registerEvents(cooldownManager, this);
		}
		//endregion
		if(configuration.isItemCollectorEnabled()) collector = new ItemsCollector(this);
		worldBlacklist = configuration.getWorldBlacklist();
		worldBlacklistMode = (worldBlacklist.size() == 0) ? WorldBlacklistMode.None : configuration.getWorldBlockMode();

		gameModes = configuration.getAllowedGameModes();

		openSound = configuration.getOpenSound();
	}

	private void unload()
	{
		if(inventoryClearCommand != null)
		{
			inventoryClearCommand.close();
			inventoryClearCommand = null;
		}
		if(collector != null) collector.close();
		commandManager.close();
		if(collector != null) collector.cancel();
		if(database != null) database.close(); // Close the DB connection, we won't need them any longer
		HandlerList.unregisterAll(this); // Stop the listeners
		cooldownManager = null;
		getServer().getScheduler().cancelTasks(this); // Kill all running task
		itemFilter = null;
	}

	public void reload()
	{
		unload();
		configuration.reload();
		backpacksConfig.reload();
		load();
	}

	@Override
	public void openBackpack(final @NotNull Player opener, final @NotNull MinepacksPlayer owner, final boolean editable, final @Nullable String title)
	{
		owner.getBackpack(backpack -> openBackpack(opener, backpack, editable, title));
	}

	@Override
	public void openBackpack(final @NotNull Player opener, final @NotNull Backpack backpack, final boolean editable, final @Nullable String title)
	{
		WorldBlacklistMode disabled = isDisabled(opener);
		if(disabled != WorldBlacklistMode.None)
		{
			switch(disabled)
			{
				case Message: messageWorldDisabled.send(opener); break;
				case MissingPermission: messageNoPermission.send(opener); break;
			}
			return;
		}
		//noinspection ObjectEquality
		if(opener.getOpenInventory().getTopInventory().getHolder() == backpack) return; // == is fine as there is only one instance of each backpack
		if(openSound != null)
		{
			opener.playSound(opener.getEyeLocation(), openSound, 1, 0);
		}
		backpack.open(opener, editable);
	}

	@Override
	public @Nullable BackpackExtended getBackpackLoadedOnly(@NotNull OfflinePlayer owner)
	{
		MinepacksPlayerExtended minepacksPlayer = getMinepacksPlayerLoadedOnly(owner);
		return (minepacksPlayer != null) ? minepacksPlayer.getBackpack() : null;
	}

	@Override
	public void getBackpack(final @NotNull OfflinePlayer owner, final @NotNull Callback<Backpack> callback)
	{
		getMinepacksPlayer(owner).getBackpack(callback);
	}

	@Override
	public @NotNull MinepacksPlayerData getMinepacksPlayer(final @NotNull OfflinePlayer player)
	{
		return database.getPlayer(player);
	}

	@Override
	public void getMinepacksPlayer(final @NotNull OfflinePlayer player, final @NotNull Callback<MinepacksPlayer> callback)
	{
		getMinepacksPlayer(player).notifyOnLoad(callback);
	}

	@Override
	public @Nullable MinepacksPlayerExtended getMinepacksPlayerLoadedOnly(@NotNull OfflinePlayer player)
	{
		MinepacksPlayerExtended mpPlayer = database.getPlayerCached(player);
		if(mpPlayer != null && mpPlayer.isLoaded()) return mpPlayer;
		return null;
	}

	@Override
	public @NotNull MinepacksCommandManager getCommandManager()
	{
		return commandManager;
	}

	public int getBackpackPermSize(final @NotNull Player player)
	{
		for(int i = maxSize; i > 1; i--)
		{
			if(player.hasPermission("backpack.size." + i)) return i * 9;
		}
		return 9;
	}

	@Override
	public @NotNull WorldBlacklistMode isDisabled(final @NotNull Player player)
	{
		if(worldBlacklistMode == WorldBlacklistMode.None || (worldBlacklistMode != WorldBlacklistMode.NoPlugin && player.hasPermission(Permissions.IGNORE_WORLD_BLACKLIST))) return WorldBlacklistMode.None;
		if(worldBlacklist.contains(player.getWorld().getName().toLowerCase(Locale.ROOT))) return worldBlacklistMode;
		return WorldBlacklistMode.None;
	}

	@Override
	public boolean isPlayerGameModeAllowed(final @NotNull Player player)
	{
		return gameModes.contains(player.getGameMode()) || player.hasPermission(Permissions.IGNORE_GAME_MODE);
	}

	@Override
	public boolean isBackpackItem(final @Nullable ItemStack itemStack)
	{
		if(shortcut == null) return false;
		for(ItemConfig itemConfig : backpacksConfig.getBackpackItems())
		{
			if(shortcut.isItemShortcut(itemStack, itemConfig)) return true;
		}
		return false;
	}

	@Override
	public @NotNull Version getVersion()
	{
		return new Version(getDescription().getVersion());
	}
}