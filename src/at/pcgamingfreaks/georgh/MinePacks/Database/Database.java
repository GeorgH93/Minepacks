/*
 *   Copyright (C) 2014-2015 GeorgH93
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

package at.pcgamingfreaks.georgh.MinePacks.Database;

import java.util.HashSet;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import at.pcgamingfreaks.georgh.MinePacks.Backpack;
import at.pcgamingfreaks.georgh.MinePacks.MinePacks;
import at.pcgamingfreaks.georgh.MinePacks.Database.ItemStackSerializer.ItemStackSerializer;

public class Database
{
	protected MinePacks plugin;
	
	protected boolean UseUUIDs, UseUUIDSeparators;
	protected long maxAge;
	
	private HashSet<Backpack> backpacks = new HashSet<>();
	protected ItemStackSerializer itsSerializer = new ItemStackSerializer();
	
	public Database(MinePacks mp)
	{
		plugin = mp;
		UseUUIDSeparators	= plugin.config.getUseUUIDSeparators();
		UseUUIDs			= plugin.config.getUseUUIDs();
		maxAge				= plugin.config.getAutoCleanupMaxInactiveDays();
	}
	
	public void close() { }
	
	protected String getPlayerNameOrUUID(OfflinePlayer player)
	{
		if(UseUUIDs)
		{
			if(UseUUIDSeparators)
			{
				return player.getUniqueId().toString();
			}
			else
			{
				return player.getUniqueId().toString().replace("-", "");
			}
		}
		else
		{
			return player.getName();
		}
	}
	
	public static Database getDatabase(MinePacks Plugin)
	{
		switch(Plugin.config.getDatabaseType().toLowerCase())
		{
			case "mysql": return new MySQL(Plugin);
			case "flat":
			case "file":
			case "files": return new Files(Plugin);
			case "sqlite":
			default: return new SQLite(Plugin);
		}
	}
	
	private Backpack findBackpack(OfflinePlayer player)
	{
		for(Backpack bp : backpacks)
		{
			if(bp.getOwner().equals(player))
			{
				return bp;
			}
		}
		return null;
	}
	
	public Backpack getBackpack(String title)
	{
		for(Backpack bp : backpacks)
		{
			if(bp.getTitle().equals(title))
			{
				return bp;
			}
		}
		return null;
	}
	
	public Backpack getBackpack(OfflinePlayer player, boolean loadedOnly)
	{
		Backpack lbp = findBackpack(player);
		if(lbp == null && !loadedOnly)
		{
			lbp = loadBackpack(player);
			if(lbp == null)
			{
				lbp = new Backpack(player);
			}
			backpacks.add(lbp);
		}
		return lbp;
	}
	
	public void UnloadBackpack(Backpack backpack)
	{
		backpacks.remove(backpack);
	}
	
	// DB Functions
	
	public void updatePlayer(Player player) {}
	
	public void saveBackpack(Backpack backpack) {}
	
	public Backpack loadBackpack(OfflinePlayer player) { return null; }
}