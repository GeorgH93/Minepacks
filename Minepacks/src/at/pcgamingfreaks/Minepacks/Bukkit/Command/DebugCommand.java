/*
 *   Copyright (C) 2024 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Command;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.MessageBuilder;
import at.pcgamingfreaks.Bukkit.Util.InventoryUtils;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.Message.MessageClickEvent;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Message.MessageFormat;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DebugCommand extends MinepacksCommand
{
	private final Message messageDone, messageStart;
	private BufferedWriter writer = null;

	public DebugCommand(final @NotNull Minepacks plugin)
	{
		super(plugin, "debug", "Just for debug reasons", Permissions.RELOAD, true);
		MessageBuilder builder = new MessageBuilder("Please do not interact with your game for the next minute!", MessageColor.GOLD);
		builder.appendNewLine().append("The plugin will now collect data about your server and plugins.").appendNewLine();
		builder.append("This will involve opening inventory's and your backpack.").appendNewLine();
		builder.append("Please do not interact with your game till this is over!", MessageColor.RED, MessageFormat.BOLD);
		messageStart = builder.getMessage();

		builder = new MessageBuilder("All data has been collected!", MessageColor.GREEN, MessageFormat.BOLD).appendNewLine();
		builder.append("You can now interact with your game again.").appendNewLine();
		builder.append("The collected data can be found in your plugins directory inside the 'debug.txt' file.").appendNewLine();
		builder.append("Please upload this file to ");
		builder.append("https://pastebin.com/", MessageColor.YELLOW, MessageFormat.UNDERLINE).onClick(MessageClickEvent.ClickEventAction.OPEN_URL, "https://pastebin.com/");
		builder.append(" and send the link to the developer.");
		messageDone = builder.getMessage();
	}

	@SneakyThrows
	private void debugSystem(final @NotNull CommandSender commandSender)
	{
		final Player sender = (Player) commandSender;
		messageStart.send(sender);

		File debugFile = new File(plugin.getDataFolder(), "debug.txt");
		if(debugFile.exists() && !debugFile.delete())
		{
			plugin.getLogger().warning("Unable to delete debug.txt file!");
		}
		writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(debugFile.toPath()), StandardCharsets.UTF_8));

		writer.append(plugin.getDescription().getName()).append(" Version: ").append(plugin.getDescription().getVersion());
		writer.append("\nServer: ").append(Bukkit.getServer().getBukkitVersion()).append(" (").append(Bukkit.getServer().getVersion()).append(")");
		writer.append("\nJava: ").append(System.getProperty("java.version"));
		writer.append("\nProxy: ").append(Utils.detectBungeeCord() ? "Bungee" : (Utils.detectVelocity() ? "Velocity" : "None"));
		writer.append("\nOnline Mode: ").append(String.valueOf(Bukkit.getServer().getOnlineMode())).append(" ; Proxy Online Mode: ").append(String.valueOf(Utils.getBungeeOrVelocityOnlineMode()));
		writer.append("\n\nPlugins:\n");
		for(Plugin p : Bukkit.getServer().getPluginManager().getPlugins())
		{
			writer.append(p.getName()).append(' ').append(p.getDescription().getVersion()).append('\n');
		}
		writer.append("\nPlugin Config:\n");
		try(BufferedReader configReader = new BufferedReader(new InputStreamReader(Files.newInputStream(new File(plugin.getDataFolder(), "config.yml").toPath()), StandardCharsets.UTF_8)))
		{
			String line;
			while((line = configReader.readLine()) != null)
			{
				if(line.isEmpty()) continue;
				if(line.contains("Host") || line.contains("Password") || line.contains("User")) line = line.replaceAll("^(\\s+\\w+):.*$", "$1: ********");
				writer.append(line).append('\n');
			}
		}
		writer.append("\n\n\nSelf-test results:\n");

		ItemStack slot = sender.getInventory().getItem(0);
		if(slot == null || slot.getAmount() == 0 || slot.getType() == Material.AIR) sender.getInventory().setItem(0, new ItemStack(Material.ACACIA_BOAT));

		Minepacks.getScheduler().runAtEntityLater(sender, () -> sender.performCommand("backpack"), 5*20L);
		Minepacks.getScheduler().runAtEntityLater(sender, () -> Bukkit.getPluginManager().callEvent(new ClickEvent(sender.getOpenInventory(), InventoryType.SlotType.QUICKBAR, InventoryUtils.getPlayerTopInventory(sender).getSize() + 27, ClickType.LEFT, InventoryAction.PICKUP_ALL)), 10*20L);
		Minepacks.getScheduler().runAtEntityLater(sender, sender::closeInventory, 20*20L);
		Minepacks.getScheduler().runLater(() -> {
			try
			{
				writer.flush();
				writer.close();
				writer = null;
			}
			catch(Exception e) { plugin.getLogger().log(Level.SEVERE, "Error while writing debug file.", e);}
			sender.getInventory().setItem(0, slot);
			messageDone.send(sender);
		}, 30*20L);
	}

	@SneakyThrows
	@Override
	public void execute(@NotNull CommandSender commandSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if(writer != null) return;
		if (args.length == 2 && args[0].equals("permissions"))
		{
			Player player = Bukkit.getServer().getPlayer(args[1]);
			if (player == null)
			{
				commandSender.sendMessage("Player " + args[1] + " is offline.");
				return;
			}
			commandSender.sendMessage("### Permissions for " + player.getName() + " ###");
			for(String perm : Permissions.getPermissions())
			{
				commandSender.sendMessage(perm + ": " + player.hasPermission(perm));
			}
			commandSender.sendMessage("###############################");
		}
		else if (args.length == 2 && args[0].equals("size"))
		{
			executeSize(commandSender, args[1]);
		}
		else
		{
			debugSystem(commandSender);
		}
	}

	void executeSize(final @NotNull CommandSender commandSender, final @NotNull String playerName)
	{
		Player player = Bukkit.getServer().getPlayer(playerName);
		if (player == null)
		{
			commandSender.sendMessage("Player " + playerName + " is offline.");
			return;
		}
		Backpack bp = Minepacks.getInstance().getBackpackCachedOnly(player);
		int bpSize = -1, bpInvSize = -1, sizeShouldBe = Minepacks.getInstance().getBackpackPermSize(player);
		String actualSize = "backpack not loaded", actualSizeInventory = "backpack not loaded";
		if (bp != null)
		{
			bpSize = bp.getSize();
			bpInvSize = bp.getInventory().getSize();
			actualSize = String.valueOf(bpSize);
			actualSizeInventory = String.valueOf(bpInvSize);
		}
		commandSender.sendMessage("### Backpack size for " + player.getName() + " ###");
		commandSender.sendMessage("Size: " + actualSize);
		commandSender.sendMessage("Inventory Size: " + actualSizeInventory);
		commandSender.sendMessage("Should be: " + sizeShouldBe);
		if (bpSize != sizeShouldBe && bp != null)
		{
			commandSender.sendMessage("Size mismatch detected, attempt resize ...");
			((at.pcgamingfreaks.Minepacks.Bukkit.Backpack) bp).checkResize();
			if (bp.getSize() != sizeShouldBe)
			{
				commandSender.sendMessage("Failed to resize backpack.");
			}
			else
			{
				commandSender.sendMessage("Resized backpack successfully.");
			}
		}
		if (bp != null && bp.getSize() != bp.getInventory().getSize())
		{
			commandSender.sendMessage("Inventory size does not match backpack size!");
		}
		commandSender.sendMessage("Player class: " + player.getClass().getName());
		commandSender.sendMessage("###############################");
	}

	@Override
	public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String mainCommandAlias, @NotNull String alias, @NotNull String[] args)
	{
		if (args.length == 1)
		{
			List<String> completeList = new ArrayList<>(3);
			if ("size".startsWith(args[0])) { completeList.add("size"); }
			if ("system".startsWith(args[0])) { completeList.add("system"); }
			if ("permissions".startsWith(args[0])) { completeList.add("permissions"); }
			return completeList;
		}
		else if (args.length == 2)
		{
			return Utils.getPlayerNamesStartingWith(args[args.length - 1], null);
		}
		return null;
	}

	@Override
	public @Nullable List<HelpData> getHelp(@NotNull CommandSender requester)
	{
		return null;
	}

	private class ClickEvent extends InventoryClickEvent
	{
		public ClickEvent(@NotNull InventoryView view, InventoryType.@NotNull SlotType type, int slot, @NotNull ClickType click, @NotNull InventoryAction action)
		{
			super(view, type, slot, click, action);
		}

		@SneakyThrows
		@Override
		public void setCancelled(boolean toCancel)
		{
			super.setCancelled(toCancel);
			writer.append(ExceptionUtils.getStackTrace(new Exception("Click event has been canceled!!!")));
			writer.append("\n\n");
		}
	}
}