package com.pythoncraft.skyfight;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
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

import com.pythoncraft.skyfight.command.InventoryCommand;
import com.pythoncraft.skyfight.command.InventoryTabCompleter;
import com.pythoncraft.gamelib.gui.GUIClickEvent;
import com.pythoncraft.gamelib.gui.GUIIdentifier;
import com.pythoncraft.gamelib.gui.GUIManager;
import com.pythoncraft.gamelib.inventory.Kit;
import com.pythoncraft.gamelib.inventory.InventoryLayout;

import net.kyori.adventure.text.format.NamedTextColor;

import com.pythoncraft.gamelib.inventory.ItemLoader;
import com.pythoncraft.gamelib.inventory.ItemTemplate;
import com.pythoncraft.gamelib.BlockFill;
import com.pythoncraft.gamelib.Chat;
import com.pythoncraft.gamelib.GameLib;
import com.pythoncraft.gamelib.Logger;
import com.pythoncraft.gamelib.PlayerActions;


public class PluginMain extends JavaPlugin implements Listener {
    
    static PluginMain instance;
    public static PluginMain getInstance() { return instance; }

    private File configFile;
    private FileConfiguration config;
    private File kitFile;
    private FileConfiguration kitConfig;
    private File layoutsFile;
    private FileConfiguration layoutsConfig;

    public List<Kit> kits = new ArrayList<>();
    public Kit defaultKit;
    public Kit showcaseKit;
    public HashMap<String, String> kitPlaceholders = new HashMap<>();

    public List<BlockFill> arenaFills = new ArrayList<>();
    public World world;

    public ScoreboardManager sm;
    public Scoreboard scoreboard;

    public Team redTeam;
    public Team yellowTeam;
    public List<Team> teams = new ArrayList<>();
    public Location redSpawn;
    public Location yellowSpawn;
    public List<Location> spawnPoints = new ArrayList<>();
    public Location lobby;
    public Location spectatorSpawn;

    public boolean isGame = false;
    public HashSet<Player> playersInGame = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(GUIManager.getInstance(), this);

        // Save default config files from resources if they don't exist
        saveResource("config.yml", false);
        saveResource("kits.yml", false);
        saveResource("layouts.yml", false);

        this.configFile  = new File(getDataFolder(), "config.yml");
        this.config        = YamlConfiguration.loadConfiguration(this.configFile);
        this.kitFile     = new File(getDataFolder(), "kits.yml");
        this.kitConfig     = YamlConfiguration.loadConfiguration(this.kitFile);
        this.layoutsFile = new File(getDataFolder(), "layouts.yml");
        this.layoutsConfig = YamlConfiguration.loadConfiguration(this.layoutsFile);

        this.world = Bukkit.getWorld("world");

        this.sm = Bukkit.getScoreboardManager();
        this.scoreboard = sm.getMainScoreboard();

        this.redTeam = GameLib.createTeam("red", "§c§l[RED]§r ", NamedTextColor.RED);
        this.yellowTeam = GameLib.createTeam("yellow", "§e§l[YELLOW]§r ", NamedTextColor.YELLOW);
        this.teams = List.of(redTeam, yellowTeam);

        this.loadConfig();

        GUIManager.getInstance().register("team", true, guiPlayer -> {
            Inventory inventory = Bukkit.createInventory(new GUIIdentifier("team"), 27, Chat.component("§lSelect Team"));

            ItemStack red = new ItemStack(Material.RED_WOOL);
            ItemMeta redMeta = red.getItemMeta();
            redMeta.displayName(Chat.component("§c§lRed Team"));
            red.setItemMeta(redMeta);

            ItemStack yellow = new ItemStack(Material.YELLOW_WOOL);
            ItemMeta yellowMeta = yellow.getItemMeta();
            yellowMeta.displayName(Chat.component("§e§lYellow Team"));
            yellow.setItemMeta(yellowMeta);

            inventory.setItem(11, red);
            inventory.setItem(15, yellow);

            return inventory;
        });

