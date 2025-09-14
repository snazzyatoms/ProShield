// src/main/java/com/snazzyatoms/proshield/gui/AdminGUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.expansion.ExpansionQueue;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AdminGUIListener implements Listener {

    private final GUIManager guiManager;

    public AdminGUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        if (title.contains("Expansion Requests")) {
            event.setCancelled(true);

            List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();
            if (pending.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "No pending requests.");
                return;
            }

            ExpansionRequest req = pending.get(0); // always pick the first
            UUID targetId = req.getPlayerId();

            switch (name.toLowerCase(Locale.ROOT)) {
                case "approve selected" -> {
                    // Directly run the approve command with UUID
                    Bukkit.dispatchCommand(player, "proshield approve " + targetId);
                }
                case "deny selected" -> {
                    // Open the deny reasons GUI (handled in GUIManager)
                    guiManager.openMenu(player, "deny-reasons");
                }
                case "back" -> guiManager.openMenu(player, "main");
            }
        }

        if (title.contains("Deny Reasons")) {
            event.setCancelled(true);

            List<ExpansionRequest> pending = ExpansionQueue.getPendingRequests();
            if (pending.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "No requests to deny.");
                guiManager.openMenu(player, "admin-expansions");
                return;
            }

            ExpansionRequest req = pending.get(0);
            UUID targetId = req.getPlayerId();

            switch (name.toLowerCase(Locale.ROOT)) {
                case "back" -> guiManager.openMenu(player, "admin-expansions");
                case "other" -> {
                    // kick into chat input mode
                    GUIManager.startAwaitingReason(player, req);
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Type your denial reason in chat or type 'cancel'.");
                }
                default -> {
                    // predefined deny reason button clicked
                    String reason = name; // button label = reason
                    Bukkit.dispatchCommand(player, "proshield deny " + targetId + " " + reason);
                    guiManager.openMenu(player, "admin-expansions");
                }
            }
        }
    }
}
