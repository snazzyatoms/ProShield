package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AdminGUIManager
 *
 * ✅ Builds the admin menus
 * ✅ Handles Expansion Requests view
 * ✅ Integrates with ExpansionRequestManager
 */
public class AdminGUIManager {

    private final ProShield plugin;

    public AdminGUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the main Admin menu
     */
    public void openAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "Admin Menu");

        inv.setItem(11, makeItem(Material.PAPER, "&eExpansion Requests",
                "&7View and manage all player requests"));

        inv.setItem(15, makeItem(Material.BARRIER, "&cBack",
                "&7Return to ProShield Main Menu"));

        player.openInventory(inv);
    }

    /**
     * Opens the Expansion Requests menu
     */
    public void openExpansionRequestsMenu(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + "Expansion Requests");

        List<ExpansionRequest> requests = ExpansionRequestManager.getRequests();
        if (requests.isEmpty()) {
            inv.setItem(22, makeItem(Material.BOOK, "&7No Pending Requests",
                    "&fPlayers can request expansions via their GUI."));
        } else {
            int slot = 0;
            for (ExpansionRequest req : requests) {
                if (slot >= 45) break; // avoid overflow

                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW + "Request from " +
                        Bukkit.getOfflinePlayer(req.getPlayerId()).getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Extra Radius: +" + req.getExtraRadius() + " blocks");
                lore.add(ChatColor.GRAY + "Requested at: " + req.getRequestTime());
                lore.add("");
                lore.add(ChatColor.GREEN + "Click to select this request");
                meta.setLore(lore);
                paper.setItemMeta(meta);

                inv.setItem(slot++, paper);
            }
        }

        // Approve/Deny/Back buttons
        inv.setItem(46, makeItem(Material.EMERALD, "&aApprove Selected",
                "&7Approve and apply instantly (if enabled)"));
        inv.setItem(47, makeItem(Material.REDSTONE, "&cDeny Selected",
                "&7Deny and enter reason"));
        inv.setItem(53, makeItem(Material.BARRIER, "&cBack",
                "&7Return to Admin Menu"));

        // Teaser
        inv.setItem(49, makeItem(Material.BOOK, "&dComing in 2.0",
                "&7Expansion via currency/permissions",
                "&7Automatic upgrades", "&7VIP claim bonuses"));

        admin.openInventory(inv);
    }

    // Utility to build GUI items
    private ItemStack makeItem(Material mat, String name, String... loreText) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = new ArrayList<>();
        for (String line : loreText) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }
}
