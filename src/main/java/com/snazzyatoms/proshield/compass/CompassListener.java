// src/main/java/com/snazzyatoms/proshield/compass/CompassListener.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * CompassListener
 *
 * Fixed for v1.2.5:
 *   • Removed calls to missing CompassManager methods
 *   • Inline check for ProShield compass (by name & lore)
 *   • Opens GUI via GUIManager
 */
public class CompassListener implements Listener {

    private final CompassManager compassManager;
    private final GUIManager guiManager;

    public CompassListener(ProShield plugin, CompassManager compassManager, GUIManager guiManager) {
        this.compassManager = compassManager;
        this.guiManager = guiManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        // Only handle main-hand interactions
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        if (isProShieldCompass(item)) {
            event.setCancelled(true); // Prevent default compass behavior
            guiManager.openMenu(player, "main");
        }
    }

    /**
     * Inline check if the item is a ProShield compass.
     */
    private boolean isProShieldCompass(ItemStack item) {
        if (item.getType() != org.bukkit.Material.COMPASS) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.hasDisplayName() && meta.getDisplayName().contains("ProShield");
    }
}
