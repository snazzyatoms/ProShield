package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class AdminGUIManager {

    private final ProShield plugin;
    private final ExpansionRequestManager requestManager;

    public AdminGUIManager(ProShield plugin, ExpansionRequestManager requestManager) {
        this.plugin = plugin;
        this.requestManager = requestManager;
    }

    /**
     * Open the main Expansion Requests menu
     */
    public void openExpansionMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§cExpansion Requests");

        inv.setItem(11, createItem(Material.PAPER, "§ePending Requests",
                List.of("§7View and manage all player requests")));

        inv.setItem(13, createItem(Material.EMERALD, "§aApprove Selected",
                List.of("§7Approve and apply instantly (if enabled)", "§7Reason optional")));

        inv.setItem(15, createItem(Material.REDSTONE, "§cDeny Selected",
                List.of("§7Deny with reason")));

        inv.setItem(22, createItem(Material.BOOK, "§dComing in 2.0",
                List.of("§7Expansion via currency/permissions", "§7Automatic upgrades", "§7VIP claim bonuses")));

        // Back button to Admin Menu
        inv.setItem(26, createItem(Material.BARRIER, "§cBack",
                List.of("§7Return to Admin Menu")));

        player.openInventory(inv);
    }

    /**
     * Open the pending requests list
     */
    public void openRequestsList(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§ePending Expansion Requests");

        int slot = 0;
        for (ExpansionRequest req : requestManager.getPendingRequests()) {
            if (slot >= inv.getSize()) break;

            UUID pid = req.getPlayerId();
            String name = Bukkit.getOfflinePlayer(pid).getName();
            inv.setItem(slot++, createItem(Material.PAPER, "§b" + name,
                    List.of("§7Requested +" + req.getExtraRadius() + " blocks",
                            "§7At: " + req.getRequestTime())));
        }

        // Back button
        inv.setItem(53, createItem(Material.BARRIER, "§cBack",
                List.of("§7Return to Expansion Menu")));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
