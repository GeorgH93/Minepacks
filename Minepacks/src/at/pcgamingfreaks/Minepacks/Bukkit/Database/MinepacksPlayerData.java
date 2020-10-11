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

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.Message.IMessage;
import at.pcgamingfreaks.Database.Cache.ICacheablePlayer;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksPlayerExtended;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.MagicValues;
import at.pcgamingfreaks.UUIDConverter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class MinepacksPlayerData implements MinepacksPlayerExtended, ICacheablePlayer
{
	@Getter @Setter private @NotNull String name;
	private final @NotNull UUID uuid;
	private final int hash;
	@Getter private final @NotNull OfflinePlayer player;
	@Getter private String backpackStyleName = MagicValues.BACKPACK_STYLE_NAME_DEFAULT;
	private ItemConfig backpackStyle = null;
	@Getter @Setter private Backpack backpack = null;
	@Getter @Setter	private Object databaseKey = null;

	public MinepacksPlayerData(final @Nullable UUID uuid, final @NotNull String name)
	{
		this.name = name;
		this.uuid = (uuid != null) ? uuid : UUIDConverter.getUUIDFromNameAsUUID(name, false);
		this.hash = this.uuid.hashCode();
		this.player = Bukkit.getOfflinePlayer(this.uuid);
	}

	@Override
	public boolean equals(Object otherPlayer)
	{
		return otherPlayer instanceof MinepacksPlayerData && uuid.equals(((MinepacksPlayerData) otherPlayer).uuid);
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	@Override
	public @NotNull UUID getUUID()
	{
		return uuid;
	}

	@Override
	public @Nullable Player getPlayerOnline()
	{
		return Bukkit.getPlayer(getUUID());
	}

	@Override
	public @NotNull String getDisplayName()
	{
		Player bukkitPlayer = getPlayerOnline();
		return (bukkitPlayer != null) ? bukkitPlayer.getDisplayName() : ChatColor.GRAY + getName();
	}

	@Override
	public boolean hasPermission(final @NotNull String permission)
	{
		Player bukkitPlayer = getPlayerOnline();
		return bukkitPlayer != null && bukkitPlayer.hasPermission(permission);
	}

	@Override
	public boolean isOnline()
	{
		Player bukkitPlayer = getPlayerOnline();
		return bukkitPlayer != null && bukkitPlayer.isOnline();
	}

	@Override
	public long getLastPlayed()
	{
		return player.getLastPlayed();
	}

	@Override
	public boolean canBeUncached()
	{
		return !isOnline();
	}

	@Override
	public void setBackpackStyle(@NotNull String style)
	{
		if(style.equals(MagicValues.BACKPACK_STYLE_NAME_DISABLED))
		{
			backpackStyleName = style;
			backpackStyle = null;
		}
		else
		{
			if(style.equals(MagicValues.BACKPACK_STYLE_NAME_DEFAULT) || !BackpacksConfig.getInstance().getValidShortcutStyles().contains(style))
			{
				backpackStyleName = MagicValues.BACKPACK_STYLE_NAME_DEFAULT;
				style = BackpacksConfig.getInstance().getDefaultBackpackItem();
			}
			else backpackStyleName = style;
			backpackStyle = BackpacksConfig.getInstance().getItemConfig("Items." + style);
		}
		//TODO update database
	}

	@Override
	public @Nullable ItemStack getBackpackItem()
	{
		return (backpackStyle == null) ? null : backpackStyle.make();
	}

	@Override
	public void send(@NotNull IMessage message, @Nullable Object... args)
	{
		sendMessage(message, args);
	}

	@Override
	public void sendMessage(@NotNull IMessage message, @Nullable Object... args)
	{
		Player bukkitPlayer = getPlayerOnline();
		if(bukkitPlayer == null) return; // Is only null if the player is not online
		message.send(bukkitPlayer, args);
	}
}