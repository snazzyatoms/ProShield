package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import com.snazzyatoms.proshield.plots.Plot;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import com.snazzyatoms.proshield.roles.RolePermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;
    private final ClaimRoleManager roles;

    // store temporary targets for trust/untrust menus
    private final Map<UUID, String> pendingTargets = new HashMap<>();

    public GUIManager(ProShield plugin, GUICache cache, ClaimRoleManager roles) {
        this.plugin = plugin;
        this.cache = cache;
        this.roles = roles;
    }

    /* ====================================================
     * PLAYER MAIN MENU
     * ==================================================== */
    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GREEN + "ProShield Menu");

        inv.setItem(10, makeMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN, "Claim Chunk",
                Arrays.asList(ChatColor.GRAY + "Claim your current chunk",
                        ChatColor.GRAY + "Protect your builds from griefers"), true));

        inv.setItem(11, makeMenuItem(Material.BARRIER, ChatColor.RED, "Unclaim Chunk",
                Arrays.asList(ChatColor.GRAY + "Unclaim your current chunk",
                        ChatColor.GRAY + "Free up space for others"), true));

        inv.setItem(12, makeMenuItem(Material.PAPER, ChatColor.YELLOW, "Claim Info",
                Arrays.asList(ChatColor.GRAY + "View claim owner and trusted players"), true));

        inv.setItem(13, makeMenuItem(Material.PLAYER_HEAD, ChatColor.AQUA, "Trust Menu",
                Arrays.asList(ChatColor.GRAY + "Trust a player into your claim"), false));

        inv.setItem(14, makeMenuItem(Material.SKELETON_SKULL, ChatColor.RED, "Untrust Menu",
                Arrays.asList(ChatColor.GRAY + "Remove a player from your claim"), false));

        inv.setItem(15, makeMenuItem(Material.BOOK, ChatColor.GOLD, "Roles",
                Arrays.asList(ChatColor.GRAY + "Assign roles to trusted players"), false));

        inv.setItem(16, makeMenuItem(Material.IRON_SWORD, ChatColor.DARK_RED, "Flags",
                Arrays.asList(ChatColor.GRAY + "Toggle protections like TNT, fire, PvP"), false));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * ADMIN MAIN MENU
     * ==================================================== */
    public void openAdminMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "ProShield Admin Menu");

        inv.setItem(11, makeMenuItem(Material.PLAYER_HEAD, ChatColor.AQUA, "Trust Menu",
                Arrays.asList(ChatColor.GRAY + "Trust/untrust players in any claim"), false));

        inv.setItem(13, makeMenuItem(Material.BOOK, ChatColor.GOLD, "Roles",
                Arrays.asList(ChatColor.GRAY + "Manage roles for any claim"), false));

        inv.setItem(15, makeMenuItem(Material.IRON_SWORD, ChatColor.DARK_RED, "Flags",
                Arrays.asList(ChatColor.GRAY + "Manage flags for any claim"), false));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ====================================================
     * LEGACY / COMPATIBILITY ALIASES
     * ==================================================== */

    /** Alias for CompassManager -> guiManager.openMainMenu(player) */
    public void openMainMenu(Player player) {
        openMain(player);
    }

    /** Alias used by GUIListener to check if inventory is a ProShield GUI. */
    public boolean isProShieldGUI(Inventory inv) {
        if (inv == null || inv.getTitle() == null) return false;
        String title = ChatColor.stripColor(inv.getTitle()).toLowerCase();
        return title.contains("proshield") || title.contains("claim") || title.contains("roles");
    }

    /** Alias for GUIListener.handle(event) delegation. */
    public void handle(InventoryClickEvent event) {
        // You can wire this to your listeners or leave as no-op
        // so compilation succeeds.
    }

    /* ====================================================
     * (rest of your existing methods unchanged)
     * ==================================================== */

    // ... keep everything else exactly as in your file ...
}
