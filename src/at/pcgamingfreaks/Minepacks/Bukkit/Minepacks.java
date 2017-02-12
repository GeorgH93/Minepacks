/*
 *   Copyright (C) 2016 GeorgH93
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
import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin;
import at.pcgamingfreaks.Minepacks.Bukkit.Commands.OnCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Config;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Language;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.DisableShulkerboxes;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Database;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.DropOnDeath;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.EventListener;
import at.pcgamingfreaks.Minepacks.Bukkit.Listener.ItemFilter;
import at.pcgamingfreaks.StringUtils;
import at.pcgamingfreaks.Version;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Minepacks extends JavaPlugin implements MinepacksPlugin
{
	private static Version version = new Version("2.0-SNAPSHOT");
	private static Minepacks instance = null;

	public Config config;
	public Language lang;
	private Database database;

	public final Map<UUID, Long> cooldowns = new HashMap<>();

	public String backpackTitleOther, backpackTitle;
	public Message messageNoPermission, messageInvalidBackpack;

	private int maxSize;

	public static Minepacks getInstance()
	{
		return instance;
	}

	@Override
	public Version getVersion()
	{
		return version;
	}

	@Override
	public void onEnable()
	{
		Utils.warnOnJava_1_7(getLogger());
		version = new Version(getDescription().getVersion());
		//region Check compatibility with used minecraft version
		if(MCVersion.is(MCVersion.UNKNOWN) || MCVersion.isNewerThan(MCVersion.MC_NMS_1_11_R1))
		{
			String name = Bukkit.getServer().getClass().getPackage().getName();
			String[] version = name.substring(name.lastIndexOf('.') + 2).split("_");
			this.warnOnVersionIncompatibility(version[0] + "." + version[1]);
			this.setEnabled(false);
			return;
		}
		//endregion
		instance = this;
		config = new Config(this);
		lang = new Language(this);
		lang.load(config.getLanguage(), config.getLanguageUpdateMode());
		database = Database.getDatabase(this);
		getCommand("backpack").setExecutor(new OnCommand(this));

		//region register events
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new EventListener(this), this);
		if(config.getDropOnDeath()) pluginManager.registerEvents(new DropOnDeath(this), this);
		if(config.isItemFilterEnabled()) pluginManager.registerEvents(new ItemFilter(this), this);
		if(config.isShulkerboxesDisable()) pluginManager.registerEvents(new DisableShulkerboxes(this), this);
		//endregion

		if(config.getFullInvCollect())
		{
			(new ItemsCollector(this)).runTaskTimer(this, config.getFullInvCheckInterval(), config.getFullInvCheckInterval());
		}

		maxSize = config.getBackpackMaxSize();
		backpackTitleOther = config.getBPTitleOther();
		backpackTitle = StringUtils.limitLength(config.getBPTitle(), 32);
		messageNoPermission = lang.getMessage("Ingame.NoPermission");
		messageInvalidBackpack = lang.getMessage("Ingame.InvalidBackpack");
		getServer().getServicesManager().register(Minepacks.class, this, this, ServicePriority.Normal);

		if(config.getAutoUpdate()) // Lets check for updates
		{
			getLogger().info("Checking for updates ...");
			Updater updater = new Updater(this, this.getFile(), true, 83445); // Create a new updater with dev.bukkit.org as update provider
			updater.update(); // Starts the update
		}
		StringUtils.getPluginEnabledMessage(getDescription().getName());
	}

	@Override
	public void onDisable()
	{
		Updater updater = null;
		if(config.getAutoUpdate()) // Lets check for updates
		{
			getLogger().info("Checking for updates ...");
			updater = new Updater(this, this.getFile(), true, 83445); // Create a new updater with dev.bukkit.org as update provider
			updater.update(); // Starts the update, if there is a new update available it will download while we close the rest
		}
		getServer().getScheduler().cancelTasks(this); // Stop the listener, we don't need them any longer
		database.close(); // Close the DB connection, we won't need them any longer
		instance = null;
		if(updater != null) updater.waitForAsyncOperation(); // The update can download while we kill the listeners and close the DB connections
		StringUtils.getPluginDisabledMessage(getDescription().getName());
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

	public Database getDb()
	{
		return database;
	}

	@Override
	public void openBackpack(@NotNull final Player opener, @NotNull final OfflinePlayer owner, final boolean editable)
	{
		Validate.notNull(owner);
		database.getBackpack(owner, new Callback<Backpack>()
		{
			@Override
			public void onResult(Backpack backpack)
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
		if(backpack == null)
		{
			messageInvalidBackpack.send(opener);
			return;
		}
		backpack.open(opener, editable);
	}

	@Override
	public @Nullable Backpack getBackpack(@NotNull OfflinePlayer owner)
	{
		return database.getBackpack(owner, false);
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

	public int getBackpackPermSize(Player player)
	{
		for(int i = maxSize; i > 1; i--)
		{
			if(player.hasPermission("backpack.size." + i))
			{
				return i * 9;
			}
		}
		return 9;
	}
}