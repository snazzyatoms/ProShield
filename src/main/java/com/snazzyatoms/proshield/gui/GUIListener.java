package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.listeners.RolesListener;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Global GUI listener that delegates to specific listeners like RolesListener.
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final RolesListener rolesListener;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;

        // RolesListener now only needs plugin + roleManager
        this.rolesListener = new RolesListener(plugin, plugin.getRoleManager());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (guiManager.isProShieldGUI(inv)) {
            // Pass events down to specialized handlers
            rolesListener.handle(event);
        }
    }
}
