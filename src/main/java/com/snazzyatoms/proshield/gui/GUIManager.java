package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
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

    /* -------------------
     * Utility
     * ------------------- */
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    /* -------------------
     * Player GUIs
     * ------------------- */
    public void openMain(Player player) {
        Inventory inv = cache.getMain(player.getUniqueId());
        if (inv == null) {
            inv = Bukkit.createInventory(player, 54, "🛡️ ProShield Menu");
            inv.setItem(11, createItem(Material.GRASS_BLOCK, "§aClaim Chunk", "Protect your land"));
            inv.setItem(13, createItem(Material.PAPER, "§eClaim Info", "View claim owner & trusted"));
            inv.setItem(15, createItem(Material.BARRIER, "§cUnclaim Chunk", "Remove protection"));
            inv.setItem(20, createItem(Material.PLAYER_HEAD, "§bTrust Player", "Grant access"));
            inv.setItem(21, createItem(Material.SKELETON_SKULL, "§cUntrust Player", "Revoke access"));
            inv.setItem(22, createItem(Material.BOOK, "§6Manage Roles", "Assign claim roles"));
            inv.setItem(23, createItem(Material.REDSTONE_TORCH, "§dClaim Flags", "Toggle protections"));
            inv.setItem(24, createItem(Material.WRITABLE_BOOK, "§9Transfer Claim", "Give ownership to another"));
            inv.setItem(31, createItem(Material.COMPASS, "§fHelp", "Commands & info"));
            if (player.hasPermission("proshield.admin")) {
                inv.setItem(33, createItem(Material.NETHER_STAR, "§cAdmin Menu", "Open admin tools"));
            }
            cache.cacheMain(player.getUniqueId(), inv);
        }
        player.openInventory(inv);
    }

    public void openTrust(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, "🤝 Trust Player");
        inv.setItem(11, createItem(Material.PLAYER_HEAD, "§aAdd Trusted", "Trust a player here"));
        inv.setItem(15, createItem(Material.BARRIER, "§cBack", "Return to main"));
        cache.cacheTrust(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    public void openRoles(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, "📜 Manage Roles");
        inv.setItem(10, createItem(Material.MAP, "§fVisitor", "Walk only"));
        inv.setItem(11, createItem(Material.WOODEN_SWORD, "§aMember", "Basic interaction"));
        inv.setItem(12, createItem(Material.CHEST, "§bContainer", "Use chests/furnaces"));
        inv.setItem(13, createItem(Material.STONE, "§eBuilder", "Build & break"));
        inv.setItem(14, createItem(Material.NETHER_STAR, "§dCo-Owner", "Full access"));
        inv.setItem(18, createItem(Material.BARRIER, "§cBack", "Return to main"));
        cache.cacheRole(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    public void openFlags(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, "⚑ Claim Flags");
        inv.setItem(10, createItem(Material.DIAMOND_SWORD, "§cPvP", "Toggle PvP in claims"));
        inv.setItem(11, createItem(Material.TNT, "§6Explosions", "Toggle TNT & creepers"));
        inv.setItem(12, createItem(Material.FLINT_AND_STEEL, "§4Fire", "Toggle fire spread"));
        inv.setItem(13, createItem(Material.COW_SPAWN_EGG, "§aAnimals", "Toggle mob/animal interactions"));
        inv.setItem(14, createItem(Material.REDSTONE, "§cRedstone", "Toggle redstone mechanics"));
        inv.setItem(18, createItem(Material.BARRIER, "§cBack", "Return to main"));
        cache.cacheFlag(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    public void openTransfer(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, "🔑 Transfer Claim");
        inv.setItem(11, createItem(Material.WRITABLE_BOOK, "§9Transfer Ownership", "Choose player to transfer to"));
        inv.setItem(15, createItem(Material.BARRIER, "§cBack", "Return to main"));
        cache.cacheTransfer(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------
     * Admin GUI
     * ------------------- */
    public void openAdmin(Player player) {
        Inventory inv = cache.getAdmin(player.getUniqueId());
        if (inv == null) {
            inv = Bukkit.createInventory(player, 54, "⚒️ ProShield Admin Menu");
            inv.setItem(10, createItem(Material.FLINT_AND_STEEL, "§cToggle Fire", "Enable/disable fire spread"));
            inv.setItem(11, createItem(Material.TNT, "§6Toggle Explosions", "Enable/disable explosions"));
            inv.setItem(12, createItem(Material.ENDER_EYE, "§5Toggle Entity Grief", "Stop Endermen, Ravagers, etc."));
            inv.setItem(13, createItem(Material.REDSTONE, "§cToggle Interactions", "Doors, buttons, levers"));
            inv.setItem(14, createItem(Material.DIAMOND_SWORD, "§4Toggle PvP", "Enable/disable PvP in claims"));
            inv.setItem(20, createItem(Material.CHEST, "§bToggle Keep Items", "Keep drops in claims"));
            inv.setItem(21, createItem(Material.BOOK, "§ePurge Expired", "Remove inactive claims"));
            inv.setItem(22, createItem(Material.BOOKSHELF, "§fHelp", "Show admin help"));
            inv.setItem(23, createItem(Material.COMMAND_BLOCK, "§dDebug", "Enable/disable debug mode"));
            inv.setItem(24, createItem(Material.COMPASS, "§fCompass Policy", "Drop if full"));
            inv.setItem(25, createItem(Material.BEACON, "§bReload Configs", "Reload plugin settings"));
            inv.setItem(30, createItem(Material.ENDER_PEARL, "§aTeleport Tools", "Jump to claims"));
            inv.setItem(31, createItem(Material.BARRIER, "§cBack", "Return to main"));
            cache.cacheAdmin(player.getUniqueId(), inv);
        }
        player.openInventory(inv);
    }
}
