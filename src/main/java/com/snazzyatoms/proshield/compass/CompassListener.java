// src/main/java/com/snazzyatoms/proshield/compass/CompassListener.java
package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Handles ProShield Compass interactions.
 * Opens the main ProShield GUI when a player right-clicks
 * with the ProShield compass in hand.
 */
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
        ItemStack item = player.getInventory().getItemInMainHand();

        // Must be a compass with custom name
        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        if (displayName == null) return;

        // Match case-insensitive name
        if (!displayName.equalsIgnoreCase("ProShield Compass")) return;

        // Only trigger on right-click
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        // Open GUI
        guiManager.openMenu(player, "main");
        event.setCancelled(true);
    }
}
