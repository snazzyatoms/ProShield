// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.listeners.AdminMenuListener;
import com.snazzyatoms.proshield.gui.listeners.PlayerMenuListener;
import com.snazzyatoms.proshield.gui.listeners.RolesMenuListener;
import com.snazzyatoms.proshield.gui.listeners.TrustMenuListener;
import com.snazzyatoms.proshield.gui.listeners.UntrustMenuListener;
import org.bukkit.event.Listener;

/**
 * GUIListener
 *
 * ✅ Central hook for registering all GUI-related listeners.
 * ✅ Covers Player, Admin, Roles, Trust, and Untrust menus.
 * ✅ Keeps GUIs static, functional, and connected.
 */
public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.guiManager = guiManager;

        // Register all listeners
        plugin.getServer().getPluginManager().registerEvents(new PlayerMenuListener(plugin, guiManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new AdminMenuListener(plugin, guiManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new RolesMenuListener(plugin, guiManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new TrustMenuListener(plugin, guiManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new UntrustMenuListener(plugin, guiManager), plugin);
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
