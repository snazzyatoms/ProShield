// src/main/java/com/snazzyatoms/proshield/util/GUIEntrypoint.java
package com.snazzyatoms.proshield.util;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;

/**
 * Centralized entrypoint for opening GUIs.
 * Ensures consistency if GUIManager method signatures change.
 *
 * Usage:
 *   GUIEntrypoint.openMain(player);
 */
public final class GUIEntrypoint {

    private GUIEntrypoint() {} // utility class

    public static void openMain(Player player) {
        ProShield plugin = ProShield.getInstance();
        if (plugin != null && plugin.getGuiManager() != null) {
            plugin.getGuiManager().openMainMenu(player); // ✅ single canonical call
        }
    }
}
