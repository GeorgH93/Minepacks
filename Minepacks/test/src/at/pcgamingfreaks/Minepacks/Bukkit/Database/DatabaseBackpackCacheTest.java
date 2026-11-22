/*
 *   Copyright (C) 2026 GeorgH93
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

import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Regression test for issue #413: concurrent cache misses must not create two backpack instances for one owner,
 * otherwise two viewers get separate inventories and can each withdraw the same items.
 */
public class DatabaseBackpackCacheTest
{
	private static class PendingLoadDatabase extends Database
	{
		final List<Callback<Backpack>> pendingLoads = new ArrayList<>();

		PendingLoadDatabase(Minepacks plugin) { super(plugin); }

		@Override public void updatePlayer(Player player) {}

		@Override public void saveBackpack(Backpack backpack) {}

		@Override
		protected void loadBackpack(OfflinePlayer player, Callback<Backpack> callback) { pendingLoads.add(callback); }
	}

	@TempDir File dataFolder;
	private MockedStatic<Bukkit> bukkit;
	private PendingLoadDatabase database;
	private OfflinePlayer owner;

	@BeforeEach
	public void setup()
	{
		bukkit = mockStatic(Bukkit.class);
		bukkit.when(Bukkit::getPluginManager).thenReturn(mock(PluginManager.class));
		// PluginLib platform detection runs when the Backpack class is first loaded
		bukkit.when(Bukkit::getName).thenReturn("Paper");
		bukkit.when(Bukkit::getVersion).thenReturn("git-Paper-1 (MC: 1.21.8)");
		bukkit.when(Bukkit::getBukkitVersion).thenReturn("1.21.8-R0.1-SNAPSHOT");
		Config config = mock(Config.class);
		when(config.isBungeeCordModeEnabled()).thenReturn(true); // Uses OnDisconnect strategy, avoids Minepacks.getInstance()
		Minepacks plugin = mock(Minepacks.class);
		when(plugin.getConfiguration()).thenReturn(config);
		when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
		when(plugin.getDataFolder()).thenReturn(dataFolder);
		database = new PendingLoadDatabase(plugin);
		owner = mock(OfflinePlayer.class);
		when(owner.getUniqueId()).thenReturn(UUID.randomUUID());
	}

	@AfterEach
	public void teardown()
	{
		bukkit.close();
	}

	@Test
	public void concurrentLoadsShareOneBackpackInstance()
	{
		List<at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack> results = new ArrayList<>();
		database.getBackpack(owner, results::add); // Owner runs /backpack
		database.getBackpack(owner, results::add); // Second player runs /backpack <owner> before the first load finished
		assertEquals(2, database.pendingLoads.size());

		database.pendingLoads.get(0).onResult(mock(Backpack.class));
		database.pendingLoads.get(1).onResult(mock(Backpack.class));

		assertEquals(2, results.size());
		assertSame(results.get(0), results.get(1));
		assertSame(results.get(0), database.getBackpack(owner));
	}

	@Test
	public void joinLoadDoesNotReplaceBackpackAlreadyHandedOut()
	{
		List<at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack> results = new ArrayList<>();
		database.asyncLoadBackpack(owner); // Load on join
		database.getBackpack(owner, results::add); // Backpack opened before the join load finished

		database.pendingLoads.get(1).onResult(mock(Backpack.class));
		database.pendingLoads.get(0).onResult(mock(Backpack.class));

		assertSame(results.get(0), database.getBackpack(owner));
	}
}
