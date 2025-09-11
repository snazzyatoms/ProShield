package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRole;
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
 * ✅ Central place for opening ProShield GUIs.
 * ✅ Handles both player & admin menus.
 * ✅ All methods used by commands/listeners exist here:
 *    - openMain, openFlagsMenu, openRolesMenu, openTransferMenu
 *    - openTrustMenu, openUntrustMenu
 */
public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* -------------------------------------------------------
     * MAIN MENU
     * ------------------------------------------------------- */
    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§aProShield Menu");

        inv.setItem(10, createItem(Material.BOOK, "§eFlags", "§7Manage your claim flags"));
        inv.setItem(12, createItem(Material.PLAYER_HEAD, "§bTrust", "§7Trust a player into your claim"));
        inv.setItem(14, createItem(Material.BARRIER, "§cUntrust", "§7Remove a trusted player"));
        inv.setItem(16, createItem(Material.PAPER, "§dRoles", "§7Assign roles to trusted players"));
        inv.setItem(22, createItem(Material.CHEST, "§6Transfer", "§7Transfer claim ownership"));

        cache.setPlayerMenu(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * FLAGS MENU
     * ------------------------------------------------------- */
    public void openFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dClaim Flags");

        inv.setItem(11, createItem(Material.IRON_SWORD, "§cPvP", "§7Toggle PvP inside claim"));
        inv.setItem(13, createItem(Material.TNT, "§cExplosions", "§7Allow or block TNT/creeper explosions"));
        inv.setItem(15, createItem(Material.FLINT_AND_STEEL, "§6Fire", "§7Allow or block fire spread/ignite"));

        cache.setPlayerMenu(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * ROLES MENU
     * ------------------------------------------------------- */
    public void openRolesMenu(Player player, Plot plot) {
        Inventory inv = Bukkit.createInventory(null, 27, "§bClaim Roles");

        inv.setItem(10, createItem(Material.WOODEN_PICKAXE, "§7Builder", "§7Trusted player can build"));
        inv.setItem(12, createItem(Material.CHEST, "§6Container Access", "§7Open/use containers"));
        inv.setItem(14, createItem(Material.IRON_DOOR, "§aRedstone Access", "§7Use buttons/levers/doors"));
        inv.setItem(16, createItem(Material.NETHER_STAR, "§dAdmin Role", "§7Full trusted access"));

        cache.setPlayerMenu(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * TRANSFER MENU
     * ------------------------------------------------------- */
    public void openTransferMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "§6Transfer Claim");

        inv.setItem(4, createItem(Material.NAME_TAG, "§eTransfer Ownership",
                "§7Transfer this claim to another player"));

        cache.setPlayerMenu(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * TRUST MENU
     * ------------------------------------------------------- */
    public void openTrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "§bTrust Player");

        inv.setItem(4, createItem(Material.PLAYER_HEAD, "§aTrust Player",
                "§7Trust someone into your claim"));

        cache.setPlayerMenu(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * UNTRUST MENU
     * ------------------------------------------------------- */
    public void openUntrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "§cUntrust Player");

        inv.setItem(4, createItem(Material.BARRIER, "§cUntrust Player",
                "§7Remove someone from your trusted list"));

        cache.setPlayerMenu(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * HELPERS
     * ------------------------------------------------------- */
    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public GUICache getCache() {
        return cache;
    }
}
