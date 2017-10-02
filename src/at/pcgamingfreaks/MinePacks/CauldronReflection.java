package at.pcgamingfreaks.MinePacks;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CauldronReflection
{
	private static final String bukkitVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
	/**
	 * Map of mc-dev simple class name to fully qualified Forge class name.
	 */
	private static Map<String, String> ForgeClassMappings;
	/**
	 * Map of Forge fully qualified class names to a map from mc-dev field names to Forge field names.
	 */
	private static Map<String, Map<String, String>> ForgeFieldMappings;

	/**
	 * Map of Forge fully qualified class names to a map from mc-dev method names to a map from method signatures to Forge method
	 * names.
	 */
	private static Map<String, Map<String, Map<String, String>>> ForgeMethodMappings;
	private static final boolean isForge = Bukkit.getServer().getName().toLowerCase().contains("cauldron");
	private static Map<Class<?>, String> primitiveTypes;

	static
	{
		final String nameseg_class = "a-zA-Z0-9$_";
		final String fqn_class = nameseg_class + "/";

		primitiveTypes = ImmutableMap.<Class<?>, String>builder().put(boolean.class, "Z").put(byte.class, "B").put(char.class, "C").put(short.class, "S").put(int.class, "I").put(long.class, "J").put(float.class, "F").put(double.class, "D").put(void.class, "V").build();

		if(isForge)
		{
			// Initialize the maps by reading the srg file
			ForgeClassMappings = new HashMap<>();
			ForgeFieldMappings = new HashMap<>();
			ForgeMethodMappings = new HashMap<>();
			try
			{
				InputStream stream = Class.forName("net.minecraftforge.common.MinecraftForge").getClassLoader().getResourceAsStream("mappings/" + bukkitVersion + "/cb2numpkg.srg");
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

				// 1: cb-simpleName
				// 2: forge-fullName (Needs dir2fqn())
				Pattern classPattern = Pattern.compile("^CL: net/minecraft/server/([" + nameseg_class + "]+) ([" + fqn_class + "]+)$");
				// 1: cb-simpleName
				// 2: cb-fieldName
				// 3: forge-fullName (Needs dir2fqn())
				// 4: forge-fieldName
				Pattern fieldPattern = Pattern.compile("^FD: net/minecraft/server/([" + nameseg_class + "]+)/([" + nameseg_class + "]+) ([" + fqn_class + "]+)/([" + nameseg_class + "]+)$");
				// 1: cb-simpleName
				// 2: cb-methodName
				// 3: cb-signature-args
				// 4: cb-signature-ret
				// 5: forge-fullName (Needs dir2fqn())
				// 6: forge-methodName
				// 7: forge-signature-args
				// 8: forge-signature-ret
				Pattern methodPattern = Pattern.compile("^MD: net/minecraft/server/([" + fqn_class + "]+)/([" + nameseg_class + "]+) \\(([;\\[" + fqn_class + "]*)\\)([;\\[" + fqn_class + "]+) " + "([" + fqn_class + "]+)/([" + nameseg_class + "]+) \\(([;\\[" + fqn_class + "]*)\\)([;\\[" + fqn_class + "]+)$");

				String line;
				while((line = reader.readLine()) != null)
				{
					Matcher classMatcher = classPattern.matcher(line);
					if(classMatcher.matches())
					{
						// by CB class name
						ForgeClassMappings.put(classMatcher.group(1), classMatcher.group(2).replaceAll("/", "."));
						continue;
					}
					Matcher fieldMatcher = fieldPattern.matcher(line);
					if(fieldMatcher.matches())
					{
						// by CB class name
						Map<String, String> innerMap = ForgeFieldMappings.get(fieldMatcher.group(3).replaceAll("/", "."));
						if(innerMap == null)
						{
							innerMap = new HashMap<>();
							ForgeFieldMappings.put(fieldMatcher.group(3).replaceAll("/", "."), innerMap);
						}
						// by CB field name to Forge field name
						innerMap.put(fieldMatcher.group(2), fieldMatcher.group(4));
						continue;
					}
					Matcher methodMatcher = methodPattern.matcher(line);
					if(methodMatcher.matches())
					{
						// get by CB class name
						Map<String, Map<String, String>> middleMap = ForgeMethodMappings.get(methodMatcher.group(5).replaceAll("/", "."));
						if(middleMap == null)
						{
							middleMap = new HashMap<>();
							ForgeMethodMappings.put(methodMatcher.group(5).replaceAll("/", "."), middleMap);
						}
						// get by CB method name
						Map<String, String> innerMap = middleMap.get(methodMatcher.group(2));
						if(innerMap == null)
						{
							innerMap = new HashMap<>();
							middleMap.put(methodMatcher.group(2), innerMap);
						}
						// store the parameter strings
						innerMap.put(methodMatcher.group(3), methodMatcher.group(6));
						innerMap.put(methodMatcher.group(7), methodMatcher.group(6));
					}
				}
			}
			catch(ClassNotFoundException | IOException e)
			{
				e.printStackTrace();
				System.err.println("Warning: Running on Cauldron server, but couldn't load mappings file.");
			}
		}
	}

	public static String getVersion()
	{
		return bukkitVersion;
	}

	public static Class<?> getNMSClass(String className)
	{
		try
		{
			if(isForge)
			{
				String forgeName = ForgeClassMappings.get(className);
				if(forgeName != null)
				{
					try
					{
						return Class.forName(forgeName);
					}
					catch(ClassNotFoundException ignored)
					{
					}
				}
				else
				{
					throw new RuntimeException("Missing Forge mapping for " + className);
				}
			}
			else
			{
				return Class.forName("net.minecraft.server." + bukkitVersion + "." + className);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static Object getHandle(Object obj)
	{
		try
		{
			//noinspection ConstantConditions
			return getMethod(obj.getClass(), "getHandle").invoke(obj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... args)
	{
		for(Method m : clazz.getMethods())
		{
			if(m.getName().equals(name) && (args.length == 0 || classListEqual(args, m.getParameterTypes())))
			{
				m.setAccessible(true);
				return m;
			}
		}
		return null;
	}

	public static boolean classListEqual(Class<?>[] l1, Class<?>[] l2)
	{
		boolean equal = true;
		if(l1.length != l2.length)
		{
			return false;
		}
		for(int i = 0; i < l1.length; i++)
		{
			if(l1[i] != l2[i])
			{
				equal = false;
				break;
			}
		}
		return equal;
	}

	public static Field getNMSField(Class clazz, String fieldName)
	{
		if(isForge)
		{
			try
			{
				return clazz.getField(ForgeFieldMappings.get(clazz.getName()).get(fieldName));
			}
			catch(NoSuchFieldException ex)
			{
				ex.printStackTrace();
			}
			catch(NullPointerException ignored) {}
		}
		try
		{
			return clazz.getField(fieldName);
		}
		catch(NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static Field getNMSField(String className, String fieldName)
	{
		return getNMSField(getNMSClass(className), fieldName);
	}

	public static Method getNMSMethod(Class<?> clazz, String methodName, Class<?>... parameters)
	{
		if(isForge)
		{
			try
			{
				Map<String, String> innerMap = ForgeMethodMappings.get(clazz.getName()).get(methodName);
				StringBuilder sb = new StringBuilder();
				for(Class<?> cl : parameters)
				{
					sb.append(methodSignaturePart(cl));
				}
				return clazz.getMethod(innerMap.get(sb.toString()), parameters);
			}
			catch(NoSuchMethodException e)
			{
				e.printStackTrace();
			}
			catch(NullPointerException ignored)
			{
			}
		}
		try
		{
			return clazz.getMethod(methodName, parameters);
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private static String methodSignaturePart(Class<?> param)
	{
		if(param.isArray())
		{
			return "[" + methodSignaturePart(param.getComponentType());
		}
		else if(param.isPrimitive())
		{
			return primitiveTypes.get(param);
		}
		else
		{
			return "L" + param.getName().replaceAll("\\.", "/") + ";";
		}
	}
}