// src/main/java/com/snazzyatoms/proshield/gui/AdminGUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;

/**
 * AdminGUIManager
 * ----------------
 * Legacy wrapper for opening admin menus.
 * Delegates to the unified GUIManager to ensure consistency.
 */
public class AdminGUIManager {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public AdminGUIManager(ProShield plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
    }

    /**
     * Opens the admin tools menu (or any menu by key).
     * This delegates directly to GUIManager.openMenu()
     * so all items/actions are synchronized.
     *
     * @param player Player to open menu for
     * @param key    Menu key under gui.menus.<key> (defaults to admin-tools)
     */
    public void openMenu(Player player, String key) {
        if (key == null || key.isEmpty()) {
            key = "admin-tools";
        }
        guiManager.openMenu(player, key);
    }

    /**
     * Convenience helper for opening the default admin tools menu.
     */
    public void openAdminTools(Player player) {
        openMenu(player, "admin-tools");
    }
}
