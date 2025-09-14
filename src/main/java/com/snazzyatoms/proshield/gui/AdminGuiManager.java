// src/main/java/com/snazzyatoms/proshield/gui/AdminGUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AdminGUIManager {

    private final ProShield plugin;

    public AdminGUIManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the Expansion Requests menu for admins.
     */
    public void openExpansionMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                ChatColor.translateAlternateColorCodes('&', "&cExpansion Requests"));

        // Pending Requests
        ItemStack pending = makeItem(Material.PAPER, "&ePending Requests",
                "&7View and manage all player requests");
        inv.setItem(11, pending);

        // Approve
        ItemStack approve = makeItem(Material.EMERALD, "&aApprove Selected",
                "&7Approve and apply instantly (if enabled)",
                "&7Reason optional");
        inv.setItem(13, approve);

        // Deny
        ItemStack deny = makeItem(Material.REDSTONE, "&cDeny Selected",
                "&7Deny with reason");
        inv.setItem(15, deny);

        // Coming Soon
        ItemStack teaser = makeItem(Material.BOOK, "&dComing in 2.0",
                "&7Expansion via currency/permissions",
                "&7Automatic upgrades",
                "&7VIP claim bonuses");
        inv.setItem(22, teaser);

        // Back
        ItemStack back = makeItem(Material.BARRIER, "&cBack",
                "&7Return to Admin Menu");
        inv.setItem(26, back);

        player.openInventory(inv);
    }

    private ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(Arrays.stream(lore)
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .toList());
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
