// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * GUIListener
 *
 * ✅ Cleaned up for v1.2.5
 * - Cancels clicks in ProShield menus
 * - Delegates all logic to GUIManager.handleClick()
 * - Supports player + admin menus (including Expansion Requests)
 */
public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.contains("ProShield")
                || title.contains("Claim")
                || title.contains("Flags")
                || title.contains("Trusted Players")
                || title.contains("Expansion Requests")) {

            event.setCancelled(true); // block item pickup
            guiManager.handleClick(event); // delegate to GUIManager
        }
    }
}
