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
 * Constructor matches ProShield.java: new CompassListener(this)
 */
public class CompassListener implements Listener {

    private final ProShield plugin;

    public CompassListener(ProShield plugin) {
        this.plugin = plugin;
    }

    private boolean isProShieldCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        String dn = ChatColor.stripColor(meta.getDisplayName());
        return dn != null && dn.equalsIgnoreCase("ProShield Compass");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Only fire for main hand to avoid double triggers on some servers
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!isProShieldCompass(item)) return;

        event.setCancelled(true);
        plugin.getGuiManager().openMenu(player, "main");
    }
}
