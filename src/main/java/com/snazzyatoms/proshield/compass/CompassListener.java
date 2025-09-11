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
 *
 * Listens for players right-clicking the ProShield compass
 * and opens the correct GUI via CompassManager.
 */
public class CompassListener implements Listener {

    private final CompassManager compassManager;

    public CompassListener(ProShield plugin, CompassManager compassManager) {
        this.compassManager = compassManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        // Only handle main-hand interactions
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (compassManager.isProShieldCompass(item)) {
            event.setCancelled(true); // Prevent default compass behavior
            compassManager.openFromCompass(player, item);
        }
    }
}
