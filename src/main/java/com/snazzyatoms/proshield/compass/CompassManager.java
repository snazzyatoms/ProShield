package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
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

    public void giveCompass(Player player, boolean force) {
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aProShield Compass");
            compass.setItemMeta(meta);
        }

        // Only give if forced or not already in inventory
        if (force || !player.getInventory().contains(compass)) {
            player.getInventory().addItem(compass);
        }
    }

    public void openCompassGUI(Player player) {
        guiManager.openMainMenu(player, player.hasPermission("proshield.admin"));
    }
}
