// src/main/java/com/snazzyatoms/proshield/util/GUIEntrypoint.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.entity.Player;

/**
 * GUIEntrypoint
 *
 * Provides a unified, safe entry point to open ProShield GUIs
 * from commands, events, or listeners.
 *
 * This avoids scattered direct calls to GUIManager and ensures
 * method names stay consistent across versions.
 */
public final class GUIEntrypoint {

    private GUIEntrypoint() {
        // utility
    }

    /** Opens the ProShield main menu for the player. */
    public static void openMain(Player player) {
        ProShield plugin = ProShield.getInstance();
        GUIManager gm = plugin.getGuiManager();
        if (gm != null) {
            gm.openMainMenu(player);
        } else {
            player.sendMessage("§c[ProShield] GUI system not available.");
        }
    }

    /** Opens the admin tools menu (permission required). */
    public static void openAdmin(Player player) {
        ProShield plugin = ProShield.getInstance();
        GUIManager gm = plugin.getGuiManager();
        if (gm != null) {
            gm.openAdminTools(player);
        } else {
            player.sendMessage("§c[ProShield] Admin GUI not available.");
        }
    }
}
