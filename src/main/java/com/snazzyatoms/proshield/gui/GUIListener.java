package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Handles inventory clicks and compass interactions for ProShield GUIs.
 * Supports both Player and Admin menus.
 */
public class GUIListener implements Listener {

    private final ProShield plugin;
    private final GUIManager gui;

    public GUIListener(ProShield plugin, GUIManager gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    // =========================================================
    // COMPASS OPENING
    // =========================================================

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        if (!gui.isProShieldCompass(item)) return;

        Player player = event.getPlayer();
        boolean admin = player.hasPermission("proshield.admin");

        event.setCancelled(true);
        gui.openMain(player, admin);
    }

    // =========================================================
    // INVENTORY CLICKS
    // =========================================================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (event.getView().getTitle() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        ItemStack clicked = event.getCurrentItem();

        // Stop taking GUI items
        event.setCancelled(true);

        // Detect if this is a ProShield GUI
        boolean admin = player.hasPermission("proshield.admin");

        if (title.equalsIgnoreCase("ProShield Menu")
                || title.equalsIgnoreCase("ProShield Admin")
                || title.equalsIgnoreCase("ProShield Help")) {

            String action = GUICache.getAction(clicked);

            if (action != null) {
                gui.handleButtonClick(player, action, admin);
            }
        }
    }
}
