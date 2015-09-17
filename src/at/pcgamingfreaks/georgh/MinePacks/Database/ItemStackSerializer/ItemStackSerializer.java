package at.pcgamingfreaks.georgh.MinePacks.Database.ItemStackSerializer;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer
{
	Base serializer, base = new Base();
	int usedVersion = 1;
	
	public ItemStackSerializer()
	{
		String name = Bukkit.getServer().getClass().getPackage().getName();
		String[] version = name.substring(name.lastIndexOf('.') + 2).split("_");
		try
		{
			if(version[0].equals("1"))
			{
				if(version[1].equals("8"))
				{
					serializer = new MC_1_8();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
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

	@SuppressWarnings("unused")
	public ItemStack[] deserialize(byte[] data)
	{
		return deserialize(data, usedVersion);
	}
	
	public ItemStack[] deserialize(byte[] data, int version)
	{
		switch(version)
		{
			case 0: return base.toItemStack(data);
			default: return serializer.toItemStack(data);
		}
	}
	
	public int getUsedVersion()
	{
		return usedVersion;
	}
}