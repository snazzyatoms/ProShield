package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * GUIManager
 *
 * ✅ Centralized creation + opening of Player & Admin GUIs
 * ✅ Fully wired with PlayerMenuListener & AdminMenuListener
 * ✅ Operator/Admins see Player GUI by default, but can toggle into Admin GUI
 * ✅ All items styled, consistent across versions 1.2.0 → 1.2.5
 */
public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* -------------------------------------------------------
     * PLAYER MENU
     * ------------------------------------------------------- */
    public void openPlayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.LIGHT_PURPLE + "ProShield Menu");

        inv.setItem(11, makeItem(Material.GRASS_BLOCK, "&aClaim Land", "&7Claim your current chunk"));
        inv.setItem(13, makeItem(Material.PAPER, "&bClaim Info", "&7View info about this claim"));
        inv.setItem(15, makeItem(Material.BARRIER, "&cUnclaim Land", "&7Remove your current claim"));

        inv.setItem(19, makeItem(Material.IRON_DOOR, "&6Trust Players", "&7Manage trusted players"));
        inv.setItem(21, makeItem(Material.CHEST, "&eFlags", "&7Open claim flags menu"));
        inv.setItem(23, makeItem(Material.ARMOR_STAND, "&dRoles", "&7Manage claim roles"));

        // Admin toggle item (if player is admin)
        if (player.hasPermission("proshield.admin")) {
            inv.setItem(26, makeItem(Material.NETHER_STAR, "&cAdmin Menu",
                    "&7Switch to admin tools & toggles"));
        }

        cache.setPlayerMenu(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * ADMIN MENU
     * ------------------------------------------------------- */
    public void openAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.RED + "ProShield Admin Menu");

        inv.setItem(10, makeItem(Material.COMMAND_BLOCK, "&cDebug Logging", "&7Toggle debug output"));
        inv.setItem(12, makeItem(Material.OAK_SIGN, "&aWilderness Messages", "&7Toggle wilderness entry msgs"));
        inv.setItem(14, makeItem(Material.REDSTONE_TORCH, "&6Admin Flag Chat", "&7Toggle admin flag chat msgs"));

        inv.setItem(19, makeItem(Material.ANVIL, "&eForce Unclaim", "&7Forcefully remove a claim"));
        inv.setItem(21, makeItem(Material.PLAYER_HEAD, "&bTransfer Claim", "&7Transfer claim ownership"));
        inv.setItem(23, makeItem(Material.ENDER_PEARL, "&dTeleport to Claim", "&7Jump to a player's claim"));
        inv.setItem(25, makeItem(Material.LAVA_BUCKET, "&4Purge Expired Claims", "&7Remove old/abandoned claims"));

        // Back to player menu
        inv.setItem(26, makeItem(Material.ARROW, "&7Back to Player Menu", "&7Return to regular ProShield menu"));

        cache.setAdminMenu(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    /* -------------------------------------------------------
     * HELPERS
     * ------------------------------------------------------- */
    private ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> loreList = Arrays.stream(lore)
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .toList();
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }
}
