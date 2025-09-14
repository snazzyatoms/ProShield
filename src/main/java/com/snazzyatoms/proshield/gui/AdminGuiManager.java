// src/main/java/com/snazzyatoms/proshield/gui/AdminGUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.expansion.ExpansionQueue;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AdminGUIManager {

    private final ProShield plugin;
    private final MessagesUtil messages;

    public AdminGUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesUtil();
    }

    /**
     * Open the admin expansion requests menu.
     */
    public void openExpansionRequests(Player admin) {
        List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();

        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + "Expansion Requests");

        if (pending.isEmpty()) {
            ItemStack info = new ItemStack(Material.BARRIER);
            ItemMeta meta = info.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "No Pending Requests");
            info.setItemMeta(meta);
            inv.setItem(22, info);
        } else {
            int slot = 0;
            for (ExpansionRequest req : pending) {
                if (slot >= inv.getSize()) break;

                ItemStack paper = new ItemStack(Material.PAPER);
                ItemMeta meta = paper.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + "Request: " + Bukkit.getOfflinePlayer(req.getPlayerId()).getName());

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Extra Radius: " + ChatColor.WHITE + req.getExtraRadius() + " blocks");
                lore.add(ChatColor.GRAY + "Status: " + ChatColor.WHITE + req.getStatus());
                if (req.getReason() != null) {
                    lore.add(ChatColor.RED + "Reason: " + req.getReason());
                }
                lore.add("");
                lore.add(ChatColor.GREEN + "▶ Left-click: Approve");
                lore.add(ChatColor.RED + "▶ Right-click: Deny");

                meta.setLore(lore);
                paper.setItemMeta(meta);

                inv.setItem(slot++, paper);
            }
        }

        admin.openInventory(inv);
    }
}
