// src/main/java/com/snazzyatoms/proshield/compass/CompassManager.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompassManager {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public CompassManager(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    /**
     * Gives the player a ProShield Compass if they don’t already have one.
     */
    public void giveCompass(Player player) {
        for (ItemStack i : player.getInventory().getContents()) {
            if (i != null && i.getType() == Material.COMPASS && i.hasItemMeta()) {
                ItemMeta m = i.getItemMeta();
                if (m != null && "§aProShield Compass".equals(m.getDisplayName())) {
                    return; // already has one
                }
            }
        }
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aProShield Compass");
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
    }

    /**
     * Check if item is the ProShield Compass.
     */
    public boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && "§aProShield Compass".equals(meta.getDisplayName());
    }

    /**
     * Open GUI depending on player’s permissions.
     */
    public void openCompassGUI(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.hasPermission("proshield.admin")) {
                guiManager.openMenu(player, "admin");
            } else {
                guiManager.openMenu(player, "main");
            }
        });
    }
}
