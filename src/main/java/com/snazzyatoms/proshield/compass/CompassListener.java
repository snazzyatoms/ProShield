// src/main/java/com/snazzyatoms/proshield/compass/CompassListener.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * CompassListener
 * - Listens for ProShield Compass usage
 * - Opens correct GUI based on permissions
 */
public class CompassListener implements Listener {

    private final CompassManager compassManager;

    public CompassListener(ProShield plugin, CompassManager compassManager) {
        this.compassManager = compassManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return; // main hand only

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (compassManager.isProShieldCompass(item)) {
            event.setCancelled(true); // block default compass use
            compassManager.openCompassGUI(player); // open GUI
        }
    }
}
