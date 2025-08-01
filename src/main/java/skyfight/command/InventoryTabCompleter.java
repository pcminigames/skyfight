package skyfight.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import skyfight.PluginMain;

public class InventoryTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1 -> {
                return Arrays.asList("help", "list", "use", "reset", "new", "submit", "try", "exit", "remove");
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("use") || args[0].equalsIgnoreCase("try") || args[0].equalsIgnoreCase("remove")) {
                    return PluginMain.inventoryOrders.keySet().stream().toList();
                }
            }
            default -> {
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    public static List<String> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

}