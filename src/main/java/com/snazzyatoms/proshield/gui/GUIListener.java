package com.snazzyatoms.proshield.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();

        if (inv == null || inv.getTitle() == null) return;

        String title = inv.getTitle();

        // Only intercept ProShield GUIs
        if (!title.contains("ProShield") && !title.contains("Player Tools") && !title.contains("Admin Tools")) {
            return;
        }

        event.setCancelled(true); // Prevent item taking/moving

        int slot = event.getRawSlot();
        guiManager.handleButtonClick(player, title, slot);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        if (inv == null || inv.getTitle() == null) return;

        String title = inv.getTitle();

        // Optional: we could clear cache entries for player-specific menus
        if (title.contains("ProShield") || title.contains("Player Tools") || title.contains("Admin Tools")) {
            // Currently we don't clear cache here, since GUIs are shared globally.
            // But this is where per-player cleanup could go in the future.
        }
    }
}
