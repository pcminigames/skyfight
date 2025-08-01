package skyfight;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Kit {
    public String name;
    public Material material;
    public HashMap<String, ItemTemplate> items = new HashMap<>();

    public Kit(String name, Material material, HashMap<String, ItemTemplate> items) {
        this.name = name;
        this.material = material;
        this.items = items;
    }

    public void give(Player player) {
        HashMap<String, Integer> inventoryOrder = PluginMain.inventoryOrders.get(PluginMain.savedInventories.getOrDefault(player.getName(), "default"));

        for (String slotName : items.keySet()) {
            int slot = inventoryOrder.getOrDefault(slotName, 0);
            ItemTemplate item = items.get(slotName);
            if (item == null) {continue;}

            player.getInventory().setItem(slot, item.get(player).clone());
        }
    }

    public void give(Player player, HashMap<String, Integer> inventory) {
        for (String slotName : items.keySet()) {
            int slot = inventory.getOrDefault(slotName, 0);
            ItemTemplate item = items.get(slotName);
            if (item == null) {continue;}

            player.getInventory().setItem(slot, item.get(player).clone());
        }
    }
}
