package at.pcgamingfreaks.georgh.MinePacks.Database.Serializer;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer
{
	Base serializer, base;
	int usedVersion = 1;
	
	public ItemStackSerializer()
	{
		String name = Bukkit.getServer().getClass().getPackage().getName();
		String[] version = name.substring(name.lastIndexOf('.') + 2).split("_");
		base = new Base();
		try
		{
			if(version[0].equals("1"))
			{
				if(version[1].equals("8"))
				{
					/*if(version[2].equals("R1"))
					{
						serializer = new MC_1_8_R1();
					}
					else if(version[2].equals("R2"))
					{
						serializer = new MC_1_8_R2();
					}
					else if(version[2].equals("R3"))
					{
						serializer = new MC_1_8_R3();
					}*/
					serializer = new MC_1_8();
				}
			}
		}
		catch(Exception e) {}
		if(serializer == null)
		{
			usedVersion = 0;
			serializer = base;
		}
	}
	
	public byte[] serialize(Inventory inv)
	{
		return serializer.toByteArray(inv);
	}
	
	public ItemStack[] deserialize(byte[] data)
	{
		return serializer.toItemStack(data);
	}
	
	public ItemStack[] deserialize(byte[] data, int version)
	{
		if(version == 0)
		{
			return base.toItemStack(data);
		}
		return serializer.toItemStack(data);
	}
	
	public int getUsedVersion()
	{
		return usedVersion;
	}
}