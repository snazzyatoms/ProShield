package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GUIManager {

    private final ProShield plugin;
    private final GUICache cache;

    public GUIManager(ProShield plugin, GUICache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    /* ----------------------------------------
     * Helper to build menu items
     * ---------------------------------------- */
    private ItemStack makeMenuItem(Material mat, ChatColor color, String name, List<String> lore, boolean hideAttributes) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + name);
        meta.setLore(lore);
        if (hideAttributes) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        item.setItemMeta(meta);
        return item;
    }

    /* ----------------------------------------
     * Player Main Menu
     * ---------------------------------------- */
    public void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + "ProShield Menu");

        inv.setItem(10, makeMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN, "Claim Chunk",
                Arrays.asList(ChatColor.GRAY + "Claim your current chunk",
                        ChatColor.GRAY + "Protect your builds safely."), false));

        inv.setItem(11, makeMenuItem(Material.DIRT, ChatColor.RED, "Unclaim Chunk",
                Arrays.asList(ChatColor.GRAY + "Remove your claim",
                        ChatColor.GRAY + "and free the land."), false));

        inv.setItem(12, makeMenuItem(Material.BOOK, ChatColor.YELLOW, "Claim Info",
                Arrays.asList(ChatColor.GRAY + "View claim owner &",
                        ChatColor.GRAY + "trusted players."), false));

        inv.setItem(14, makeMenuItem(Material.WRITABLE_BOOK, ChatColor.GREEN, "Trust Menu",
                Arrays.asList(ChatColor.GRAY + "Trust a player",
                        ChatColor.GRAY + "and grant them access."), false));

        inv.setItem(15, makeMenuItem(Material.PAPER, ChatColor.RED, "Untrust Menu",
                Arrays.asList(ChatColor.GRAY + "Remove a player",
                        ChatColor.GRAY + "from trusted list."), false));

        inv.setItem(16, makeMenuItem(Material.IRON_PICKAXE, ChatColor.GOLD, "Roles",
                Arrays.asList(ChatColor.GRAY + "Assign roles like",
                        ChatColor.GRAY + "Builder or Moderator."), true));

        inv.setItem(22, makeMenuItem(Material.IRON_SWORD, ChatColor.AQUA, "Flags",
                Arrays.asList(ChatColor.GRAY + "Toggle protections like",
                        ChatColor.GRAY + "explosions, PvP, fire, etc."), true));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ----------------------------------------
     * Admin Main Menu
     * ---------------------------------------- */
    public void openAdminMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "ProShield Admin Menu");

        inv.setItem(10, makeMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN, "Force Claim",
                Arrays.asList(ChatColor.GRAY + "Claim the current chunk",
                        ChatColor.GRAY + "on behalf of another player."), false));

        inv.setItem(11, makeMenuItem(Material.DIRT, ChatColor.RED, "Force Unclaim",
                Arrays.asList(ChatColor.GRAY + "Remove a player’s claim",
                        ChatColor.GRAY + "from this chunk."), false));

        inv.setItem(12, makeMenuItem(Material.BOOK, ChatColor.YELLOW, "Claim Info",
                Arrays.asList(ChatColor.GRAY + "View owner, trusted,",
                        ChatColor.GRAY + "and claim settings."), false));

        inv.setItem(14, makeMenuItem(Material.WRITABLE_BOOK, ChatColor.GREEN, "Trust Menu",
                Arrays.asList(ChatColor.GRAY + "Admin-manage trust",
                        ChatColor.GRAY + "for this claim."), false));

        inv.setItem(15, makeMenuItem(Material.PAPER, ChatColor.RED, "Untrust Menu",
                Arrays.asList(ChatColor.GRAY + "Admin-remove players",
                        ChatColor.GRAY + "from trusted list."), false));

        inv.setItem(16, makeMenuItem(Material.IRON_PICKAXE, ChatColor.GOLD, "Roles",
                Arrays.asList(ChatColor.GRAY + "Manage player roles",
                        ChatColor.GRAY + "in this claim."), true));

        inv.setItem(22, makeMenuItem(Material.IRON_SWORD, ChatColor.AQUA, "Flags",
                Arrays.asList(ChatColor.GRAY + "Admin toggle protections:",
                        ChatColor.GRAY + "explosions, PvP, containers, etc."), true));

        inv.setItem(26, makeMenuItem(Material.BARRIER, ChatColor.DARK_RED, "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to previous menu"), false));

        player.openInventory(inv);
    }

    /* ----------------------------------------
     * TODO: Trust, Untrust, Roles, Flags
     * These will open sub-menus (already drafted)
     * ---------------------------------------- */

    public void openTrustMenu(Player player, boolean fromAdmin) {
        // same structure, PLAYER_HEADs for player selection, etc.
    }

    public void openUntrustMenu(Player player, boolean fromAdmin) {
        // same structure
    }

    public void openRolesGUI(Player player, Object plot, boolean fromAdmin) {
        // same structure
    }

    public void openFlagsMenu(Player player, boolean fromAdmin) {
        // dynamic lore with ENABLED/DISABLED states
    }
}
