package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * GUIListener (ProShield v1.2.6 FINAL)
 *
 * - Routes clicks to the correct GUIManager handler
 * - Cancels vanilla inventory behavior inside ProShield GUIs
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
    }

    // Legacy fallback
    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = (guiManager != null ? guiManager : plugin.getGuiManager());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (guiManager == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Always cancel vanilla actions inside ProShield menus
        event.setCancelled(true);

        String title = event.getView().getTitle();

        try {
            if (title.contains("ProShield Menu")) {
                guiManager.handleMainClick(player, event);
            } else if (title.contains("Claim Info")) {
                guiManager.handleMainClick(player, event); // INFO handled in main handler
            } else if (title.contains("Trusted")) {
                guiManager.handleTrustedClick(player, event);
            } else if (title.contains("Assign Role")) {
                guiManager.handleAssignRoleClick(player, event);
            } else if (title.contains("Flags")) {
                guiManager.handleFlagsClick(player, event);
            } else if (title.contains("Admin Tools")) {
                guiManager.handleAdminClick(player, event);
            } else if (title.contains("World Controls") && !title.contains("World:")) {
                guiManager.handleWorldControlsClick(player, event);
            } else if (title.contains("World:")) {
                guiManager.handleWorldControlsClick(player, event);
            } else if (title.contains("Expansion Requests")) {
                guiManager.handleExpansionReviewClick(player, event);
            } else if (title.contains("Expansion History")) {
                guiManager.handleHistoryClick(player, event);
            } else if (title.contains("Deny Reason")) {
                guiManager.handleDenyReasonClick(player, event);
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("[GUIListener] Error handling click for " + player.getName()
                    + " in GUI: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        // No explicit cleanup needed — GUIManager v1.2.6 uses View stack only
    }
}
