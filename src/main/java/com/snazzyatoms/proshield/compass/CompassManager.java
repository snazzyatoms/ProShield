// src/main/java/com/snazzyatoms/proshield/compass/CompassManager.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * CompassManager
 *
 * Fixed for v1.2.5:
 *   • Preserves giveCompass() from v1.2.4
 *   • Restores compatibility methods (isProShieldCompass, openFromCompass)
 *   • Centralized compass name constant
 */
public class CompassManager {

    private final ProShield plugin;
    private final GUIManager guiManager;

    private static final String COMPASS_NAME = "§aProShield Compass";

    public CompassManager(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    /**
     * Give player the ProShield compass if they don’t already have it.
     */
    public void giveCompass(Player player) {
        for (ItemStack i : player.getInventory().getContents()) {
            if (isProShieldCompass(i)) return;
        }

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(COMPASS_NAME);
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
    }

    /**
     * Check if the given item is the ProShield compass.
     */
    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && COMPASS_NAME.equals(meta.getDisplayName());
    }

    /**
     * Open GUI when using compass.
     */
    public void openFromCompass(Player player, ItemStack item) {
        if (!isProShieldCompass(item)) return;
        openCompassGUI(player);
    }

    /**
     * Open the main ProShield menu directly.
     */
    public void openCompassGUI(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> guiManager.openMenu(player, "main"));
    }
}
