// src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.listeners.AdminMenuListener;
import com.snazzyatoms.proshield.gui.listeners.PlayerMenuListener;
import org.bukkit.event.Listener;

public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.guiManager = guiManager;
        // Register real listeners
        plugin.getServer().getPluginManager().registerEvents(new PlayerMenuListener(plugin, guiManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new AdminMenuListener(plugin, guiManager), plugin);
    }

    public GUIManager getGuiManager() { return guiManager; }
}
