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
 * GUIListener (ProShield v1.2.6 FINAL-SYNCED)
 *
 * - Forwards ALL clicks into GUIManager.handleClick()
 * - Cancels vanilla inventory behavior inside ProShield GUIs
 * - No duplicate routing logic (keeps GUIManager authoritative)
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public GUIListener(ProShield plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
    }

    // Legacy fallback (safe for testing/migration)
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

        // Cancel vanilla behavior always (GUIManager decides outcomes)
        event.setCancelled(true);

        try {
            guiManager.handleClick(event); // ✅ Central router
        } catch (Throwable ex) {
            plugin.getLogger().warning("[GUIListener] Error routing click for player " 
                    + player.getName() + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        // No cleanup required — GUIManager maintains its own View stack
    }
}
