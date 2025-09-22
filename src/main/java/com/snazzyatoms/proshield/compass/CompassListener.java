package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Opens the main ProShield GUI when a player right-clicks the ProShield Compass.
 * Fully synchronized with GUIManager (v1.2.6+).
 */
public class CompassListener implements Listener {

    private final ProShield plugin;

    public CompassListener(ProShield plugin) {
        this.plugin = plugin;
    }

    /** Checks if an item is the ProShield Compass */
    private boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        String dn = ChatColor.stripColor(meta.getDisplayName());
        return dn != null && dn.equalsIgnoreCase("ProShield Compass");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Avoid double triggers from off-hand
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!isProShieldCompass(item)) return;

        // Cancel vanilla compass behavior
        event.setCancelled(true);

        // ✅ Updated for GUIManager v1.2.6+
        plugin.getGuiManager().openMainMenu(player);
    }
}
