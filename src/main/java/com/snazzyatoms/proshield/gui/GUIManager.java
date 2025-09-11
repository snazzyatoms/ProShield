package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * GUIManager
 *
 * Handles all menus:
 * - Main menu (player)
 * - Flags menu
 * - Roles menu
 * - Trust/untrust menus
 * - Transfer ownership
 * - Admin menu
 *
 * Uses GUICache to track which menu a player is viewing.
 */
public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* -------------------------
     * Main Menu
     * ------------------------- */
    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dProShield Menu");

        inv.setItem(11, menuItem(Material.PAPER, "§eFlags", "Toggle claim flags"));
        inv.setItem(13, menuItem(Material.BOOK, "§bRoles", "Manage claim roles"));
        inv.setItem(15, menuItem(Material.ENDER_PEARL, "§aTransfer", "Transfer ownership"));

        player.openInventory(inv);
        cache.setPlayerMenu(player, inv);
    }

    /* -------------------------
     * Flags Menu
     * ------------------------- */
    public void openFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dClaim Flags");

        inv.setItem(11, menuItem(Material.REDSTONE_TORCH, "§cPvP", "Toggle PvP inside this claim"));
        inv.setItem(13, menuItem(Material.TNT, "§cExplosions", "Toggle explosions inside this claim"));
        inv.setItem(15, menuItem(Material.FLINT_AND_STEEL, "§cFire", "Toggle fire inside this claim"));

        player.openInventory(inv);
        cache.setPlayerMenu(player, inv);
    }

    /* -------------------------
     * Roles Menu
     * ------------------------- */
    public void openRolesGUI(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dClaim Roles");

        inv.setItem(11, menuItem(Material.IRON_SWORD, "§bBuilders", "Players who can build"));
        inv.setItem(13, menuItem(Material.CHEST, "§aContainers", "Players who can open containers"));
        inv.setItem(15, menuItem(Material.NAME_TAG, "§eManagers", "Players who can manage claim"));

        player.openInventory(inv);
        cache.setPlayerMenu(player, inv);
    }

    /* -------------------------
     * Transfer Menu
     * ------------------------- */
    public void openTransferMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dTransfer Ownership");

        inv.setItem(13, menuItem(Material.PAPER, "§aTransfer", "Select a player to transfer ownership"));

        player.openInventory(inv);
        cache.setPlayerMenu(player, inv);
    }

    /* -------------------------
     * Trust Menu
     * ------------------------- */
    public void openTrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dTrust Player");

        inv.setItem(13, menuItem(Material.PLAYER_HEAD, "§aTrust", "Trust a player"));

        player.openInventory(inv);
        cache.setPlayerMenu(player, inv);
    }

    /* -------------------------
     * Untrust Menu
     * ------------------------- */
    public void openUntrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dUntrust Player");

        inv.setItem(13, menuItem(Material.PLAYER_HEAD, "§cUntrust", "Remove a player’s trust"));

        player.openInventory(inv);
        cache.setPlayerMenu(player, inv);
    }

    /* -------------------------
     * Admin Menu
     * ------------------------- */
    public void openAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§4Admin Menu");

        inv.setItem(11, menuItem(Material.COMPASS, "§cTeleport", "Teleport to claims"));
        inv.setItem(13, menuItem(Material.BARRIER, "§cForce Unclaim", "Unclaim land as admin"));
        inv.setItem(15, menuItem(Material.BOOK, "§cFlags", "Toggle claim flags as admin"));

        player.openInventory(inv);
        cache.setAdminMenu(player, inv);
    }

    /* -------------------------
     * Utilities
     * ------------------------- */
    private ItemStack menuItem(Material mat, String name, String... lore) {
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
