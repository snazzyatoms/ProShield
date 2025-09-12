package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles giving and managing the ProShield Compass to players.
 */
public class CompassManager {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public CompassManager(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    /**
     * Give the player a ProShield compass.
     * @param player Player to receive
     * @param force  Whether to override existing compass
     */
    public void giveCompass(Player player, boolean force) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aProShield Compass");
            compass.setItemMeta(meta);
        }

        if (force || !player.getInventory().contains(Material.COMPASS)) {
            player.getInventory().addItem(compass);
        }
    }

    /**
     * Handle opening the GUI when compass is used.
     */
    public void openCompassGUI(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> guiManager.openMainMenu(player));
    }
}
