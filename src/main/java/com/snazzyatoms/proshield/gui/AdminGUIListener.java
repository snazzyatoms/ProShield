package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminGUIListener implements Listener {

    private final ProShield plugin;
    private final AdminGUIManager adminGUIManager;
    private final ExpansionRequestManager requestManager;
    private final MessagesUtil messages;

    public AdminGUIListener(ProShield plugin, AdminGUIManager adminGUIManager,
                            ExpansionRequestManager requestManager, MessagesUtil messages) {
        this.plugin = plugin;
        this.adminGUIManager = adminGUIManager;
        this.requestManager = requestManager;
        this.messages = messages;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        ItemStack item = event.getCurrentItem();
        if (!event.getView().getTitle().contains("Expansion Requests")) return;

        event.setCancelled(true);

        // Check if item is valid
        if (item.getType() != Material.PAPER) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        // Get player name from item
        String displayName = meta.getDisplayName().replace("§eRequest: §f", "");
        ExpansionRequest req = requestManager.findByName(displayName);

        if (req == null) {
            messages.send(player, "&cRequest not found.");
            return;
        }

        switch (event.getClick()) {
            case LEFT -> {
                // Approve
                adminGUIManager.approveRequest(player, req);
                player.closeInventory();
            }
            case RIGHT -> {
                // Deny with a default reason
                String reason = plugin.getConfig().getString("messages.default-deny-reason", "No reason given");
                adminGUIManager.denyRequest(player, req, reason);
                player.closeInventory();
            }
            default -> {
                // Do nothing for other clicks
            }
        }
    }
}
