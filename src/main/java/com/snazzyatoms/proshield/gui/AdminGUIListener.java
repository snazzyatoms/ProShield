// src/main/java/com/snazzyatoms/proshield/gui/AdminGUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionRequestManager;
import com.snazzyatoms.proshield.plots.PlotManager;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class AdminGUIListener implements Listener {

    private final ProShield plugin;
    private final AdminGUIManager adminGUI;
    private final PlotManager plotManager;
    private final MessagesUtil messages;

    private ExpansionRequest selectedRequest; // Track currently selected request

    public AdminGUIListener(ProShield plugin, AdminGUIManager adminGUI) {
        this.plugin = plugin;
        this.adminGUI = adminGUI;
        this.plotManager = plugin.getPlotManager();
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        event.setCancelled(true); // Prevent item pickup
        String name = ChatColor.stripColor(meta.getDisplayName());

        // ------------------------
        // EXPANSION REQUESTS MENU
        // ------------------------
        if (title.contains("Expansion Requests")) {
            switch (name.toLowerCase()) {
                case "pending requests" -> {
                    if (!ExpansionRequestManager.hasRequests()) {
                        messages.send(player, "&7There are no pending requests.");
                    } else {
                        selectedRequest = ExpansionRequestManager.getRequests().get(0); // pick first for now
                        messages.send(player, "&eSelected request: " + selectedRequest.getPlayerId() +
                                " (+" + selectedRequest.getExtraRadius() + " blocks).");
                    }
                }
                case "approve selected" -> {
                    if (selectedRequest == null) {
                        messages.send(player, "&7No request selected.");
                        return;
                    }
                    UUID playerId = selectedRequest.getPlayerId();
                    plotManager.expandClaim(playerId, selectedRequest.getExtraRadius());
                    ExpansionRequestManager.removeRequest(selectedRequest);
                    messages.send(player, "&aApproved expansion for " + playerId +
                            " (+" + selectedRequest.getExtraRadius() + " blocks).");
                    selectedRequest = null;
                }
                case "deny selected" -> {
                    if (selectedRequest == null) {
                        messages.send(player, "&7No request selected.");
                        return;
                    }
                    // Open reason picker GUI
                    adminGUI.openMenu(player, "deny-reasons");
                }
                case "back" -> plugin.getGuiManager().openMenu(player, "main");
            }
        }

        // ------------------------
        // DENY REASONS MENU
        // ------------------------
        if (title.contains("Deny Reasons") && selectedRequest != null) {
            String reason;
            switch (name.toLowerCase()) {
                case "too large" -> reason = "Requested size too large.";
                case "abusive" -> reason = "Request considered abusive.";
                case "other" -> reason = "Denied by admin decision.";
                case "back" -> {
                    adminGUI.openMenu(player, "admin-expansions");
                    return;
                }
                default -> {
                    return;
                }
            }

            ExpansionRequestManager.removeRequest(selectedRequest);
            messages.send(player, "&cDenied expansion for " + selectedRequest.getPlayerId() +
                    ". Reason: &7" + reason);
            selectedRequest = null;

            // Return back to Expansion Requests after denial
            adminGUI.openMenu(player, "admin-expansions");
        }
    }
}
