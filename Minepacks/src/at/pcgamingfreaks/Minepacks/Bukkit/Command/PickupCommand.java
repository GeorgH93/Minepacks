package at.pcgamingfreaks.Minepacks.Bukkit.Command;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.ItemsCollector;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PickupCommand extends MinepacksCommand {

    private final Minepacks plugin;
    private final Message featureNotEnabled;

    private final Message toggleOn;
    private final Message toggleOff;

    public PickupCommand(Minepacks plugin) {
        super(plugin, "pickup", plugin.getLanguage().getTranslated("Commands.Description.Pickup"), Permissions.PICKUP_TOGGLE, true, plugin.getLanguage().getCommandAliases("Pickup"));

        this.plugin = plugin;
        featureNotEnabled       = plugin.getLanguage().getMessage("Ingame.Pickup.NotEnabled");
        toggleOn                = plugin.getLanguage().getMessage("Ingame.Pickup.ToggleOn");
        toggleOff                = plugin.getLanguage().getMessage("Ingame.Pickup.ToggleOff");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String s1, @NotNull String[] args) {
        Player player = (Player) sender;
        ItemsCollector collector = plugin.getItemsCollector();

        if (collector == null || !collector.isToggleable()) {
            featureNotEnabled.send(player);
            return;
        }

        boolean isEnabled = collector.toggleState(player.getUniqueId());

        if (isEnabled) {
            toggleOn.send(player);
            return;
        }

        toggleOff.send(player);
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String s1, @NotNull String[] strings) {
        return null;
    }
}
