/*
 *   Copyright (C) 2014 GeorgH93
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

public class Database
{
	protected MinePacks plugin;
	
	public HashSet<Backpack> backpacks = new HashSet<Backpack>();
	
	public Database(MinePacks mp)
	{
		plugin = mp;
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
	
	public Backpack getBackpack(OfflinePlayer player)
	{
		Backpack lbp = findBackpack(player);
		if(lbp == null)
		{
			lbp = LoadBackpack(player);
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
	
	public void UpdatePlayer(Player player) {}
	
	public void SaveBackpack(Backpack backpack) {}
	
	public Backpack LoadBackpack(OfflinePlayer player) { return null; }
}