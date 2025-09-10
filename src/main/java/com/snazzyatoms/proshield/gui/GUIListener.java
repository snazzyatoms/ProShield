package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for ProShield menu clicks and compass opens.
 * Compatible with 1.18–1.21 (no InventoryView#getTitle usage).
 */
public class GUIListener implements Listener {

    private final GUIManager gui;

    public GUIListener(GUIManager gui) {
        this.gui = gui;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCompassUse(PlayerInteractEvent e) {
        // Only care about hand interactions with a compass
        if (e.getHand() == EquipmentSlot.OFF_HAND) return; // avoid double-fire
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;
        if (!gui.isProShieldCompass(item)) return;

        e.setCancelled(true);
        Player p = e.getPlayer();

        // If compass metadata says ADMIN and player has perms, open admin; else main.
        boolean wantsAdmin = false;
        try {
            String rawName = item.getItemMeta() != null ? ChatColor.stripColor(item.getItemMeta().getDisplayName()) : "";
            wantsAdmin = rawName != null && rawName.toLowerCase().contains("admin");
        } catch (Throwable ignored) {}

        if (wantsAdmin) gui.openAdmin(p);
        else gui.openMain(p);

        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.1f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        Inventory clicked = e.getInventory(); // top inventory of the view
        if (!gui.isOurInventory(clicked)) return;

        e.setCancelled(true); // prevent taking/moving icons
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= clicked.getSize()) return; // only react to top inv slots

        ItemStack current = e.getCurrentItem();
        gui.handleInventoryClick(player, clicked, slot, current);
    }
}
