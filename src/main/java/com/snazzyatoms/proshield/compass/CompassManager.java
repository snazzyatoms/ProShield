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
     * Gives the ProShield compass to a player.
     */
    public void giveCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bProShield Compass");
            compass.setItemMeta(meta);
        }
        player.getInventory().addItem(compass);
    }

    /**
     * Gives the compass to all players online (e.g., on reload).
     */
    public void giveCompassToAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            giveCompass(player);
        }
    }
}
