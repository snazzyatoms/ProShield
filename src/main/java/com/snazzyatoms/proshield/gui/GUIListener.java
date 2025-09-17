package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    // New style (recommended): only plugin, fetch manager from plugin
    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
    }

    // Legacy style (to satisfy existing call sites)
    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager != null ? guiManager : plugin.getGuiManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String title = event.getView().getTitle();
        if (title == null) return;

        // Only handle our menus
        if (!(title.contains("ProShield")
                || title.contains("Trusted Players")
                || title.contains("Assign Role")
                || title.contains("Claim Flags")
                || title.contains("Admin Tools")
                || title.contains("Expansion Requests")
                || title.contains("Expansion History")
                || title.contains("Request Expansion")
                || title.contains("Deny Reasons"))) {
            return;
        }

        event.setCancelled(true); // prevent vanilla slot movement

        if (title.contains("ProShield Menu")) {
            guiManager.handleMainClick(player, event);
        } else if (title.contains("Trusted Players")) {
            guiManager.handleTrustedClick(player, event);
        } else if (title.contains("Assign Role")) {
            guiManager.handleAssignRoleClick(player, event);
        } else if (title.contains("Claim Flags")) {
            guiManager.handleFlagsClick(player, event);
        } else if (title.contains("Admin Tools")) {
            guiManager.handleAdminClick(player, event);
        } else if (title.contains("Expansion Requests")) {
            guiManager.handleExpansionReviewClick(player, event);
        } else if (title.contains("Expansion History")) {
            guiManager.handleHistoryClick(player, event);
        } else if (title.contains("Request Expansion")) {
            plugin.getExpansionRequestManager().handlePlayerRequestClick(player, event);
        } else if (title.contains("Deny Reasons")) {
            guiManager.handleDenyReasonClick(player, event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title != null && title.contains("Assign Role")) {
            guiManager.clearPendingRoleAssignment(player.getUniqueId());
        }
    }
}
