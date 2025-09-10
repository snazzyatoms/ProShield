package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * GUIManager builds and manages all ProShield inventory menus.
 * Uses GUICache for per-player menu caching.
 */
public class GUIManager {

    private final ProShield plugin;
    private final PlotManager plots;
    private final GUICache cache;

    public GUIManager(ProShield plugin, PlotManager plots, GUICache cache) {
        this.plugin = plugin;
        this.plots = plots;
        this.cache = cache;
    }

    /* -----------------------------------------------------
     * Utility
     * --------------------------------------------------- */

    private ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private Inventory buildMenu(Player player, GUICache.MenuType type) {
        switch (type) {
            case MAIN: return buildMainMenu(player);
            case TRUST: return buildTrustMenu(player);
            case UNTRUST: return buildUntrustMenu(player);
            case ROLES: return buildRolesMenu(player);
            case FLAGS: return buildFlagsMenu(player);
            case TRANSFER: return buildTransferMenu(player);
            case ADMIN: return buildAdminMenu(player);
            default: return null;
        }
    }

    private Inventory getOrBuild(Player player, GUICache.MenuType type) {
        Inventory inv = cache.get(player, type);
        if (inv == null) {
            inv = buildMenu(player, type);
            cache.put(player, type, inv);
        }
        return inv;
    }

    /* -----------------------------------------------------
     * Main Player Menu
     * --------------------------------------------------- */

