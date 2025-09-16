package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.util.MessagesUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;
    private final MessagesUtil messages;

    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
        this.gui = plugin.getGuiManager();
        this.messages = plugin.getMessagesUtil();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        ItemStack clicked = event.getCurrentItem();

        // Cancel all clicks inside GUIs
        if (title != null && title.contains("ProShield")) {
            event.setCancelled(true);
        }

        // --- Main Menu ---
        if (title.equalsIgnoreCase("ProShield Menu")) {
            gui.handleMainClick(player, event);
            return;
        }

        // --- Trusted Players ---
        if (title.equalsIgnoreCase("Trusted Players")) {
            gui.handleTrustedClick(player, event);
            return;
        }

        // --- Assign Role ---
        if (title.equalsIgnoreCase("Assign Role")) {
            gui.handleAssignRoleClick(player, event);
            return;
        }

        // --- Claim Flags ---
        if (title.equalsIgnoreCase("Claim Flags")) {
            gui.handleFlagsClick(player, event);
            return;
        }

        // --- Admin Tools ---
        if (title.equalsIgnoreCase("Admin Tools")) {
            gui.handleAdminClick(player, event);
            return;
        }

        // --- Expansion Requests ---
        if (title.equalsIgnoreCase("Expansion Requests")) {
            gui.handleExpansionReviewClick(player, event);
            return;
        }

        // --- Deny Reasons ---
        if (title.equalsIgnoreCase("Deny Reasons")) {
            gui.handleDenyReasonClick(player, event);
            return;
        }

        // --- Expansion History (pagination) ---
        if (title.startsWith("Expansion History")) {
            gui.handleHistoryClick(player, event);
            return;
        }
    }
}
