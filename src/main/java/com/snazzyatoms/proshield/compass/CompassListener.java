// src/main/java/com/snazzyatoms/proshield/compass/CompassListener.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CompassListener implements Listener {

    private final ProShield plugin;
    private final GUIManager guiManager;

    public CompassListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS) {
            if (player.getInventory().getItemInMainHand().getItemMeta() != null &&
                player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("ProShield")) {
                guiManager.openMenu(player, "main");
                event.setCancelled(true);
            }
        }
    }
}
