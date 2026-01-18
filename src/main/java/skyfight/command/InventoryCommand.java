package skyfight.command;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.pythoncraft.gamelib.Chat;
import com.pythoncraft.gamelib.inventory.InventoryLayout;

import skyfight.PluginMain;

public class InventoryCommand implements CommandExecutor {
    public static HashMap<Player, String> creatingModePlayers = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {return true;}
        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage(Chat.c("\n§c§lInventory Command Help:"));
            player.sendMessage(Chat.c("  §f/inventory help §7- Show this help message"));
            player.sendMessage(Chat.c("  §f/inventory list §7- List all existing inventory managements"));
            player.sendMessage(Chat.c("  §f/inventory use <layout>§7 - Use an existing inventory management"));
            player.sendMessage(Chat.c("  §f/inventory reset §7- Reset your inventory management to the default state"));
            player.sendMessage(Chat.c("  §f/inventory new <name>§7 - Create a new inventory management with the given name"));
            player.sendMessage(Chat.c("  §f/inventory submit §7- Submit your current inventory management"));
            player.sendMessage(Chat.c("  §f/inventory exit §7- Exit the inventory management creation mode or trial mode"));
            player.sendMessage(Chat.c("  §f/inventory try <layout>§7 - Try an existing inventory management without saving it as your preferred one"));
            player.sendMessage(Chat.c("  §f/inventory remove <layout>§7 - Remove an existing inventory management"));

            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            player.sendMessage(Chat.c("\n§c§lExisting inventory layouts:"));
            player.sendMessage(Chat.c(" §7") + String.join(", ", InventoryLayout.layouts.keySet()));
            return true;
        }

        if (args[0].equalsIgnoreCase("use")) {
            if (args.length == 1) {return false;}

            String layoutName = args[1];
            if (!InventoryLayout.layouts.containsKey(layoutName)) {
                player.sendMessage(Chat.c("\n§c§lInventory layout \"" + layoutName + "\" not found."));
                return true;
            }

            PluginMain.getInstance().useLayout(player, layoutName);
            return true;
        }

        if (args[0].equalsIgnoreCase("try")) {
            if (args.length == 1) {
                player.sendMessage(Chat.c("§cUsage: /inventory try <inventory>"));
                return true;
            }

            String layoutName = args[1];
            if (!InventoryLayout.layouts.containsKey(layoutName)) {
                player.sendMessage(Chat.c("\n§c§lInventory layout \"" + layoutName + "\" not found."));
                return true;
            }

            PluginMain.getInstance().tryLayout(player, layoutName);
            return true;
        }

        if (args[0].equalsIgnoreCase("exit")) {
            exit(player);
        }

        if (args[0].equalsIgnoreCase("reset")) {
            PluginMain.getInstance().useLayout(player, "default");
            player.sendMessage(Chat.c("\n§a§lYour inventory layout has been reset to the default state."));
            return true;
        }

        if (args[0].equalsIgnoreCase("new")) {
            if (args.length == 1) {
                player.sendMessage(Chat.c("§cUsage: /inventory new <name>"));
                return true;
            }
            
            String layoutName = args[1];
            if (InventoryLayout.layouts.containsKey(layoutName)) {
                player.sendMessage(Chat.c("\n§c§lInventory layout \"" + layoutName + "\" already exists."));
                return true;
            }

            creatingModePlayers.put(player, layoutName);

            player.sendMessage(Chat.c("\n§a§lHow to create a new inventory layout:"));
            player.sendMessage(Chat.c("1. §7Order the items in your inventory as you want them to be saved. Try not to throw some items out of your inventory. The concrete blocks will be replaced with some items from your chosen kit. The items from the kit will be filled in this order: §c█§6█§e█§a█§2█§3█§b█§1█§9█§5█§d█§f█§7█§0█"));
            player.sendMessage(Chat.c("2. §7Use §f/inventory submit§7 to save your inventory layout or §f/inventory exit§7 to discard it."));
            PluginMain.getInstance().tryLayout(player, "default");

            return true;
        }

        if (args[0].equalsIgnoreCase("submit")) {
            if (!creatingModePlayers.containsKey(player)) {
                player.sendMessage(Chat.c("\n§c§lYou are not in the inventory layout creation mode."));
                return true;
            }

            String layoutName = creatingModePlayers.get(player);

            HashMap<Integer, String> items = new HashMap<>();
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                if (player.getInventory().getItem(i) != null) {
                    items.put(i, player.getInventory().getItem(i).getType().name());
                }
            }

            PluginMain.getInstance().createLayout(player, items, layoutName);
            creatingModePlayers.remove(player);
            exit(player);

            PluginMain.getInstance().useLayout(player, layoutName);
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (!player.hasPermission("skyfight.inventory.remove")) {
                player.sendMessage(Chat.c("\n§c§lYou do not have permission to remove inventory layouts."));
                return true;
            }
            
            if (args.length == 1) {
                player.sendMessage(Chat.c("§cUsage: /inventory remove <name>"));
                return true;
            }

            String layoutName = args[1];
            if (!InventoryLayout.layouts.containsKey(layoutName)) {
                player.sendMessage(Chat.c("\n§c§lInventory layout \"" + layoutName + "\" not found."));
                return true;
            }

            PluginMain.getInstance().removeLayout(layoutName);
            return true;
        }

        if (args[0].equalsIgnoreCase("players")) {
            if (!player.hasPermission("skyfight.inventory.players")) {
                player.sendMessage(Chat.c("\n§c§lYou do not have permission to view players' inventory layouts."));
                return true;
            }

            player.sendMessage(Chat.c("\n§c§lPlayer inventory layouts:"));
            for (String playerName : InventoryLayout.playerLayouts.keySet()) {
                String layoutName = InventoryLayout.playerLayouts.get(playerName);
                player.sendMessage(Chat.c(" §7" + playerName + " §f- §a" + layoutName));
            }
            return true;
        }

        return false;
    }

    public static void exit(Player player) {
        if (creatingModePlayers.containsKey(player)) {
            creatingModePlayers.remove(player);
        }

        Inventory i = player.getInventory();
        i.clear();
        i.setItem(0, PluginMain.getMenuItem());
        player.clearActivePotionEffects();
    }
}