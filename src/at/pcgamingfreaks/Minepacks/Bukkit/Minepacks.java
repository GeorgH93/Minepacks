/*
 *   Copyright (C) 2016-2018 GeorgH93
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
import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Updater;
import at.pcgamingfreaks.Bukkit.Utils;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommandManager;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin;
import at.pcgamingfreaks.Minepacks.Bukkit.Command.CommandManager;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Config;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Language;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.DisableShulkerboxes;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Database;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.DropOnDeath;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.EventListener;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.ItemFilter;
import at.pcgamingfreaks.PluginLib.Bukkit.PluginLib;
import at.pcgamingfreaks.StringUtils;
import at.pcgamingfreaks.Updater.UpdateProviders.BukkitUpdateProvider;
import at.pcgamingfreaks.Updater.UpdateProviders.JenkinsUpdateProvider;
import at.pcgamingfreaks.Updater.UpdateProviders.UpdateProvider;
import at.pcgamingfreaks.Version;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Minepacks extends JavaPlugin implements MinepacksPlugin
{
	private static final int BUKKIT_PROJECT_ID = 83445;
	private static final String JENKINS_URL = "https://ci.pcgamingfreaks.at", JENKINS_JOB = "Minepacks V2";
	private static Minepacks instance = null;

	private Config config;
	private Language lang;
	private Database database;

	public final Map<UUID, Long> cooldowns = new HashMap<>();

	public String backpackTitleOther = "%s Backpack", backpackTitle = "Backpack";
	public Message messageNoPermission, messageInvalidBackpack, messageWorldDisabled, messageNotFromConsole;

	private int maxSize;
	private Collection<String> worldBlacklist;
	private WorldBlacklistMode worldBlacklistMode;
	private ItemsCollector collector;
	private CommandManager commandManager;
	private Collection<GameMode> gameModes;

	public static Minepacks getInstance()
	{
		return instance;
	}

	@Override
	public void onEnable()
	{
		Utils.warnOnJava_1_7(getLogger());
		//region Check compatibility with used minecraft version
		if(MCVersion.is(MCVersion.UNKNOWN) || MCVersion.isNewerThan(MCVersion.MC_NMS_1_12_R1))
		{
			String name = Bukkit.getServer().getClass().getPackage().getName();
			String[] version = name.substring(name.lastIndexOf('.') + 2).split("_");
			this.warnOnVersionIncompatibility(version[0] + "." + version[1]);
			this.setEnabled(false);
			return;
		}
		//endregion

		if(PluginLib.getInstance().getVersion().olderThan(new Version("1.0.4-SNAPSHOT")))
		{
			getLogger().warning("You are using an outdated version of the PCGF PluginLib! Please update it!");
			setEnabled(false);
			return;
		}

		//region check if a plugin folder exists (was renamed from MinePacks to Minepacks with the V2.0 update)
		if(!getDataFolder().exists())
		{
			File oldPluginFolder = new File(getDataFolder().getParentFile(), "MinePacks");
			if(oldPluginFolder.exists() && !oldPluginFolder.renameTo(getDataFolder()))
			{
				getLogger().warning("Failed to rename the plugins data-folder.\n" +
						            "Please rename the \"MinePacks\" folder to \"Minepacks\" and restart the server, to move your data from Minepacks V1.X to Minepacks V2.X!");
			}
		}
		//endregion
		instance = this;
		config = new Config(this);
		lang = new Language(this);

		load();

		if(config.getAutoUpdate()) update(null);
		StringUtils.getPluginEnabledMessage(getDescription().getName());
	}

	@Override
	public void onDisable()
	{
		if(config == null) return;
		Updater updater = null;
		if(config.getAutoUpdate()) updater = update(null);
		unload();
		if(updater != null) updater.waitForAsyncOperation();
		StringUtils.getPluginDisabledMessage(getDescription().getName());
		instance = null;
	}

	public Updater update(@Nullable at.pcgamingfreaks.Updater.Updater.UpdaterResponse output)
	{
		UpdateProvider updateProvider;
		if(config.useUpdaterDevBuilds())
		{
			updateProvider = new JenkinsUpdateProvider(JENKINS_URL, JENKINS_JOB, getLogger());
		}
		else
		{
			updateProvider = new BukkitUpdateProvider(BUKKIT_PROJECT_ID, getLogger());
		}
		Updater updater = new Updater(this, this.getFile(), true, updateProvider);
		updater.update(output);
		return updater;
	}

	private void load()
	{
		lang.load(config.getLanguage(), config.getLanguageUpdateMode());
		database = Database.getDatabase(this);
		maxSize = config.getBackpackMaxSize();
		backpackTitleOther = config.getBPTitleOther();
		backpackTitle = StringUtils.limitLength(config.getBPTitle(), 32);
		messageNotFromConsole  = lang.getMessage("NotFromConsole");
		messageNoPermission    = lang.getMessage("Ingame.NoPermission");
		messageInvalidBackpack = lang.getMessage("Ingame.InvalidBackpack");
		messageWorldDisabled   = lang.getMessage("Ingame.WorldDisabled");

		commandManager = new CommandManager(this);

		//region register events
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new EventListener(this), this);
		if(config.getDropOnDeath()) pluginManager.registerEvents(new DropOnDeath(this), this);
		if(config.isItemFilterEnabled()) pluginManager.registerEvents(new ItemFilter(this), this);
		if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) && config.isShulkerboxesDisable()) pluginManager.registerEvents(new DisableShulkerboxes(this), this);
		//endregion
		if(config.getFullInvCollect())
		{
			collector = new ItemsCollector(this);
			collector.runTaskTimer(this, config.getFullInvCheckInterval(), config.getFullInvCheckInterval());
		}
		worldBlacklist = config.getWorldBlacklist();
		if(worldBlacklist.size() == 0)
		{
			worldBlacklistMode = WorldBlacklistMode.None;
		}
		else
		{
			worldBlacklistMode = config.getWorldBlacklistMode();
		}

		gameModes = config.getAllowedGameModes();
	}

	private void unload()
	{
		commandManager.close();
		if(collector != null) collector.cancel();
		if(database != null) database.close();
		HandlerList.unregisterAll(this); // Stop the listeners
		getServer().getScheduler().cancelTasks(this); // Kill all running task
		database.close(); // Close the DB connection, we won't need them any longer
		cooldowns.clear();
	}

	public void reload()
	{
		unload();
		config.reload();
		load();
	}

	public void warnOnVersionIncompatibility(String version)
	{
		getLogger().warning(ConsoleColor.RED + "################################" + ConsoleColor.RESET);
		getLogger().warning(ConsoleColor.RED + String.format("Your minecraft version (MC %1$s) is currently not compatible with this plugins version (%2$s). " +
				                                                     "Please check for updates!", version, getDescription().getVersion()) + ConsoleColor.RESET);
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

	public Database getDatabase()
	{
		return database;
	}

	@Override
	public void openBackpack(@NotNull final Player opener, @NotNull final OfflinePlayer owner, final boolean editable)
	{
		Validate.notNull(owner);
		database.getBackpack(owner, new Callback<at.pcgamingfreaks.Minepacks.Bukkit.Backpack>()
		{
			@Override
			public void onResult(at.pcgamingfreaks.Minepacks.Bukkit.Backpack backpack)
			{
				openBackpack(opener, backpack, editable);
			}

			@Override
			public void onFail() {}
		});
	}

	@Override
	public void openBackpack(@NotNull final Player opener, @Nullable final Backpack backpack, boolean editable)
	{
		Validate.notNull(opener);
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
		if(backpack == null)
		{
			messageInvalidBackpack.send(opener);
			return;
		}
		backpack.open(opener, editable);
	}

	@Override
	public @Nullable Backpack getBackpackCachedOnly(@NotNull OfflinePlayer owner)
	{
		return database.getBackpack(owner);
	}

	@Override
	public void getBackpack(@NotNull OfflinePlayer owner, @NotNull Callback<at.pcgamingfreaks.Minepacks.Bukkit.Backpack> callback)
	{
		database.getBackpack(owner, callback);
	}

	@Override
	public MinepacksCommandManager getCommandManager()
	{
		return commandManager;
	}

	public int getBackpackPermSize(Player player)
	{
		for(int i = maxSize; i > 1; i--)
		{
			if(player.hasPermission("backpack.size." + i)) return i * 9;
		}
		return 9;
	}

	public WorldBlacklistMode isDisabled(Player player)
	{
		if(worldBlacklistMode == WorldBlacklistMode.None || (worldBlacklistMode != WorldBlacklistMode.NoPlugin && player.hasPermission("backpack.ignoreWorldBlacklist"))) return WorldBlacklistMode.None;
		if(worldBlacklist.contains(player.getWorld().getName().toLowerCase())) return worldBlacklistMode;
		return WorldBlacklistMode.None;
	}

	@Override
	public boolean isPlayerGameModeAllowed(Player player)
	{
		return gameModes.contains(player.getGameMode()) || player.hasPermission("backpack.ignoreGameMode");
	}
}