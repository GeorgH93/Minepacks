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

package at.pcgamingfreaks.Minepacks.Bukkit.API;

import at.pcgamingfreaks.Bukkit.Message.IMessage;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface MinepacksPlayer
{
	@NotNull String getName();

	@NotNull UUID getUUID();

	@NotNull OfflinePlayer getPlayer();

	@Nullable Player getPlayerOnline();

	@NotNull String getDisplayName();

	boolean hasPermission(final @NotNull String permission);

	boolean isOnline();

	void setBackpackStyle(final @NotNull String style);

	@Nullable ItemStack getBackpackItem();

	@NotNull Backpack getBackpack();

	void send(@NotNull IMessage message, @Nullable Object... args);

	void sendMessage(@NotNull IMessage message, @Nullable Object... args);
}