// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.listeners.AdminMenuListener;
import com.snazzyatoms.proshield.gui.listeners.PlayerMenuListener;
import com.snazzyatoms.proshield.gui.listeners.RolesListener;
import com.snazzyatoms.proshield.gui.listeners.TrustListener;
import com.snazzyatoms.proshield.gui.listeners.UntrustListener;
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
        plugin.getServer().getPluginManager().registerEvents(new RolesListener(plugin, plugin.getPlotManager(), plugin.getRoleManager(), guiManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new TrustListener(plugin, plugin.getPlotManager(), plugin.getRoleManager(), guiManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new UntrustListener(plugin, plugin.getPlotManager(), plugin.getRoleManager(), guiManager), plugin);
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
