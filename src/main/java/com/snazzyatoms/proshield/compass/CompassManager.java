// src/main/java/com/snazzyatoms/proshield/compass/CompassManager.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class CompassManager {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public CompassManager(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    /**
     * Creates the ProShield compass item.
     */
    private ItemStack createCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bProShield Compass");
            meta.setLore(Collections.singletonList("§7Right-click to open ProShield menu"));
            compass.setItemMeta(meta);
        }
        return compass;
    }

    /**
     * Gives the ProShield compass to a player,
     * avoiding duplicates if they already have one.
     */
    public void giveCompass(Player player) {
        // Prevent duplicate compasses
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() &&
                        "§bProShield Compass".equals(meta.getDisplayName())) {
                    return; // already has one
                }
            }
        }
        player.getInventory().addItem(createCompass());
    }

    /**
     * Gives the compass to all online players (e.g., on reload).
     */
    public void giveCompassToAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            giveCompass(player);
        }
    }
}
