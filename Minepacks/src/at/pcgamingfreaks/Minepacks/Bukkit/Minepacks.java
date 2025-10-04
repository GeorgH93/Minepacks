/*
 *   Copyright (C) 2025 GeorgH93
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

import at.pcgamingfreaks.Bukkit.Config.PermissionLoader;
import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.ManagedUpdater;
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Util.InventoryUtils;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Minepacks.Bukkit.API.*;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Command.CommandManager;
import at.pcgamingfreaks.Minepacks.Bukkit.Command.InventoryClearCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Command.ShortcutCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Config;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Database;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Language;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.*;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.ItemFilter;
import at.pcgamingfreaks.Minepacks.Bukkit.Placeholder.PlaceholderManager;
import at.pcgamingfreaks.Minepacks.Bukkit.SpecialInfoWorker.NoDatabaseWorker;
import at.pcgamingfreaks.Minepacks.MagicValues;
import at.pcgamingfreaks.Plugin.IPlugin;
import at.pcgamingfreaks.ServerType;
import at.pcgamingfreaks.Updater.UpdateResponseCallback;
import at.pcgamingfreaks.Util.StringUtils;
import at.pcgamingfreaks.Version;
import at.pcgf.libs.com.tcoded.folialib.FoliaLib;
import at.pcgf.libs.com.tcoded.folialib.impl.PlatformScheduler;

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

import java.io.File;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class Minepacks extends JavaPlugin implements MinepacksPlugin, IPlugin
{
	@Getter private static Minepacks instance = null;
	@Getter private static FoliaLib foliaLib = null;

	private ManagedUpdater updater = null;
	private Config config;
	private Language lang;
	@Getter private Database database;

	public Message messageNoPermission, messageInvalidBackpack, messageWorldDisabled, messageNotFromConsole, messageNotANumber;

	private int maxSize;
	@Getter private Set<String> worldBlacklist;
	private WorldBlacklistMode worldBlacklistMode;
	private ItemsCollector collector;
	private CommandManager commandManager;
	private InventoryClearCommand inventoryClearCommand;
	private Collection<GameMode> gameModes;
	private CooldownManager cooldownManager = null;
	private ItemFilter itemFilter = null;
	private Sound openSound = null;
	private ItemShortcut shortcut = null;
	@Getter private PlaceholderManager placeholderManager = null;

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
		checkOldDataFolder();

		if(!checkPCGF_PluginLib()) return;

		if (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_19_3) && ServerType.isPaperCompatible())
		{
			PermissionLoader.loadPermissionsFromPlugin(this);
		}

		updater = new ManagedUpdater(this);
		instance = this;
		foliaLib = new FoliaLib(this);
		config = new Config(this);
		updater.setChannel(config.getUpdateChannel());
		if(config.useUpdater()) updater.update();

		if(!checkMcVersion()) return;

		lang = new Language(this);
		load();

		getLogger().info(StringUtils.getPluginEnabledMessage(getDescription().getName()));
	}

	private boolean checkMcVersion()
	{
		if (MCVersion.isNewerThan(MCVersion.MC_NMS_1_20_R3) && ServerType.isPaperCompatible())
		{
			getLogger().warning("Paper support is experimental! Use at your own risk!");
			getLogger().warning("No guarantee for data integrity! Backup constantly!");
		}
		// DO NOT REMOVE THIS! This is protecting your data! To add support for a new version, update PCGF PluginLib and then update the last version check!
		if (MCVersion.is(MCVersion.UNKNOWN) || !MCVersion.isUUIDsSupportAvailable() || MCVersion.isNewerThan(MCVersion.MC_NMS_1_21_R6))
		{
			this.warnOnVersionIncompatibility();
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

	private void checkOldDataFolder()
	{
		if(!getDataFolder().exists())
		{
			File oldPluginFolder = new File(getDataFolder().getParentFile(), "MinePacks");
			if(oldPluginFolder.exists() && !oldPluginFolder.renameTo(getDataFolder()))
			{
				getLogger().warning("Failed to rename the plugins data-folder.\n" +
						                    "Please rename the \"MinePacks\" folder to \"Minepacks\" and restart the server, to move your data from Minepacks V1.X to Minepacks V2.X!");
			}
		}
	}

	@Override
	public void onDisable()
	{
		if(config == null) return;
		if(config.useUpdater()) updater.update();
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
		updater.setChannel(config.getUpdateChannel());
		lang.load(config);
		database = Database.getDatabase(this);
		if(database == null)
		{
			new NoDatabaseWorker(this);
			return;
		}
		maxSize = config.getBackpackMaxSize();
		at.pcgamingfreaks.Minepacks.Bukkit.Backpack.setShrinkApproach(config.getShrinkApproach());
		at.pcgamingfreaks.Minepacks.Bukkit.Backpack.setTitle(config.useDynamicBPTitle() ? config.getBPTitle() : config.getBPTitleOther(), config.getBPTitleOther());
		at.pcgamingfreaks.Minepacks.Bukkit.Backpack.setMessageBackpackShrunk(lang.getMessage("Ingame.BackpackShrunk"));
		messageNotFromConsole  = lang.getMessage("NotFromConsole");
		messageNoPermission    = lang.getMessage("Ingame.NoPermission");
		messageInvalidBackpack = lang.getMessage("Ingame.InvalidBackpack");
		messageWorldDisabled   = lang.getMessage("Ingame.WorldDisabled");
		messageNotANumber      = lang.getMessage("Ingame.NaN");

		commandManager = new CommandManager(this);
		if(config.isInventoryManagementClearCommandEnabled()) inventoryClearCommand = new InventoryClearCommand(this);

		//region register events
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new BackpackEventListener(this), this);
		if(config.getDropOnDeath()) pluginManager.registerEvents(new DropOnDeath(this), this);
		if(config.isItemFilterEnabled())
		{
			itemFilter = new ItemFilter(this);
			pluginManager.registerEvents(itemFilter, this);
		}
		if(config.isShulkerboxesDisable()) pluginManager.registerEvents(new DisableShulkerboxes(this), this);
		if(config.isItemShortcutEnabled())
		{
			shortcut = new ItemShortcut(this);
			pluginManager.registerEvents(shortcut, this);
			commandManager.registerSubCommand(new ShortcutCommand(this, shortcut));
		}
		else shortcut = null;
		if(config.isWorldWhitelistMode()) pluginManager.registerEvents(new WorldBlacklistUpdater(this), this);
		//endregion
		if(config.getFullInvCollect() || config.isFullInvToggleAllowed()) collector = new ItemsCollector(this);
		worldBlacklist = config.getWorldBlacklist();
		worldBlacklistMode = (worldBlacklist.isEmpty()) ? WorldBlacklistMode.None : config.getWorldBlockMode();

		gameModes = config.getAllowedGameModes();
		if(config.getCommandCooldown() > 0) cooldownManager = new CooldownManager(this);

		openSound = config.getOpenSound();

		placeholderManager = new PlaceholderManager(this);
	}

	private void unload()
	{
		if(lang == null) return;
		if(inventoryClearCommand != null)
		{
			inventoryClearCommand.close();
			inventoryClearCommand = null;
		}
		if (placeholderManager != null) { placeholderManager.close(); placeholderManager = null; }
		if(collector != null) collector.close();
		if(commandManager != null) commandManager.close();
		if(collector != null) collector.cancel();
		if(database != null) database.close(); // Close the DB connection, we won't need them any longer
		HandlerList.unregisterAll(this); // Stop the listeners
		if(cooldownManager != null) cooldownManager.close();
		cooldownManager = null;
		getScheduler().cancelAllTasks(); // Kill all running task
		itemFilter = null;
	}

	public void reload()
	{
		unload();
		config.reload();
		load();
	}

	public void warnOnVersionIncompatibility()
	{
		getLogger().warning(ConsoleColor.RED + "################################" + ConsoleColor.RESET);
		getLogger().warning(ConsoleColor.RED + String.format("Your minecraft version (MC %1$s) is currently not compatible with this plugins version (%2$s). " +
				                                                     "Please check for updates!", Bukkit.getServer().getVersion(), getDescription().getVersion()) + ConsoleColor.RESET);
		getLogger().warning(ConsoleColor.RED + "################################" + ConsoleColor.RESET);
		Utils.blockThread(5);
	}

	public Config getConfiguration()
	{
		return config;
	}

	public Language getLanguage()
	{
		return lang;
	}

	@Override
	public void openBackpack(@NotNull final Player opener, @NotNull final OfflinePlayer owner, final boolean editable)
	{
		openBackpack(opener, owner, editable, null);
	}

	@Override
	public void openBackpack(@NotNull final Player opener, @Nullable final Backpack backpack, boolean editable)
	{
		openBackpack(opener, backpack, editable, null);
	}

	@Override
	public void openBackpack(@NotNull Player opener, @NotNull OfflinePlayer owner, boolean editable, @Nullable String title)
	{
		database.getBackpack(owner, backpack -> openBackpack(opener, backpack, editable, title));
	}

	@Override
	public void openBackpack(@NotNull Player opener, @Nullable Backpack backpack, boolean editable, @Nullable String title)
	{
		WorldBlacklistMode disabled = isDisabled(opener);
		if(disabled != WorldBlacklistMode.None)
		{
			if (disabled == WorldBlacklistMode.Message) messageWorldDisabled.send(opener);
			else if (disabled == WorldBlacklistMode.MissingPermission) messageNoPermission.send(opener);
			return;
		}
		if(backpack == null)
		{
			messageInvalidBackpack.send(opener);
			return;
		}
		//noinspection ObjectEquality
		if(InventoryUtils.getPlayerTopInventory(opener).getHolder() == backpack) return; // == is fine as there is only one instance of each backpack
		if(openSound != null)
		{
			opener.playSound(opener.getLocation(), openSound, 1, 0);
		}
		backpack.open(opener, editable);
	}

	@Override
	public @Nullable Backpack getBackpackCachedOnly(@NotNull OfflinePlayer owner)
	{
		return database.getBackpack(owner);
	}

	@Override
	public void getBackpack(@NotNull OfflinePlayer owner, @NotNull Callback<Backpack> callback)
	{
		database.getBackpack(owner, callback);
	}

	@Override
	public void getBackpack(@NotNull final OfflinePlayer owner, @NotNull final Callback<Backpack> callback, boolean createNewIfNotExists)
	{
		database.getBackpack(owner, callback, createNewIfNotExists);
	}

	@Override
	public MinepacksCommandManager getCommandManager()
	{
		/*if[STANDALONE]
		return null;
		else[STANDALONE]*/
		return commandManager;
		/*end[STANDALONE]*/
	}

	public int getBackpackPermSize(Player player)
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

	public @Nullable CooldownManager getCooldownManager()
	{
		return cooldownManager;
	}

	@Override
	public @Nullable ItemFilter getItemFilter()
	{
		return itemFilter;
	}

	@Override
	public boolean isBackpackItem(final @Nullable ItemStack itemStack)
	{
		if(shortcut == null) return false;
		return shortcut.isItemShortcut(itemStack);
	}

	public ItemsCollector getItemsCollector()
	{
		return collector;
	}

	public static PlatformScheduler getScheduler()
	{
		return foliaLib.getScheduler();
	}

	@Override
	public @NotNull Version getVersion()
	{
		return new Version(getDescription().getVersion());
	}
}