        GUIManager.getInstance().register("kit", true, guiPlayer -> {
            Inventory inventory = Bukkit.createInventory(new GUIIdentifier("kit"), 54, Chat.component("§lSelect Kit"));

            int slot = 0;
            for (Kit kit : kits) {
                Material kitMaterial = kit.material != null ? kit.material : Material.STICK;
                ItemStack item = new ItemStack(kitMaterial);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    meta.displayName(Chat.component(kit.displayName));
                    meta.setAttributeModifiers(MultimapBuilder.hashKeys().hashSetValues().build());
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    item.setItemMeta(meta);
                }

                inventory.setItem(slot, item);
                slot += 1;
            }

            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta barrierMeta = barrier.getItemMeta();
            barrierMeta.displayName(Chat.component("§c§lBack to team selection"));
            barrier.setItemMeta(barrierMeta);
            inventory.setItem(53, barrier);

            return inventory;
        });

        this.getCommand("inventory").setTabCompleter(new InventoryTabCompleter());
		this.getCommand("inventory").setExecutor(new InventoryCommand());
    }

    @Override
    public void onDisable() {
        this.playersInGame.clear();
        this.isGame = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("kit")) {
            if (isGame) {
                sender.sendMessage("§c§lYou cannot open the kit menu while a game is in progress!");
                return true;
            }

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
	public void onGuiClick(GUIClickEvent guiClickEvent) {
        InventoryClickEvent inventoryClickEvent = guiClickEvent.getInventoryClickEvent();
        int slot = inventoryClickEvent.getSlot();
        Player player = (Player) inventoryClickEvent.getWhoClicked();

		if (guiClickEvent.getID().equals("team")) {
            if (slot == 11 && !redTeam.hasEntity(player)) {
                redTeam.addEntity(player);
            } else if (slot == 15 && !yellowTeam.hasEntity(player)) {
                yellowTeam.addEntity(player);
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
    public void onRightClick(PlayerInteractEvent event) {
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
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {return;}

        if (!this.isGame) {event.setCancelled(true);}
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (this.playersInGame.contains(player)) {this.playersInGame.remove(player);}

        boolean hasYellow = false;
        boolean hasRed = false;

        for (Player p : this.playersInGame) {
            if (yellowTeam.hasEntity(p)) {hasYellow = true;}
            if (redTeam.hasEntity(p)) {hasRed = true;}
        }

        // Needed to pass these variables to the Bukkit scheduler function
        final boolean hasYellowFinal = hasYellow;
        final boolean hasRedFinal = hasRed;

        if (!hasYellow || !hasRed) {
            // End the game if one team is eliminated
            this.isGame = false;

            Bukkit.getScheduler().runTask(this, () -> {
                Chat.broadcast("§lGame Over!");
                if (hasRedFinal && !hasYellowFinal) {
                    Chat.broadcast("§c§lRed Team§r wins!");
                } else if (hasYellowFinal && !hasRedFinal) {
                    Chat.broadcast("§e§lYellow Team§r wins!");
                } else if (!hasRedFinal && !hasYellowFinal) {
                    Chat.broadcast("§a§lNo one wins!§r");
                }
                
                clearArena();
            });

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (player.equals(p)) {continue;}
                tpToLobby(p);
            }

            this.playersInGame.clear();
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (this.isGame) {
            event.setRespawnLocation(this.spectatorSpawn);
            Bukkit.getScheduler().runTask(this, () -> {
                GameLib.spectate(player, this.spectatorSpawn);
            });
        } else {
            event.setRespawnLocation(this.lobby);
            Bukkit.getScheduler().runTask(this, () -> {
                tpToLobby(player);
            });
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (this.isGame) {
            GameLib.spectate(player, this.spectatorSpawn);
        } else {
            tpToLobby(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.playersInGame.remove(player);
        InventoryCommand.exit(player);
    }

    public static ItemStack getMenuItem() {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Chat.component("§b§lSkyFight Menu"));
            meta.lore(List.of(Chat.component("§7Right click to open the menu")));
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            item.setItemMeta(meta);
        }
        return item;
    }


    public void playerStart(Player player, Kit kit) {
        Team team = this.scoreboard.getEntityTeam(player);

        PlayerActions.setupPlayerReset(List.of(
            new PotionEffect(PotionEffectType.SATURATION, 50, 0, false, false),
            new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, false, false)
        )).accept(player, playersInGame);

        player.setGameMode(GameMode.ADVENTURE);

        this.defaultKit.give(player);
        kit.give(player);
        player.closeInventory();

        player.teleport(this.spawnPoints.get(teams.indexOf(team)));

        if (!this.playersInGame.contains(player)) {this.playersInGame.add(player);}

        Bukkit.getScheduler().runTask(this, () -> {
            if (this.playersInGame.size() == Bukkit.getOnlinePlayers().size() && !this.isGame) {
                this.startGame();
            }
        });
    }

    public void startGame() {
        if (this.isGame) {return;}

        this.isGame = true;
        Chat.broadcast("\n§lGame started!");

        for (Player player : this.playersInGame) {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    public void tpToLobby(Player player) {
        PlayerActions.setupPlayerReset(null).accept(player, playersInGame);
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setFallDistance(0);
        player.teleport(this.lobby);

        if (!this.isGame) {player.getInventory().setItem(0, getMenuItem());}
    }


    public void useLayout(Player player, String layoutName) {
        InventoryLayout inventory = InventoryLayout.layouts.get(layoutName);
        if (inventory == null) {
            Chat.message(player, "§c§lInventory layout \"" + layoutName + "\" not found!");
            return;
        }

        InventoryLayout.playerLayouts.put(player.getName(), layoutName);
        try {
            config.set("saved-layouts." + player.getName(), layoutName);
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(Chat.c("§c§lFailed to save inventory layout!"));
            return;
        }

        player.sendMessage(Chat.c("Inventory layout successfully updated to §a§l" + layoutName + "§r."));
    }

    public void tryLayout(Player player, String layoutName) {
        if (isGame || this.playersInGame.contains(player)) {
            player.sendMessage(Chat.c("§c§lYou cannot try an inventory layout while a game is in progress!"));
            return;
        }

        InventoryLayout layout = InventoryLayout.layouts.get(layoutName);
        if (layout == null) {
            player.sendMessage(Chat.c("§c§lInventory layout \"" + layoutName + "\" not found!"));
            return;
        }

        this.applyLayout(player, layout);
    }

    public void applyLayout(Player player, InventoryLayout layout) {
        player.getInventory().clear();
        this.defaultKit.give(player, layout);
        this.showcaseKit.give(player, layout);
    }

    public void createLayout(Player player, HashMap<Integer, String> items, String name) {
        InventoryLayout layout = new InventoryLayout(name);

        for (int slot : items.keySet()) {
            String slotItem = items.get(slot).toLowerCase();
            layout.mapSlot(this.kitPlaceholders.get(slotItem), slot);
        }

        InventoryLayout.layouts.put(name, layout);
        InventoryLayout.playerLayouts.put(player.getName(), name);

        try {
            this.layoutsConfig.set("layouts." + name, layout.slots);
            this.layoutsConfig.save(this.layoutsFile);
        } catch (IOException e) {
            e.printStackTrace();
            Chat.message(player, "§c§lFailed to create inventory layout!");
            return;
        }

        player.sendMessage(Chat.c("Inventory layout §a§l\"" + name + "\"§r created successfully!"));
    }

    public void removeLayout(String name) {
        InventoryLayout.layouts.remove(name);

        Logger.info("Player layouts: {0}", InventoryLayout.playerLayouts);

        for (String playerName : InventoryLayout.playerLayouts.keySet()) {
            if (InventoryLayout.playerLayouts.get(playerName).equals(name)) {
                InventoryLayout.playerLayouts.put(playerName, "default");
                Logger.info("Reset inventory layout for player {0} to default", playerName);
            }
        }

        try {
            this.layoutsConfig.set("layouts." + name, null);
            this.config.set("saved-layouts." + name, "default");
            this.config.save(this.configFile);
            this.layoutsConfig.save(this.layoutsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void clearArena() {
        for (BlockFill fill : this.arenaFills) {
            fill.fill(this.world);
        }

        for (Entity e : this.world.getEntities()) {
            if (!(e instanceof Player)) {e.remove();}
        }
    }

    private void loadConfig() {
        this.loadInventoryLayouts();
        this.loadKits();
        this.loadArenaFill();

        this.redSpawn       = ItemLoader.getLocationFromSection(this.config.getConfigurationSection("spawn-locations.red"), this.world);
        this.yellowSpawn    = ItemLoader.getLocationFromSection(this.config.getConfigurationSection("spawn-locations.yellow"), this.world);
        this.lobby          = ItemLoader.getLocationFromSection(this.config.getConfigurationSection("spawn-locations.lobby"), this.world);
        this.spectatorSpawn = ItemLoader.getLocationFromSection(this.config.getConfigurationSection("spawn-locations.spectator"), this.world);

        this.spawnPoints = List.of(redSpawn, yellowSpawn);
    }

    private void loadInventoryLayouts() {
        var layoutsSection = this.layoutsConfig.getConfigurationSection("layouts");
        if (layoutsSection == null) {return;}
        
        for (String invKey : layoutsSection.getKeys(false)) {
            InventoryLayout order = new InventoryLayout(invKey);

            var invSection = layoutsSection.getConfigurationSection(invKey);
            if (invSection == null) {continue;}

            for (String slot : invSection.getKeys(false)) {
                order.mapSlot(slot, invSection.getInt(slot));
            }

            InventoryLayout.layouts.put(invKey, order);
        }

        var placeholdersSection = this.layoutsConfig.getConfigurationSection("placeholders");
        if (placeholdersSection != null) {
            for (String slotName : placeholdersSection.getKeys(false)) {
                String placeholderItem = placeholdersSection.getString(slotName);
                if (placeholderItem != null) {
                    this.kitPlaceholders.put(placeholderItem, slotName);
                }
            }
        }

        HashMap<String, ItemTemplate> items = new HashMap<>();
        for (String placeholderItem : this.kitPlaceholders.keySet()) {
            String slotName = this.kitPlaceholders.get(placeholderItem);
            ItemStack itemStack = ItemLoader.loadShortItemStack(placeholderItem);
            items.put(slotName, new ItemTemplate(itemStack));
        }

        this.showcaseKit = new Kit("showcase", Material.STICK, items);

        var savedSection = this.config.getConfigurationSection("saved-layouts");
        if (savedSection == null) {return;}

        for (String playerName : savedSection.getKeys(false)) {
            String layoutName = savedSection.getString(playerName);
            if (layoutName != null) {
                InventoryLayout.playerLayouts.put(playerName, layoutName);
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
