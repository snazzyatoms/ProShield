// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.listeners.AdminMenuListener;
import com.snazzyatoms.proshield.gui.listeners.PlayerMenuListener;
import org.bukkit.event.Listener;

/**
 * GUIListener
 *
 * ✅ Central hook for registering both Player & Admin menu listeners.
 * ✅ Defers all logic to GUIManager + dedicated listeners.
 * ✅ Keeps menus consistent with CompassManager (player vs admin).
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;
    private final PlayerMenuListener playerMenuListener;
    private final AdminMenuListener adminMenuListener;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;

        // Attach listeners (✅ now use GUIManager, not GUICache)
        this.playerMenuListener = new PlayerMenuListener(plugin, guiManager);
        this.adminMenuListener = new AdminMenuListener(plugin, guiManager);

        // Register with Bukkit
        plugin.getServer().getPluginManager().registerEvents(playerMenuListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(adminMenuListener, plugin);
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
