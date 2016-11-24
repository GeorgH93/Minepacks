/*
 *   Copyright (C) 2014-2016 GeorgH93
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

package at.pcgamingfreaks.Minepacks;

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.Updater;
import at.pcgamingfreaks.Bukkit.Utils;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Minepacks.Database.Config;
import at.pcgamingfreaks.Minepacks.Database.Database;
import at.pcgamingfreaks.Minepacks.Database.Language;
import at.pcgamingfreaks.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.logging.Logger;

public class Minepacks extends JavaPlugin
{
	private static Minepacks instance = null;

	public Logger log;
	public Config config;
	public Language lang;
	public Database DB;

	public HashMap<Player, Long> cooldowns = new HashMap<>();

	public String backpackTitleOther, backpackTitle;
	public String messageInvalidBackpack;

	private int maxSize;

	public static Minepacks getInstance()
	{
		return instance;
	}

	@Override
	public void onEnable()
	{
		log = getLogger();
		Utils.warnOnJava_1_7(log);
		instance = this;
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
		config = new Config(this);
		lang = new Language(this);
		lang.load(config.getLanguage(), config.getLanguageUpdateMode());
		DB = Database.getDatabase(this);
		getCommand("backpack").setExecutor(new OnCommand(this));
		getServer().getPluginManager().registerEvents(new EventListener(this), this);

		if(config.getFullInvCollect())
		{
			(new ItemsCollector(this)).runTaskTimer(this, config.getFullInvCheckInterval(), config.getFullInvCheckInterval());
		}

		maxSize = config.getBackpackMaxSize();
		backpackTitleOther = config.getBPTitleOther();
		backpackTitle = StringUtils.limitLength(config.getBPTitle(), 32);
		messageInvalidBackpack = lang.getTranslated("Ingame.InvalidBackpack");
		getServer().getServicesManager().register(Minepacks.class, this, this, ServicePriority.Normal);

		if(config.getAutoUpdate()) // Lets check for updates
		{
			log.info("Checking for updates ...");
			Updater updater = new Updater(this, this.getFile(), true, 83445); // Create a new updater with dev.bukkit.org as update provider
			updater.update(); // Starts the update
		}

		log.info(lang.get(ConsoleColor.GREEN + "Minepacks has been enabled! " + ConsoleColor.YELLOW + ":)"));
	}

	@Override
	public void onDisable()
	{
		Updater updater = null;
		if(config.getAutoUpdate()) // Lets check for updates
		{
			log.info("Checking for updates ...");
			updater = new Updater(this, this.getFile(), true, 83445); // Create a new updater with dev.bukkit.org as update provider
			updater.update(); // Starts the update, if there is a new update available it will download while we close the rest
		}
		getServer().getScheduler().cancelTasks(this); // Stop the listener, we don't need them any longer
		DB.close(); // Close the DB connection, we won't need them any longer
		if(updater != null) updater.waitForAsyncOperation(); // The update can download while we kill the listeners and close the DB connections
		instance = null;
		log.info(lang.get(ConsoleColor.RED + "Minepacks has been disabled. " + ConsoleColor.YELLOW + ":)"));
	}

	public void warnOnVersionIncompatibility(String version)
	{
		log.warning(ConsoleColor.RED + "################################" + ConsoleColor.RESET);
		log.warning(ConsoleColor.RED + String.format("Your minecraft version (MC %1$s) is currently not compatible with this plugins version (%2$s). Please check for updates!", version, getDescription().getVersion()) + ConsoleColor.RESET);
		log.warning(ConsoleColor.RED + "################################" + ConsoleColor.RESET);
		Utils.blockThread(5);
	}

	public void openBackpack(final Player opener, OfflinePlayer owner, final boolean editable)
	{
		if(owner == null)
		{
			return;
		}
		DB.getBackpack(owner, new Database.Callback<Backpack>()
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

	public void openBackpack(Player opener, Backpack backpack, boolean editable)
	{
		if(backpack == null)
		{
			opener.sendMessage(messageInvalidBackpack);
			return;
		}
		backpack.open(opener, editable);
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