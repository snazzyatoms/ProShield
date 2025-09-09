package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager {
    private final ProShield plugin;
    private final PlotManager plots;

    public static final String TITLE_MAIN = ChatColor.DARK_AQUA + "ProShield Menu";
    public static final String TITLE_ADMIN = ChatColor.RED + "ProShield Admin Menu";

    public GUIManager(ProShield plugin, PlotManager plots) {
        this.plugin = plugin;
        this.plots = plots;
    }

    /** Opens the main player GUI */
    public void openMain(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE_MAIN);

        gui.setItem(11, createItem(Material.GRASS_BLOCK, "&aClaim Land", "Claim your current chunk"));
        gui.setItem(13, createItem(Material.PAPER, "&eClaim Info", "View info about this claim"));
        gui.setItem(15, createItem(Material.BARRIER, "&cUnclaim", "Unclaim your current chunk"));
        gui.setItem(31, createItem(Material.BOOK, "&bHelp", "Learn how ProShield works"));

        // Trust / Untrust / Transfer features for players
        gui.setItem(20, createItem(Material.PLAYER_HEAD, "&aTrust Player", "Trust another player in your claim"));
        gui.setItem(21, createItem(Material.SKELETON_SKULL, "&cUntrust Player", "Remove a player from trust list"));
        gui.setItem(23, createItem(Material.CHEST, "&6Transfer Claim", "Transfer this claim to another player"));

        // Admin button only visible to those with permission
        if (player.hasPermission("proshield.admin")) {
            gui.setItem(33, createItem(Material.COMPASS, "&4Admin Tools", "Open the Admin GUI"));
        }

        gui.setItem(48, createItem(Material.ARROW, "&7Back", "Close this menu"));

        player.openInventory(gui);
    }

    /** Opens the admin GUI */
    public void openAdmin(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE_ADMIN);

        gui.setItem(10, createItem(Material.FLINT_AND_STEEL, "&cToggle Fire Protection", "Enable/disable fire inside claims"));
        gui.setItem(11, createItem(Material.TNT, "&cToggle Explosions", "Enable/disable explosions inside claims"));
        gui.setItem(12, createItem(Material.ENDERMAN_SPAWN_EGG, "&cToggle Entity Grief", "Enable/disable mob griefing inside claims"));
        gui.setItem(13, createItem(Material.REDSTONE, "&cToggle Interactions", "Enable/disable redstone/door use by strangers"));
        gui.setItem(14, createItem(Material.IRON_SWORD, "&cToggle PvP", "Enable/disable PvP inside claims"));

        gui.setItem(20, createItem(Material.CHEST, "&6Toggle Keep Items", "Enable/disable item drop protection"));
        gui.setItem(21, createItem(Material.LAVA_BUCKET, "&cPurge Expired", "Remove inactive claims"));
        gui.setItem(22, createItem(Material.BOOK, "&bHelp", "Admin help docs"));
        gui.setItem(23, createItem(Material.DEBUG_STICK, "&dToggle Debug", "Enable/disable debug logs"));
        gui.setItem(24, createItem(Material.COMPASS, "&eToggle Compass Drop", "Toggle compass drop on join"));

        // Reload option
        gui.setItem(25, createItem(Material.EMERALD, "&aReload Config", "Reload config.yml without restart"));

        // Spawn guard option
        gui.setItem(30, createItem(Material.BEDROCK, "&cSpawn Guard", "Block claiming near spawn (radius in config)"));

        gui.setItem(31, createItem(Material.ARROW, "&7Back", "Return to main menu"));

        player.openInventory(gui);
    }

    /** Handle reload action from Admin GUI */
    public void doReload(Player player) {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();
        player.sendMessage(ChatColor.GREEN + "[ProShield] Config reloaded. Spawn radius = " + cfg.getInt("spawn.radius"));
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
