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

    public void giveCompass(Player player) {
        // Avoid duplicates: only one named "§aProShield Compass"
        for (ItemStack i : player.getInventory().getContents()) {
            if (i != null && i.getType() == Material.COMPASS && i.hasItemMeta()) {
                ItemMeta m = i.getItemMeta();
                if (m != null && "§aProShield Compass".equals(m.getDisplayName())) return;
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

    public void openCompassGUI(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> guiManager.openMenu(player, "main"));
    }
}
