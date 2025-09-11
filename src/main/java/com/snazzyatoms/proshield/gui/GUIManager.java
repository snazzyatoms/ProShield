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
import java.util.UUID;

/**
 * GUIManager
 *
 * Handles construction of both Player and Admin GUIs.
 * - Player menu: Claim flags, trust roles, etc.
 * - Admin menu: Force unclaim, teleport, purge, reload, debug, etc.
 * - Uses GUICache to track which menu is open (player/admin).
 */
public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* -------------------------
     * Open GUIs
     * ------------------------- */
    public void openFlagsMenu(Player player) {
        if (player.isOp() || player.hasPermission("proshield.admin")) {
            openAdminMenu(player); // OPs/Admins get admin menu by default
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + "Claim Flags");

        // Example claim flag buttons
        gui.setItem(10, makeItem(Material.DIAMOND_SWORD, "&cPvP", Arrays.asList("Toggle PvP in this claim")));
        gui.setItem(11, makeItem(Material.TNT, "&4Explosions", Arrays.asList("Toggle explosions in this claim")));
        gui.setItem(12, makeItem(Material.FLINT_AND_STEEL, "&6Fire", Arrays.asList("Toggle fire/ignite in this claim")));
        gui.setItem(13, makeItem(Material.REDSTONE, "&cRedstone", Arrays.asList("Toggle redstone in this claim")));
        gui.setItem(14, makeItem(Material.CHEST, "&6Containers", Arrays.asList("Toggle chest/container access")));
        gui.setItem(15, makeItem(Material.BUCKET, "&bBuckets", Arrays.asList("Toggle bucket use (water/lava)")));

        gui.setItem(16, makeItem(Material.LEAD, "&aAnimals", Arrays.asList("Toggle protection for animals")));
        gui.setItem(19, makeItem(Material.ARMOR_STAND, "&eArmor Stands", Arrays.asList("Toggle armor stand use")));
        gui.setItem(20, makeItem(Material.ITEM_FRAME, "&dItem Frames", Arrays.asList("Toggle item frame use")));

        gui.setItem(22, makeItem(Material.BARRIER, "&cClose", Arrays.asList("Close this menu")));

        // Track open menu
        cache.setOpenMenu(player.getUniqueId(), "player");
        player.openInventory(gui);
    }

    public void openAdminMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.RED + "ProShield Admin Tools");

        gui.setItem(10, makeItem(Material.NETHER_STAR, "&eToggle Bypass", Arrays.asList("Enable/disable bypass mode")));
        gui.setItem(11, makeItem(Material.BARRIER, "&cForce Unclaim", Arrays.asList("Force unclaim this land")));
        gui.setItem(12, makeItem(Material.COMPASS, "&bTeleport to Claim", Arrays.asList("Teleport to a player’s claim")));
        gui.setItem(13, makeItem(Material.CHEST_MINECART, "&6Transfer Claim", Arrays.asList("Transfer ownership of a claim")));
        gui.setItem(14, makeItem(Material.BOOK, "&aFlags", Arrays.asList("Open flags menu as admin")));

        gui.setItem(15, makeItem(Material.BONE, "&cPurge Expired", Arrays.asList("Purge expired claims")));
        gui.setItem(16, makeItem(Material.REPEATER, "&dDebug Toggle", Arrays.asList("Toggle debug mode")));
        gui.setItem(19, makeItem(Material.OAK_SIGN, "&eWilderness Messages", Arrays.asList("Enable/disable wilderness messages")));
        gui.setItem(22, makeItem(Material.BARRIER, "&cClose", Arrays.asList("Close this menu")));
        gui.setItem(25, makeItem(Material.PAPER, "&bReload", Arrays.asList("Reload ProShield config")));

        cache.setOpenMenu(player.getUniqueId(), "admin");
        player.openInventory(gui);
    }

    /* -------------------------
     * Helpers
     * ------------------------- */
    private ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore != null) {
                meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public void reopenMenu(UUID uuid, String type) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return;
        if ("player".equalsIgnoreCase(type)) {
            openFlagsMenu(p);
        } else if ("admin".equalsIgnoreCase(type)) {
            openAdminMenu(p);
        }
    }
}
