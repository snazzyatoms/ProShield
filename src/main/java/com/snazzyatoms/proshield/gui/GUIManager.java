// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* ---------------------------------------------------------
     * MAIN MENUS
     * --------------------------------------------------------- */

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.AQUA + "ProShield Menu");

        // Filler for a polished look
        fill(inv, Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + " ");

        // Row of actions
        inv.setItem(10, createItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Chunk", "Claim the current chunk"));
        inv.setItem(11, createItem(Material.BARRIER, ChatColor.RED + "Unclaim Chunk", "Unclaim the current chunk"));
        inv.setItem(12, createItem(Material.MAP, ChatColor.YELLOW + "Claim Info", "Show info about this claim"));
        inv.setItem(13, createItem(Material.BOOK, ChatColor.GREEN + "Trust Menu", "Manage trusted players"));
        inv.setItem(14, createItem(Material.BOOKSHELF, ChatColor.RED + "Untrust Menu", "Remove trusted players"));
        inv.setItem(15, createItem(Material.IRON_SWORD, ChatColor.GOLD + "Flags", "Toggle claim flags"));
        inv.setItem(16, createItem(Material.NAME_TAG, ChatColor.AQUA + "Roles", "Manage claim roles"));
        inv.setItem(22, createItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "Transfer Claim", "Transfer ownership"));

        // Admin entry point
        if (player.isOp() || player.hasPermission("proshield.admin")) {
            inv.setItem(8, createItem(Material.NETHER_STAR, ChatColor.DARK_RED + "Admin Tools", "Open admin controls"));
        }

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openAdminMain(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_RED + "ProShield Admin Menu");

        fill(inv, Material.RED_STAINED_GLASS_PANE, ChatColor.DARK_RED + " ");

        // Give admins the same claim actions for convenience
        inv.setItem(10, createItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Claim Chunk", "Admin: claim current chunk"));
        inv.setItem(11, createItem(Material.BARRIER, ChatColor.RED + "Unclaim Chunk", "Admin: unclaim current chunk"));
        inv.setItem(12, createItem(Material.MAP, ChatColor.YELLOW + "Claim Info", "Show info about this claim"));

        // Admin-only tools
        inv.setItem(13, createItem(Material.COMPASS, ChatColor.AQUA + "Teleport to Claim", "Admin-only teleport"));
        inv.setItem(14, createItem(Material.TNT, ChatColor.DARK_RED + "Force Unclaim", "Remove any claim"));
        inv.setItem(15, createItem(Material.CHEST, ChatColor.GOLD + "Manage Flags (Admin)", "Override claim flags"));
        inv.setItem(16, createItem(Material.HOPPER, ChatColor.YELLOW + "Toggle Keep-Items", "Force keep-drops"));
        inv.setItem(22, createItem(Material.OAK_SIGN, ChatColor.DARK_PURPLE + "Wilderness Tools", "Manage wilderness settings"));

        // Navigation
        inv.setItem(26, createItem(Material.ARROW, ChatColor.GREEN + "Back to Player Menu", "Return to main menu"));

        cache.setAdminMenu(player, inv);
        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * SUB MENUS
     * --------------------------------------------------------- */

    public void openFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.YELLOW + "Claim Flags");

        fill(inv, Material.LIGHT_GRAY_STAINED_GLASS_PANE, ChatColor.GRAY + " ");

        inv.setItem(11, createItem(Material.IRON_SWORD, ChatColor.RED + "PvP", "Toggle PvP in this claim"));
        inv.setItem(12, createItem(Material.TNT, ChatColor.DARK_RED + "Explosions", "Toggle TNT/creeper damage"));
        inv.setItem(13, createItem(Material.FLINT_AND_STEEL, ChatColor.GOLD + "Fire", "Toggle fire spread & ignite"));
        inv.setItem(14, createItem(Material.REDSTONE, ChatColor.RED + "Redstone", "Toggle redstone mechanics"));
        inv.setItem(15, createItem(Material.CHEST, ChatColor.GREEN + "Containers", "Toggle chest/furnace access"));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openRolesGUI(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.AQUA + "Claim Roles");

        fill(inv, Material.LIGHT_BLUE_STAINED_GLASS_PANE, ChatColor.AQUA + " ");

        inv.setItem(11, createItem(Material.STONE_PICKAXE, ChatColor.GREEN + "Builder", "Normal build rights"));
        inv.setItem(12, createItem(Material.IRON_SWORD, ChatColor.RED + "Moderator", "PvP / entity rights"));
        inv.setItem(13, createItem(Material.DIAMOND, ChatColor.GOLD + "Manager", "Full management of claim"));
        inv.setItem(15, createItem(Material.BOOK, ChatColor.YELLOW + "Trusted List", "View all trusted players"));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openTransferMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.LIGHT_PURPLE + "Transfer Claim");

        fill(inv, Material.PURPLE_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + " ");

        inv.setItem(4, createItem(Material.ENDER_PEARL, ChatColor.AQUA + "Transfer Claim", "Click to confirm transfer"));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openTrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.GREEN + "Trust Player");

        fill(inv, Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + " ");

        inv.setItem(4, createItem(Material.BOOK, ChatColor.GREEN + "Trust Player", "Use /trust <name> [role]"));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openUntrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.RED + "Untrust Player");

        fill(inv, Material.RED_STAINED_GLASS_PANE, ChatColor.RED + " ");

        inv.setItem(4, createItem(Material.BARRIER, ChatColor.RED + "Untrust Player", "Use /untrust <name>"));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * HELPERS
     * --------------------------------------------------------- */

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fill(Inventory inv, Material filler, String name) {
        ItemStack glass = createItem(filler, name);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                inv.setItem(i, glass);
            }
        }
    }

    public void clearCache() { cache.clearCache(); }

    public GUICache getCache() { return cache; }

    public ProShield getPlugin() { return plugin; }
}
