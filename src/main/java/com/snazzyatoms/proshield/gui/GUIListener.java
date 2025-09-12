package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.listeners.RolesListener;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Global GUI listener that delegates to specific listeners like RolesListener.
 * Uses compatibility aliases in GUIManager to detect ProShield menus.
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final RolesListener rolesListener;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;

        // RolesListener now requires plugin + roleManager + plotManager + gui
        this.rolesListener = new RolesListener(
                plugin,
                plugin.getRoleManager(),
                plugin.getPlotManager(),
                guiManager
        );
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (guiManager.isProShieldGUI(inv)) {
            // Delegate into RolesListener
            rolesListener.onClick(event); // call its existing handler directly
        }
    }
}
