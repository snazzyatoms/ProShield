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

        inv.setItem(11, createItem(Material.BOOK, ChatColor.GREEN + "Trust Menu", "Manage trusted players"));
        inv.setItem(12, createItem(Material.BARRIER, ChatColor.RED + "Untrust Menu", "Remove trusted players"));
        inv.setItem(13, createItem(Material.IRON_SWORD, ChatColor.YELLOW + "Flags", "Toggle claim flags"));
        inv.setItem(14, createItem(Material.NAME_TAG, ChatColor.GOLD + "Roles", "Manage roles in this claim"));
        inv.setItem(15, createItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "Transfer Claim", "Transfer ownership"));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openAdminMain(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_RED + "ProShield Admin Menu");

        inv.setItem(11, createItem(Material.COMPASS, ChatColor.AQUA + "Teleport to Claim", "Admin-only teleport"));
        inv.setItem(12, createItem(Material.TNT, ChatColor.RED + "Force Unclaim", "Remove any claim"));
        inv.setItem(13, createItem(Material.CHEST, ChatColor.GOLD + "Manage Flags", "Override claim flags"));
        inv.setItem(14, createItem(Material.HOPPER, ChatColor.YELLOW + "Toggle Keep-Items", "Force keep-drops"));
        inv.setItem(15, createItem(Material.BOOK, ChatColor.DARK_PURPLE + "Wilderness Tools", "Manage wilderness settings"));

        cache.setAdminMenu(player, inv);
        player.openInventory(inv);
    }

    /* ---------------------------------------------------------
     * SUB MENUS
     * --------------------------------------------------------- */

    public void openFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.YELLOW + "Claim Flags");

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

        inv.setItem(11, createItem(Material.STONE_PICKAXE, ChatColor.GREEN + "Builder", "Normal build rights"));
        inv.setItem(12, createItem(Material.IRON_SWORD, ChatColor.RED + "Moderator", "PvP / entity rights"));
        inv.setItem(13, createItem(Material.DIAMOND, ChatColor.GOLD + "Manager", "Full management of claim"));
        inv.setItem(15, createItem(Material.BOOK, ChatColor.YELLOW + "Trusted List", "View all trusted players"));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openTransferMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.LIGHT_PURPLE + "Transfer Claim");

        inv.setItem(4, createItem(Material.ENDER_PEARL, ChatColor.AQUA + "Transfer Claim", "Click to confirm transfer"));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openTrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.GREEN + "Trust Player");

        inv.setItem(4, createItem(Material.BOOK, ChatColor.GREEN + "Trust Player", "Use /trust <name> [role]"));

        cache.setPlayerMenu(player, inv);
        player.openInventory(inv);
    }

    public void openUntrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.RED + "Untrust Player");

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

    public void clearCache() {
        cache.clearCache();
    }
}
