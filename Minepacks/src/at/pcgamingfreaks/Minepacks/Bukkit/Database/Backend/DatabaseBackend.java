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

package at.pcgamingfreaks.Minepacks.Bukkit.Database.Backend;

import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.InventorySerializer;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.Setter;

public abstract class DatabaseBackend
{
	protected static final String START_UUID_UPDATE = "Start updating database to UUIDs ...", UUIDS_UPDATED = "Updated %d accounts to UUIDs.";

	protected final Minepacks plugin;
	protected final boolean onlineUUIDs;
	@Setter protected boolean asyncSave = true;
	protected boolean useUUIDSeparators;
	@Getter protected final InventorySerializer itsSerializer;
	protected long maxAge;

	protected DatabaseBackend(final @NotNull Minepacks plugin)
	{
		this.plugin = plugin;
		useUUIDSeparators = plugin.getConfiguration().getUseUUIDSeparators();
		onlineUUIDs = plugin.getConfiguration().useOnlineUUIDs();
		maxAge = plugin.getConfiguration().getAutoCleanupMaxInactiveDays();
		itsSerializer = new InventorySerializer(plugin.getLogger());
	}

	public @NotNull String getPlayerFormattedUUID(final @NotNull OfflinePlayer player)
	{
		return (useUUIDSeparators) ? player.getUniqueId().toString() : player.getUniqueId().toString().replace("-", "");
	}

	public void close() {}

	//region DB Functions
	public abstract void updatePlayer(final @NotNull Player player);

	public abstract void saveBackpack(final @NotNull Backpack backpack);

	public void syncCooldown(final @NotNull Player player, long time) {}

	public void getCooldown(final @NotNull Player player, final @NotNull Callback<Long> callback) {}

	public abstract void loadBackpack(final @NotNull OfflinePlayer player, final @NotNull Callback<Backpack> callback);
	//endregion
}