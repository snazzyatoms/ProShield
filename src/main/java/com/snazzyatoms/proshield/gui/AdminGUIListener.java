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
                        messages.send(player, "&ePending requests:");
                        for (ExpansionRequest req : ExpansionRequestManager.getRequests()) {
                            messages.send(player, "&7- " + req.getPlayerId() +
                                    " wants +" + req.getExtraRadius() + " blocks.");
                        }
                    }
                }
                case "approve selected" -> {
                    if (!ExpansionRequestManager.hasRequests()) {
                        messages.send(player, "&7No requests to approve.");
                        return;
                    }

                    ExpansionRequest req = ExpansionRequestManager.getRequests().get(0); // ✅ Simplified: first request
                    UUID playerId = req.getPlayerId();
                    plotManager.expandClaim(playerId, req.getExtraRadius());

                    ExpansionRequestManager.removeRequest(req);
                    messages.send(player, "&aApproved expansion for " + playerId +
                            " (+" + req.getExtraRadius() + " blocks).");
                }
                case "deny selected" -> {
                    if (!ExpansionRequestManager.hasRequests()) {
                        messages.send(player, "&7No requests to deny.");
                        return;
                    }

                    ExpansionRequest req = ExpansionRequestManager.getRequests().get(0);
                    ExpansionRequestManager.removeRequest(req);
                    messages.send(player, "&cDenied expansion for " + req.getPlayerId() +
                            ". Reason: &7Too large / not allowed.");
                }
                case "back" -> {
                    // Go back to Admin Menu (main GUI for admins)
                    plugin.getGuiManager().openMenu(player, "main");
                }
            }
        }
    }
}
