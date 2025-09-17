package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansions.ExpansionRequest;
import com.snazzyatoms.proshield.expansions.ExpansionRequestManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class AdminPendingRequestsGUI {

    private final ProShield plugin;
    private final ExpansionRequestManager requestManager;

    public AdminPendingRequestsGUI(ProShield plugin) {
        this.plugin = plugin;
        this.requestManager = plugin.getExpansionRequestManager();
    }

    /**
     * Open the GUI for an admin, showing all pending requests.
     */
    public void open(Player admin) {
        List<ExpansionRequest> pending = requestManager.getPendingRequests();

        int size = ((pending.size() / 9) + 1) * 9;
        if (size > 54) size = 54; // limit to 6 rows

        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_GREEN + "Pending Expansion Requests");

        int slot = 0;
        for (ExpansionRequest req : pending) {
            if (slot >= size) break;

            OfflinePlayer requester = requestManager.getOfflinePlayer(req.getRequester());

            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + requester.getName() + ChatColor.GRAY + " → " + req.getAmount());
            meta.setLore(List.of(
                    ChatColor.GRAY + "Requested: " + req.getAmount(),
                    ChatColor.GRAY + "At: " + req.getTimestamp(),
                    ChatColor.GRAY + "Status: " + req.getStatus(),
                    ChatColor.GREEN + "Left-click: Approve",
                    ChatColor.RED + "Right-click: Deny"
            ));
            paper.setItemMeta(meta);

            gui.setItem(slot, paper);
            slot++;
        }

        admin.openInventory(gui);
    }

    /**
     * Handle clicks inside the GUI.
     */
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        Inventory inv = event.getInventory();
        if (!ChatColor.stripColor(inv.getTitle()).equalsIgnoreCase("Pending Expansion Requests")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).split(" ")[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        List<ExpansionRequest> pending = requestManager.getPendingRequestsFor(target.getUniqueId());
        if (pending.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No pending requests found for " + name);
            return;
        }

        ExpansionRequest req = pending.get(0); // pick the first pending one

        switch (event.getClick()) {
            case LEFT -> {
                requestManager.approveRequest(req, player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Approved request for " + name);
                player.closeInventory();
            }
            case RIGHT -> {
                requestManager.denyRequest(req, player.getUniqueId(), "Denied by admin");
                player.sendMessage(ChatColor.RED + "Denied request for " + name);
                player.closeInventory();
            }
        }
    }
}
