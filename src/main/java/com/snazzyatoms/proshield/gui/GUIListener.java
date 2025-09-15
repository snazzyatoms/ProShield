package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * GUIListener
 * - Delegates all inventory clicks into GUIManager.handleClick
 * - Cancels item movement in GUIs
 * - Fully synced with GUIManager v1.2.5 (approve/deny submenu etc.)
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Check if this inventory belongs to a ProShield menu (title contains "ProShield" etc.)
        String title = event.getView().getTitle();
        if (title == null || !title.contains("ProShield") && !title.contains("Expansion") && !title.contains("Admin Tools")) {
            return; // not our menu
        }

        // Always cancel vanilla item movement in GUIs
        event.setCancelled(true);

        // Forward to GUIManager’s central click handler
        guiManager.handleClick(event);
    }
}
