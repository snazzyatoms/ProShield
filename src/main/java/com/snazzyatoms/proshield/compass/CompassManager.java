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
 * - Handles giving, validating, and interacting with the ProShield Compass.
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
     * Give the player a ProShield compass (force = false by default).
     */
    public void giveCompass(Player player) {
        giveCompass(player, false);
    }

    /**
     * Give the player a ProShield compass.
     *
     * @param player Player to receive the compass
     * @param force  Whether to override existing compass
     */
    public void giveCompass(Player player, boolean force) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(COMPASS_NAME);
            compass.setItemMeta(meta);
        }

        if (force || !hasCompass(player)) {
            player.getInventory().addItem(compass);
        }
    }

    /**
     * Check if a given item is a ProShield compass.
     */
    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && COMPASS_NAME.equals(meta.getDisplayName());
    }

    /**
     * Check if a player already has a ProShield compass.
     */
    private boolean hasCompass(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isProShieldCompass(item)) return true;
        }
        return false;
    }

    /**
     * Open the GUI if the player is holding a valid ProShield compass.
     */
    public void openFromCompass(Player player, ItemStack item) {
        if (isProShieldCompass(item)) {
            openCompassGUI(player);
        }
    }

    /**
     * Open the ProShield main GUI (forced).
     */
    public void openCompassGUI(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> guiManager.openMainMenu(player));
    }
}