    private Inventory buildMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.AQUA + "ProShield");

        inv.setItem(11, makeItem(Material.GRASS_BLOCK, "&aClaim Land", "&7Claim current chunk"));
        inv.setItem(13, makeItem(Material.PAPER, "&bClaim Info", "&7View details of this claim"));
        inv.setItem(15, makeItem(Material.BARRIER, "&cUnclaim Land", "&7Unclaim current chunk"));

        // New player tools
        inv.setItem(20, makeItem(Material.PLAYER_HEAD, "&eTrust Player", "&7Add a trusted player"));
        inv.setItem(21, makeItem(Material.SKELETON_SKULL, "&eUntrust Player", "&7Remove a trusted player"));
        inv.setItem(22, makeItem(Material.BOOK, "&eRoles", "&7Manage claim roles"));
        inv.setItem(23, makeItem(Material.REDSTONE_TORCH, "&eClaim Flags", "&7Toggle claim settings"));
        inv.setItem(24, makeItem(Material.NAME_TAG, "&eTransfer Ownership", "&7Give claim to another player"));

        inv.setItem(31, makeItem(Material.COMPASS, "&fHelp", "&7View help and commands"));
        inv.setItem(33, makeItem(Material.NETHER_STAR, "&cAdmin Menu", "&7Admins only"));

        return inv;
    }

    /* -----------------------------------------------------
     * Trust / Untrust Menus
     * --------------------------------------------------- */

    private Inventory buildTrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Trust a Player");
        inv.setItem(13, makeItem(Material.PLAYER_HEAD, "&aEnter Player Name", "&7Type in chat to trust"));
        inv.setItem(22, makeItem(Material.ARROW, "&7Back"));
        return inv;
    }

    private Inventory buildUntrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "Untrust a Player");
        inv.setItem(13, makeItem(Material.SKELETON_SKULL, "&cEnter Player Name", "&7Type in chat to untrust"));
        inv.setItem(22, makeItem(Material.ARROW, "&7Back"));
        return inv;
    }

    /* -----------------------------------------------------
     * Roles Menu
     * --------------------------------------------------- */

    private Inventory buildRolesMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Manage Roles");
        inv.setItem(10, makeItem(Material.GRAY_DYE, "&7Visitor", "&7No interaction"));
        inv.setItem(11, makeItem(Material.IRON_SWORD, "&fMember", "&7Basic interaction"));
        inv.setItem(12, makeItem(Material.CHEST, "&eContainer", "&7Access containers"));
        inv.setItem(13, makeItem(Material.BRICKS, "&aBuilder", "&7Can build and break"));
        inv.setItem(14, makeItem(Material.DIAMOND, "&bCo-Owner", "&7Full control"));
        inv.setItem(22, makeItem(Material.ARROW, "&7Back"));
        return inv;
    }

    /* -----------------------------------------------------
     * Flags Menu
     * --------------------------------------------------- */

    private Inventory buildFlagsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "Claim Flags");
        inv.setItem(10, makeItem(Material.IRON_SWORD, "&cPvP", "&7Toggle PvP in claims"));
        inv.setItem(11, makeItem(Material.TNT, "&cExplosions", "&7Toggle TNT & Creeper damage"));
        inv.setItem(12, makeItem(Material.FLINT_AND_STEEL, "&cFire", "&7Toggle fire spread"));
        inv.setItem(13, makeItem(Material.SHEARS, "&cAnimal Interactions", "&7Toggle animal protection"));
        inv.setItem(14, makeItem(Material.REDSTONE, "&cRedstone", "&7Toggle redstone use"));
        inv.setItem(22, makeItem(Material.ARROW, "&7Back"));
        return inv;
    }

    /* -----------------------------------------------------
     * Transfer Ownership Menu
     * --------------------------------------------------- */

    private Inventory buildTransferMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Transfer Ownership");
        inv.setItem(13, makeItem(Material.NAME_TAG, "&eEnter Player Name", "&7Type in chat to transfer ownership"));
        inv.setItem(22, makeItem(Material.ARROW, "&7Back"));
        return inv;
    }

    /* -----------------------------------------------------
     * Admin Menu
     * --------------------------------------------------- */

    private Inventory buildAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + "Admin Menu");

        inv.setItem(10, makeItem(Material.FLINT_AND_STEEL, "&cFire Toggle"));
        inv.setItem(11, makeItem(Material.TNT, "&cExplosions Toggle"));
        inv.setItem(12, makeItem(Material.ENDER_PEARL, "&cEntity Grief Toggle"));
        inv.setItem(13, makeItem(Material.LEVER, "&cInteractions Toggle"));
        inv.setItem(14, makeItem(Material.IRON_SWORD, "&cPvP Toggle"));

        inv.setItem(20, makeItem(Material.CHEST, "&cKeep Items Toggle"));
        inv.setItem(21, makeItem(Material.BONE, "&cPurge Expired Claims"));
        inv.setItem(22, makeItem(Material.BOOK, "&cHelp"));
        inv.setItem(23, makeItem(Material.REDSTONE_TORCH, "&cDebug Toggle"));
        inv.setItem(24, makeItem(Material.COMPASS, "&cCompass Drop Setting"));

        inv.setItem(25, makeItem(Material.BARRIER, "&cReload Config"));
        inv.setItem(30, makeItem(Material.ENDER_EYE, "&cTeleport Tools"));
        inv.setItem(31, makeItem(Material.ARROW, "&7Back"));

        return inv;
    }

    /* -----------------------------------------------------
     * Openers
     * --------------------------------------------------- */

    public void openMain(Player player) {
        player.openInventory(getOrBuild(player, GUICache.MenuType.MAIN));
    }

    public void openTrust(Player player) {
        player.openInventory(getOrBuild(player, GUICache.MenuType.TRUST));
    }

    public void openUntrust(Player player) {
        player.openInventory(getOrBuild(player, GUICache.MenuType.UNTRUST));
    }

    public void openRoles(Player player) {
        player.openInventory(getOrBuild(player, GUICache.MenuType.ROLES));
    }

    public void openFlags(Player player) {
        player.openInventory(getOrBuild(player, GUICache.MenuType.FLAGS));
    }

    public void openTransfer(Player player) {
        player.openInventory(getOrBuild(player, GUICache.MenuType.TRANSFER));
    }

    public void openAdmin(Player player) {
        player.openInventory(getOrBuild(player, GUICache.MenuType.ADMIN));
    }

    /* -----------------------------------------------------
     * Cache Hooks
     * --------------------------------------------------- */

    public void clearPlayerCache(Player player) {
        cache.clear(player);
    }

    public void clearAllCache() {
        cache.clearAll();
    }
}
