package skyfight;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.MultimapBuilder;

import skyfight.command.InventoryCommand;
import skyfight.command.InventoryTabCompleter;
import com.pythoncraft.gamelib.gui.GUIClickEvent;
import com.pythoncraft.gamelib.gui.GUIIdentifier;
import com.pythoncraft.gamelib.gui.GUIManager;
import com.pythoncraft.gamelib.inventory.Kit;
import com.pythoncraft.gamelib.inventory.order.InventoryOrder;
import com.pythoncraft.gamelib.inventory.ItemLoader;
import com.pythoncraft.gamelib.inventory.ItemTemplate;
import com.pythoncraft.gamelib.BlockFill;
import com.pythoncraft.gamelib.Chat;
import com.pythoncraft.gamelib.GameLib;
import com.pythoncraft.gamelib.Logger;


public class PluginMain extends JavaPlugin implements Listener {
    
    static PluginMain instance;
    public static PluginMain getInstance() { return instance; }

    private File configFile;
    private FileConfiguration config;
    public File kitFile;
    public FileConfiguration kitConfig;

    public static List<Kit> kits = new ArrayList<>();
    public static Kit defaultKit = new Kit("default", Material.STICK, new HashMap<>());
    public static Kit showcaseKit = new Kit("showcase", Material.STICK, new HashMap<>());

    public static List<BlockFill> arenaFills = new ArrayList<>();
    public static World world;

    public static ScoreboardManager sm;
    public static Scoreboard scoreboard;

    public static Team redTeam;
    public static Team yellowTeam;
    public static List<Team> teams;

    public static Location redSpawn;
    public static Location yellowSpawn;
    public static List<Location> spawnPoints;
    public static Location lobby;
    public static Location spectatorSpawn;

    public static boolean game = false;
    public static HashSet<Player> players = new HashSet<>();

    public static HashMap<String, String> slotNames = new HashMap<String, String>() {{
        put("iron_axe", "axe");
        put("iron_pickaxe", "pickaxe");
        put("iron_sword", "sword");
        put("diamond_helmet", "head");
        put("diamond_chestplate", "chest");
        put("diamond_leggings", "legs");
        put("diamond_boots", "feet");
        put("shield", "shield");
        put("stick", "bow");
        put("arrow", "arrow");
        put("flint_and_steel", "flint");
        put("bucket", "bucket");
        put("water_bucket", "water");
        put("snowball", "pearl");
        put("golden_apple", "gapple");
        put("cooked_porkchop", "pork");
        put("baked_potato", "potato");

        put("smooth_stone", "stone1");
        put("stone", "stone2");
        put("cobblestone", "stone3");
        put("oak_planks", "planks1");
        put("spruce_planks", "planks2");
        put("birch_planks", "planks3");
        put("jungle_planks", "planks4");
        put("oak_log", "logs1");
        put("spruce_log", "logs2");
        put("jungle_log", "logs3");

        put("red_concrete", "k1");
        put("orange_concrete", "k2");
        put("yellow_concrete", "k3");
        put("lime_concrete", "k4");
        put("green_concrete", "k5");
        put("cyan_concrete", "k6");
        put("light_blue_concrete", "k7");
        put("blue_concrete", "k8");
        put("purple_concrete", "k9");
        put("magenta_concrete", "k10");
        put("pink_concrete", "k11");
        put("white_concrete", "k12");
        put("gray_concrete", "k13");
        put("black_concrete", "k14");
    }};

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(GUIManager.getInstance(), this);

