package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Claim;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUIManager
 * Provides player + admin inventory menus.
 * - Claim, info, unclaim
 * - Trust/help/admin
 * - Admin menu with toggles
 */
public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plots;

    // Cached config options
    private boolean debugMode;
    private boolean dropCompassIfFull;

    public GUIManager(PlotManager plots) {
        this.plugin = ProShield.getInstance();
        this.plots = plots;
        reloadFromConfig();
    }

    /** Refresh toggles from config (on /proshield reload) */
    public void reloadFromConfig() {
        FileConfiguration c = plugin.getConfig();
        this.debugMode = c.getBoolean("proshield.debug", false);
        this.dropCompassIfFull = c.getBoolean("compass.drop-if-full", true);
    }

    /* =========================================================
       Player Main Menu
       ========================================================= */
    public Inventory openMain(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, color("&3ProShield &7Menu"));

        inv.setItem(11, item(Material.GRASS_BLOCK, "&aClaim Chunk", "&7Claim the land you stand in."));
        inv.setItem(13, item(Material.PAPER, "&bClaim Info", "&7View owner and trusted players."));
        inv.setItem(15, item(Material.BARRIER, "&cUnclaim Chunk", "&7Remove your claim here."));

        inv.setItem(31, item(Material.BOOK, "&eHelp", "&7Click to view commands and usage."));
        inv.setItem(33, item(Material.REDSTONE, "&cAdmin Menu", "&7Admin-only tools and settings."));

        inv.setItem(48, item(Material.ARROW, "&7Back", "&7Return to previous page."));
        inv.setItem(49, item(Material.OAK_SIGN, "&eInfo", "&7Manage claims with GUI or commands."));

        p.openInventory(inv);
        return inv;
    }

    /* =========================================================
       Admin Menu
       ========================================================= */
    public Inventory openAdmin(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, color("&4ProShield Admin"));

        // Toggles
        inv.setItem(10, toggleItem(Material.FLINT_AND_STEEL, "Fire Protection", "protection.fire.enabled"));
        inv.setItem(12, toggleItem(Material.TNT, "Explosion Protection", "protection.explosions.enabled"));
        inv.setItem(14, toggleItem(Material.IRON_BARS, "Entity Grief Protection", "protection.entity-grief.enabled"));
        inv.setItem(16, toggleItem(Material.STONE_BUTTON, "Interaction Protection", "protection.interactions.enabled"));

        // PvP
        inv.setItem(20, toggleItem(Material.DIAMOND_SWORD, "PvP in Claims", "protection.pvp-in-claims"));

        // Compass
        inv.setItem(22, item(Material.COMPASS, "&bCompass Settings",
                "&7Drop-if-full: " + (dropCompassIfFull ? "&aON" : "&cOFF"),
                "&8Toggle in config.yml."));

        // Debug toggle
        inv.setItem(24, item(Material.REPEATER, "&dDebug Logging",
                "&7Currently: " + (debugMode ? "&aON" : "&cOFF"),
                "&8Toggle with /proshield debug."));

        // Admin Help (NEW small tooltip)
        inv.setItem(31, item(Material.BOOK, "&eAdmin Help",
                "&7- Manage protections per world/claim.",
                "&7- Use /proshield reload after config edits.",
                "&7- More tools are coming in v2.0!"));

        // Back button
        inv.setItem(48, item(Material.ARROW, "&7Back", "&7Return to player menu."));

        p.openInventory(inv);
        return inv;
    }

    /* =========================================================
       Helpers
       ========================================================= */

    private ItemStack item(Material mat, String name, String... loreLines) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));
            List<String> lore = new ArrayList<>();
            for (String s : loreLines) lore.add(color(s));
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack toggleItem(Material mat, String name, String path) {
        boolean enabled = plugin.getConfig().getBoolean(path, true);
        return item(mat,
                "&b" + name,
                "&7Currently: " + (enabled ? "&aON" : "&cOFF"),
                "&8Path: " + path);
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
