// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public GUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotManager = plugin.getPlotManager();
    }

    // -------------------------
    // Open menu
    // -------------------------
    public void openMenu(Player player, String menu) {
        switch (menu.toLowerCase(Locale.ROOT)) {
            case "flags" -> openFlagsMenu(player);
            case "roles" -> openRolesMenu(player);
            default -> openMainMenu(player);
        }
    }

    // -------------------------
    // Main Menu
    // -------------------------
    private void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "ProShield Menu");

        inv.setItem(10, makeItem(Material.GRASS_BLOCK, "&aClaim Land", "&7Claim this chunk of land"));
        inv.setItem(11, makeItem(Material.BOOK, "&eClaim Info", "&7View details about this claim"));
        inv.setItem(12, makeItem(Material.REDSTONE_BLOCK, "&cUnclaim Land", "&7Remove your current claim"));
        inv.setItem(13, makeItem(Material.PLAYER_HEAD, "&bTrusted Players", "&7Manage trusted players & roles"));
        inv.setItem(14, makeItem(Material.LEVER, "&dClaim Flags", "&7Toggle protection flags"));
        inv.setItem(15, makeItem(Material.BARRIER, "&cAdmin Tools", "&7Admin options"));

        player.openInventory(inv);
    }

    // -------------------------
    // Flags Menu
    // -------------------------
    private void openFlagsMenu(Player player) {
        Plot plot = plotManager.getPlot(player.getLocation());
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + "Claim Flags");

        // Get current flags for this plot (with defaults from config if missing)
        Map<String, Boolean> flags = plot != null ? plot.getFlags() : Map.of();

        inv.setItem(10, makeFlagItem(Material.TNT, "Explosions",
                "Toggles TNT and creeper block damage",
                getFlagState(flags, "explosions")));

        inv.setItem(11, makeFlagItem(Material.WATER_BUCKET, "Buckets",
                "Controls lava/water bucket use",
                getFlagState(flags, "buckets")));

        inv.setItem(12, makeFlagItem(Material.ITEM_FRAME, "Item Frames",
                "Protects item frames from grief",
                getFlagState(flags, "item-frames")));

        inv.setItem(13, makeFlagItem(Material.ARMOR_STAND, "Armor Stands",
                "Prevents breaking/moving armor stands",
                getFlagState(flags, "armor-stands")));

        inv.setItem(14, makeFlagItem(Material.CHEST, "Containers",
                "Locks chests, barrels, hoppers, furnaces",
                getFlagState(flags, "containers")));

        inv.setItem(15, makeFlagItem(Material.BONE, "Pets",
                "Protects wolves, cats, horses, and pets",
                getFlagState(flags, "pets")));

        inv.setItem(16, makeFlagItem(Material.IRON_SWORD, "PvP",
                "Enable or disable player-vs-player combat",
                getFlagState(flags, "pvp")));

        inv.setItem(17, makeFlagItem(Material.SHIELD, "Safe Zone",
                "Blocks hostile mob spawns & damage",
                getFlagState(flags, "safezone")));

        inv.setItem(26, makeItem(Material.BARRIER, "&cBack", "&7Return to main menu"));

        player.openInventory(inv);
    }

    // -------------------------
    // Roles Menu
    // -------------------------
    private void openRolesMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.YELLOW + "Trusted Players & Roles");

        // Placeholder if no trusted players exist
        inv.setItem(13, makeItem(Material.PAPER,
                "&7No Trusted Players",
                "&8You haven't trusted anyone yet.",
                "&8Use /trust <player> to add one."));

        inv.setItem(26, makeItem(Material.BARRIER, "&cBack", "&7Return to main menu"));

        player.openInventory(inv);
    }

    // -------------------------
    // Utility: Create item
    // -------------------------
    private ItemStack makeItem(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);

            // Remove "When in Main Hand" vanilla attributes
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
            meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
            item.setItemMeta(meta);
        }
        return item;
    }

    // -------------------------
    // Utility: Create flag item
    // -------------------------
    private ItemStack makeFlagItem(Material mat, String flagName, String description, boolean state) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + flagName);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + description);
            lore.add(" ");
            lore.add(ChatColor.WHITE + "Current: " + formatFlagState(state, flagName));

            meta.setLore(lore);

            // Remove vanilla attribute text
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    // -------------------------
    // State Formatting
    // -------------------------
    private String formatFlagState(boolean state, String flagName) {
        if (state) {
            return ChatColor.GREEN + "ON " + ChatColor.GRAY + "(" + flagName + " allowed)";
        } else {
            return ChatColor.RED + "OFF " + ChatColor.GRAY + "(" + flagName + " blocked)";
        }
    }

    // -------------------------
    // Helper: get flag with default
    // -------------------------
    private boolean getFlagState(Map<String, Boolean> flags, String key) {
        return flags.getOrDefault(key.toLowerCase(Locale.ROOT),
                plugin.getConfig().getBoolean("claims.default-flags." + key, false));
    }
}