        this.configFile = new File(getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        this.kitFile = new File(getDataFolder(), "kits.yml");
        this.kitConfig = YamlConfiguration.loadConfiguration(this.kitFile);

        world = Bukkit.getWorld("world");

        try {config.save(this.configFile);} catch (IOException e) {e.printStackTrace();}
        try {kitConfig.save(this.kitFile);} catch (IOException e) {e.printStackTrace();}

        sm = Bukkit.getScoreboardManager();
        scoreboard = sm.getMainScoreboard();

        redTeam = GameLib.createTeam("red", "RED", ChatColor.RED);
        yellowTeam = GameLib.createTeam("yellow", "YELLOW", ChatColor.YELLOW);

        teams = List.of(redTeam, yellowTeam);

        redSpawn    = new Location(Bukkit.getWorld("world"), -26, 0, 0, -90, 0).add(0.5, 0, 0.5);
        yellowSpawn = new Location(Bukkit.getWorld("world"),  26, 0, 0, +90, 0).add(0.5, 0, 0.5);
        spawnPoints = List.of(redSpawn, yellowSpawn);
        lobby = new Location(Bukkit.getWorld("world"), 0, 122, 0).add(0.5, 0, 0.5);
        spectatorSpawn = new Location(Bukkit.getWorld("world"), 0, 60, 0).add(0.5, 0, 0.5);

        this.loadConfig();

        GUIManager.getInstance().register("team", true, guiPlayer -> {
            Inventory inventory = Bukkit.createInventory(new GUIIdentifier("team"), 27, Chat.c("§lSelect Team"));

            ItemStack red = new ItemStack(Material.RED_WOOL);
            ItemMeta redMeta = red.getItemMeta();
            redMeta.setDisplayName(Chat.c("§c§lRed Team"));
            red.setItemMeta(redMeta);

            ItemStack yellow = new ItemStack(Material.YELLOW_WOOL);
            ItemMeta yellowMeta = yellow.getItemMeta();
            yellowMeta.setDisplayName(Chat.c("§e§lYellow Team"));
            yellow.setItemMeta(yellowMeta);

            inventory.setItem(11, red);
            inventory.setItem(15, yellow);

            return inventory;
        });

        GUIManager.getInstance().register("kit", true, guiPlayer -> {
            Inventory inventory = Bukkit.createInventory(new GUIIdentifier("kit"), 54, Chat.c("§lSelect Kit"));

            int slot = 0;
            for (Kit kit : kits) {
                if ("default".equals(kit.displayName)) {continue;}
                
                Material kitMaterial = kit.material != null ? kit.material : Material.STICK;
                ItemStack item = new ItemStack(kitMaterial);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(kit.displayName);
                    meta.setAttributeModifiers(MultimapBuilder.hashKeys().hashSetValues().build());
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    item.setItemMeta(meta);
                }

                inventory.setItem(slot, item);
                slot += 1;
            }

            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta barrierMeta = barrier.getItemMeta();
            barrierMeta.setDisplayName(Chat.c("§c§lBack to team selection"));
            barrier.setItemMeta(barrierMeta);
            inventory.setItem(53, barrier);

            return inventory;
        });

