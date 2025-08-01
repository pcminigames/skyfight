package skyfight;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import skyfight.lib.Pair;

public class ItemTemplate {
    public List<Pair<ItemStack, Predicate<Player>>> items = new ArrayList<>();

    public ItemTemplate(List<Pair<ItemStack, Predicate<Player>>> items) {
        this.items = items;
    }

    public ItemStack getNumbered(int number) {
        if (number < 0 || number >= items.size()) {
            return new ItemStack(Material.AIR);
        }

        return items.get(number).get1();
    }

    public ItemStack get(Player player) {
        for (Pair<ItemStack, Predicate<Player>> pair : this.items) {
            if (pair.get2().test(player)) {
                return pair.get1();
            }
        }

        return new ItemStack(Material.AIR);
    }
}