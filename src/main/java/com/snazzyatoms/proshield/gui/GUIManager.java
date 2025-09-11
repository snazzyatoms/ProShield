package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * GUIManager handles building and opening inventory GUIs
 * for claim management (flags, trust, roles, transfer, etc.).
 *
 * Fixes:
 * - Restored missing methods: openMain, openFlagsMenu, openRolesGUI, openTrustMenu, openUntrustMenu, openTransferMenu, openAdmin
 * - Added safe defaults (stub GUIs) so commands/listeners compile
 * - Preserved prior item builders and role logic
 */
public class GUIManager {

    private final ProShield plugin;
    private final ClaimRoleManager roles;

    public GUIManager(ProShield plugin, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.roles = roles;
    }

    /* -------------------------------------------------------
     * GUI Openers (Restored)
     * ------------------------------------------------------- */

    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dClaim Menu");
        // Add representative buttons
        inv.setItem(11, createItem(Material.CHEST, "§aFlags"));
        inv.setItem(13, createItem(Material.PLAYER_HEAD, "§bRoles"));
        inv.setItem(15, createItem(Material.BOOK, "§eTransfer"));
        player.openInventory(inv);
    }

    public void openFlagsMenu(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dClaim Flags");
        inv.setItem(11, createItem(Material.DIAMOND_SWORD, "Toggle PvP"));
        inv.setItem(12, createItem(Material.TNT, "Toggle Explosions"));
        inv.setItem(13, createItem(Material.FLINT_AND_STEEL, "Toggle Fire"));
        inv.setItem(14, createItem(Material.ENDER_PEARL, "Toggle Entity Grief"));
        inv.setItem(15, createItem(Material.REDSTONE, "Toggle Interactions"));
        inv.setItem(16, createItem(Material.CHEST, "Toggle Containers"));
        inv.setItem(22, createItem(Material.BARRIER, "§cBack"));
        player.openInventory(inv);
    }

    public void openRolesGUI(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dClaim Roles");
        inv.setItem(11, createItem(Material.PAPER, "§aTrust Player"));
        inv.setItem(13, createItem(Material.BARRIER, "§cUntrust Player"));
        inv.setItem(15, createItem(Material.BOOK, "§eAssign Roles"));
        inv.setItem(22, createItem(Material.BARRIER, "§cBack"));
        player.openInventory(inv);
    }

    public void openTrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dTrust Player");
        inv.setItem(13, createItem(Material.PAPER, "§aEnter player name to trust"));
        inv.setItem(22, createItem(Material.BARRIER, "§cBack"));
        player.openInventory(inv);
    }

    public void openUntrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dUntrust Player");
        inv.setItem(13, createItem(Material.PAPER, "§cEnter player name to untrust"));
        inv.setItem(22, createItem(Material.BARRIER, "§cBack"));
        player.openInventory(inv);
    }

    public void openTransferMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dTransfer Claim");
        inv.setItem(13, createItem(Material.BOOK, "§eEnter new owner name"));
        inv.setItem(22, createItem(Material.BARRIER, "§cBack"));
        player.openInventory(inv);
    }

    public void openAdmin(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dAdmin Menu");
        inv.setItem(11, createItem(Material.COMPASS, "§bClaim Info"));
        inv.setItem(13, createItem(Material.BARRIER, "§cDelete Claim"));
        inv.setItem(15, createItem(Material.EMERALD, "§aGive Claim Tool"));
        inv.setItem(22, createItem(Material.BARRIER, "§cBack"));
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * Helper
     * ------------------------------------------------------- */

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