        this.getCommand("inventory").setTabCompleter(new InventoryTabCompleter());
		this.getCommand("inventory").setExecutor(new InventoryCommand());
    }

    @Override
    public void onDisable() {}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("kit")) {
            Player player = (Player) sender;

            if (redTeam.hasEntity(player) || yellowTeam.hasEntity(player)) {
                GUIManager.getInstance().open("kit", player);
            } else {
                GUIManager.getInstance().open("team", player);
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("cleararena")) {
            clearArena();
            sender.sendMessage("§aArena cleared!");
            return true;
        }
        
        return false;
    }

    @EventHandler
	public void $onGuiClick(GUIClickEvent guiClickEvent) {
        InventoryClickEvent inventoryClickEvent = guiClickEvent.getInventoryClickEvent();
        int slot = inventoryClickEvent.getSlot();
        Player player = (Player) inventoryClickEvent.getWhoClicked();

		if (guiClickEvent.getID().equals("team")) {
            if (slot == 11) {
                if (!redTeam.hasEntity(player)) {redTeam.addEntity(player);}
            } else if (slot == 15) {
                if (!yellowTeam.hasEntity(player)) {yellowTeam.addEntity(player);}
            }

            if (slot == 11 || slot == 15) {
                player.closeInventory();
                GUIManager.getInstance().open("kit", player);
            }
        }

        if (guiClickEvent.getID().equals("kit")) {
            if (slot < kits.size()) {
                Team team = scoreboard.getEntityTeam(player);
                if (team == null) {
                    player.sendMessage("You must select a team first!");
                    player.closeInventory();
                    GUIManager.getInstance().open("team", player);
                    return;
                }
                playerStart(player, kits.get(slot));
            } else if (slot == 53) {
                player.closeInventory();
                GUIManager.getInstance().open("team", player);
            }
        }
    }

    @EventHandler
    public void $onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.HEART_OF_THE_SEA) {
            if (redTeam.hasEntity(player) || yellowTeam.hasEntity(player)) {
                GUIManager.getInstance().open("kit", player);
            } else {
                GUIManager.getInstance().open("team", player);
            }
        }
    }

    @EventHandler
    public void $onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {return;}

        if (!game) {event.setCancelled(true);}
    }

    @EventHandler
    public void $onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (players.contains(player)) {players.remove(player);}

        boolean hasYellow = false;
        boolean hasRed = false;

        for (Player p : players) {
            if (yellowTeam.hasEntity(p)) {hasYellow = true;}
            if (redTeam.hasEntity(p)) {hasRed = true;}
        }

        // Needed to pass these variables to the Bukkit scheduler function
        boolean hasYellowFinal = hasYellow;
        boolean hasRedFinal = hasRed;

        if (!hasYellow || !hasRed) {
            // End the game if one team is eliminated
            game = false;

            Bukkit.getScheduler().runTask(this, () -> {
                Bukkit.broadcastMessage(Chat.c("§lGame Over!"));
                if (hasRedFinal && !hasYellowFinal) {
                    Bukkit.broadcastMessage(Chat.c("§c§lRed Team§r wins!"));
                } else if (hasYellowFinal && !hasRedFinal) {
                    Bukkit.broadcastMessage(Chat.c("§e§lYellow Team§r wins!"));
                } else if (!hasRedFinal && !hasYellowFinal) {
                    Bukkit.broadcastMessage(Chat.c("§a§lNo one wins!§r"));
                }
            });

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (player.equals(p)) {continue;}
                tpToLobby(p);
            }

            players.clear();
            clearArena();
        }
    }

    @EventHandler
    public void $onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (game) {
            GameLib.spectate(player, spectatorSpawn);
        } else {
            tpToLobby(player);
        }
    }

    @EventHandler
    public void $onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (game) {
            GameLib.spectate(player, spectatorSpawn);
        } else {
            tpToLobby(player);
        }
    }

    @EventHandler
    public void $onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        players.remove(player);
        InventoryCommand.exit(player);
    }

    public static ItemStack getMenuItem() {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Chat.c("§b§lSkyFight Menu"));
            meta.setLore(List.of(Chat.c("§7Right click to open the menu")));
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            item.setItemMeta(meta);
        }
        return item;
    }


    public static void playerStart(Player player, Kit kit) {
        Team team = scoreboard.getEntityTeam(player);

        player.getInventory().clear();
        defaultKit.give(player);
        kit.give(player);
        player.closeInventory();

        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExp(0);
        player.setLevel(0);
        player.clearActivePotionEffects();

        player.teleport(spawnPoints.get(teams.indexOf(team)));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 5 * 10, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999999, 0, false, false));

        // player.setGameMode(GameMode.SURVIVAL);
        if (!players.contains(player)) {players.add(player);}

        if (players.size() == Bukkit.getOnlinePlayers().size()) {
            startGame();
        }
    }

    public static void startGame() {
        game = true;
        Bukkit.broadcastMessage(Chat.c("\n§lGame started!"));

        for (Player player : players) {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    public static void tpToLobby(Player player) {
        player.teleport(lobby);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExp(0);
        player.setLevel(0);
        player.clearActivePotionEffects();
        player.getInventory().clear();

        if (!game) {player.getInventory().setItem(0, getMenuItem());}
    }


    public void useInventory(Player player, String inventoryName) {
        InventoryOrder inventory = InventoryOrder.orders.get(inventoryName);
        if (inventory == null) {
            player.sendMessage(Chat.c("§c§lInventory \"" + inventoryName + "\" not found!"));
            return;
        }

        InventoryOrder.playerOrders.put(player.getName(), inventoryName);

        try {
            config.set("saved-inventories." + player.getName(), inventoryName);
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(Chat.c("§c§lFailed to save inventory!"));
            return;
        }

        player.sendMessage(Chat.c("Inventory successfully updated to §a§l" + inventoryName + "§r."));
    }

    public void tryInventory(Player player, String inventoryName) {
        if (game || players.contains(player)) {
            player.sendMessage(Chat.c("§c§lYou cannot try an inventory while a game is in progress!"));
            return;
        }

        InventoryOrder inventory = InventoryOrder.orders.get(inventoryName);
        if (inventory == null) {
            player.sendMessage(Chat.c("§c§lInventory \"" + inventoryName + "\" not found!"));
            return;
        }

        applyInventory(player, inventory);
    }

    public void applyInventory(Player player, InventoryOrder inventory) {
        player.getInventory().clear();
        defaultKit.give(player, inventory);
        showcaseKit.give(player, inventory);
    }

    public void createInventory(Player player, HashMap<Integer, String> items, String name) {
        InventoryOrder order = new InventoryOrder();

        for (int slot : items.keySet()) {
            order.addSlot(slotNames.get(items.get(slot).toLowerCase()), slot);
        }

        InventoryOrder.orders.put(name, order);
        InventoryOrder.playerOrders.put(player.getName(), name);

        try {
            config.set("inventory." + name, order);
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(Chat.c("§c§lFailed to create inventory!"));
            return;
        } finally {
            // InventoryCommand.exit(player);
        }

        player.sendMessage(Chat.c("Inventory §a§l\"" + name + "\"§r created successfully!"));
    }

    public void removeInventory(String name) {
        InventoryOrder.orders.remove(name);
        InventoryOrder.playerOrders.values().removeIf(value -> value.equals(name));

        for (String playerName : InventoryOrder.playerOrders.keySet()) {
            if (InventoryOrder.playerOrders.get(playerName).equals(name)) {
                InventoryOrder.playerOrders.put(playerName, "default");
            }
        }

        try {
            config.set("inventory." + name, null);
            config.set("saved-inventories." + name, "default");
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void clearArena() {
        for (BlockFill fill : arenaFills) {
            fill.fill(world);
        }

        for (Entity e : world.getEntities()) {
            if (!(e instanceof Player)) {e.remove();}
        }
    }

    private void loadConfig() {
        loadInventoryOrders();
        loadKits();
        loadArenaFill();
    }

    private void loadInventoryOrders() {
        var inventorySection = config.getConfigurationSection("inventory");
        if (inventorySection == null) {return;}
        
        for (String invKey : inventorySection.getKeys(false)) {
            InventoryOrder order = new InventoryOrder();

            var invSection = inventorySection.getConfigurationSection(invKey);
            if (invSection == null) {continue;}

            for (String slot : invSection.getKeys(false)) {
                order.addSlot(slot, invSection.getInt(slot));
            }

            InventoryOrder.orders.put(invKey, order);
        }

        var savedSection = config.getConfigurationSection("saved-inventories");
        if (savedSection == null) {return;}

        for (String playerName : savedSection.getKeys(false)) {
            String inventoryName = savedSection.getString(playerName);
            if (inventoryName != null) {
                InventoryOrder.playerOrders.put(playerName, inventoryName);
            }
        }
    }

    private void loadKits() {
        var kitsSection = kitConfig.getConfigurationSection("kits");
        if (kitsSection == null) {return;}
        
        for (String kitKey : kitsSection.getKeys(false)) {
            var kitSection = kitsSection.getConfigurationSection(kitKey);
            if (kitSection == null) {continue;}

            String name = kitSection.getString("name");
            
            String materialName = kitSection.getString("material");
            Material material = null;
            
            if (materialName != null) {
                material = Material.getMaterial(materialName.toUpperCase());
            }
            
            if (material == null) {material = GameLib.DEFAULT_MATERIAL;}
            
            var itemsSection = kitSection.getConfigurationSection("items");
            HashMap<String, ItemTemplate> items = ItemLoader.loadConditionalItemsMap(itemsSection, new HashMap<>() {{
                put("yellow", (Player p) -> yellowTeam.hasPlayer(p));
                put("red",    (Player p) -> redTeam.hasPlayer(p));
            }});

            Logger.info("Loaded kit {0} - {1} items", name, items.size());

            if (kitKey.equals("default")) {
                defaultKit = new Kit("default", material, items);
            } else if (kitKey.equals("showcase")) {
                showcaseKit = new Kit("showcase", material, items);
            } else {
                kits.add(new Kit(name, material, items));
            }
        }
    }

    private void loadArenaFill() {
        var fillSection = config.getConfigurationSection("arena-fill");
        if (fillSection == null) {return;}

        for (String key : fillSection.getKeys(false)) {
            var areaSection = fillSection.getConfigurationSection(key);
            if (areaSection == null) {continue;}

            Material material = Material.getMaterial(areaSection.getString("block", "air").toUpperCase());
            BlockFill fill = BlockFill.fromString(areaSection.getString("pos1"), areaSection.getString("pos2"), " ", world, material);
            arenaFills.add(fill);
        }
    }
}
