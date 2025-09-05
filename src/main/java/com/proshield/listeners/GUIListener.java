package com.snazzyatoms.proshield.listeners;

import com.snazzyatoms.proshield.managers.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final GUIManager guiManager;

    public GUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    /**
     * Opens Claim Management GUI when a player right-clicks with the ProShield Compass
     */
    @EventHandler
    public void onPlayerUseCompass(PlayerInteractEvent event) {
        // Ignore off-hand interactions to avoid double-triggering
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.COMPASS && item.hasItemMeta()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase("ProShield Compass")) {
            event.setCancelled(true);
            guiManager.openClaimGUI(player);
        }
    }

    /**
     * Handles clicks inside the Claim Management GUI
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        guiManager.handleGUIClick(event);
    }
}
