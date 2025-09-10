package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final GUIManager guiManager;
    private final GUICache cache;

    public GUIListener(GUIManager guiManager, GUICache cache) {
        this.guiManager = guiManager;
        this.cache = cache;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        // Check if it's a ProShield GUI
        if (!cache.isProShieldGUI(title)) {
            return;
        }

        event.setCancelled(true); // Always cancel clicks in ProShield GUIs

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        int slot = event.getRawSlot();

        // Route to GUIManager’s button handler
        guiManager.handleButtonClick(player, title, slot);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!cache.isProShieldGUI(title)) {
            return;
        }

        // Optional: cleanup player-specific GUI cache (if we later cache per-player menus)
        cache.cleanup(player);
    }
}
