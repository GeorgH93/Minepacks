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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.base.Charsets;

public class UUIDConverter
{
	public static String getNameFromUUID(String uuid)
	{
		String name = null;
		try
		{
			URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replaceAll("-", ""));
			Scanner jsonScanner = new Scanner(url.openConnection().getInputStream(), "UTF-8");
			String json = jsonScanner.next();
			name = (((JSONObject)new JSONParser().parse(json)).get("name")).toString();
			jsonScanner.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return name;
	}
	
	public static String getUUIDFromName(String name, boolean onlinemode)
	{
		return getUUIDFromName(name, onlinemode, false);
	}

	public static String getUUIDFromName(String name, boolean onlinemode, boolean withSeperators)
	{
		String uuid = null;
		if(onlinemode)
		{
			try
	    	{
	    		BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
	    		uuid = (((JSONObject)new JSONParser().parse(in)).get("id")).toString().replaceAll("\"", "");
	    		in.close();
	    	}
	    	catch (Exception e)
	    	{
	    		e.printStackTrace();
	    		uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)).toString();
	    	}
		}
		else
		{
			uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)).toString();
		}
		if(uuid != null)
		{
			if(withSeperators)
    		{
    			if(!uuid.contains("-"))
    			{
    				return uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
    			}
    		}
    		else
    		{
    			uuid = uuid.replaceAll("-", "");
    		}
		}
		return uuid;
	}
}