// src/main/java/com/snazzyatoms/proshield/gui/GUIManager.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.cache.GUICache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

/**
 * GUIManager - central controller for ProShield GUIs.
 *
 * ✅ Preserves prior logic
 * ✅ Expanded with missing methods (giveCompass, clearCache)
 */
public class GUIManager {

    private final ProShield plugin;
    private final GUICache guiCache;

    public GUIManager(ProShield plugin, GUICache guiCache) {
        this.plugin = plugin;
        this.guiCache = guiCache;
    }

    /* -------------------------------------------------------
     * GUI Cache Handling
     * ------------------------------------------------------- */

    /** Clear all cached GUI states safely. */
    public void clearCache() {
        if (guiCache != null) {
            guiCache.clearCache();
        }
    }

    public GUICache getGuiCache() {
        return guiCache;
    }

    /* -------------------------------------------------------
     * Compass Utility
     * ------------------------------------------------------- */

    /**
     * Gives the player a ProShield compass.
     *
     * @param player target player
     * @param replace whether to replace any existing compass
     */
    public void giveCompass(Player player, boolean replace) {
        if (player == null) return;

        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lProShield Compass");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            if (meta instanceof CompassMeta compassMeta) {
                // Point to player’s current location or spawn
                compassMeta.setLodestone(player.getWorld().getSpawnLocation());
                compassMeta.setLodestoneTracked(false);
            }
            compass.setItemMeta(meta);
        }

        // Check inventory
        if (!replace && player.getInventory().contains(Material.COMPASS)) {
            return; // already has one, skip
        }

        // Add to inventory
        player.getInventory().addItem(compass);
        player.updateInventory();

        if (plugin.isDebugEnabled()) {
            Bukkit.getLogger().info("[ProShield] Gave compass to " + player.getName());
        }
    }

    /* -------------------------------------------------------
     * GUI Management (placeholder for expansion)
     * ------------------------------------------------------- */

    public void openMainMenu(Player player) {
        // TODO: build and open GUI inventory (kept as placeholder for expansion)
    }
}
