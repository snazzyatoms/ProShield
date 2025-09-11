package com.snazzyatoms.proshield.compass;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static com.snazzyatoms.proshield.compass.CompassManager.*;

/**
 * Handles right-clicking the ProShield compass.
 * - Right-click: opens Main menu for all
 * - SHIFT + Right-click (Admin compass only): opens Admin menu
 */
public class CompassListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        // Only care about main hand interactions to avoid double-firing
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player p = e.getPlayer();
        ItemStack it = p.getInventory().getItemInMainHand();
        if (!CompassManager.isProShieldCompass(it)) return;

        // Block default compass behavior
        e.setCancelled(true);

        // Open menus based on type/sneak
        CompassManager.openFromCompass(p, it);

        // Soft auditory feedback
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, p.isSneaking() ? 0.85f : 1.15f);
    }
}
