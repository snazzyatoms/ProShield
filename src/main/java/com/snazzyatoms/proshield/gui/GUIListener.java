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
 * GUIListener (ProShield v1.2.6+)
 *
 * - Forwards all click events into GUIManager#handleClick
 * - Cancels vanilla inventory behavior inside ProShield GUIs
 * - Cleans up ephemeral state (role assignment, deny reasons) when needed
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

        // Always cancel to prevent vanilla actions inside ProShield menus
        event.setCancelled(true);

        try {
            guiManager.handleClick(event);
        } catch (Exception ex) {
            plugin.getLogger().warning("[GUIListener] Error handling click for " + player.getName()
                    + " in GUI: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (guiManager == null) return;

        // Optional: clear any temporary state if your GUIManager still uses it
        guiManager.clearPendingRoleAssignment(player.getUniqueId());
        guiManager.clearPendingDenyTarget(player.getUniqueId());
    }
}
