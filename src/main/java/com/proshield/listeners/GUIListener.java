package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.ProShield;
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

    private final ProShield plugin;
    private final GUIManager guiManager;

    public GUIListener(ProShield plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.COMPASS && item.hasItemMeta()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase("ProShield Compass")) {
            event.setCancelled(true);
            guiManager.openClaimGUI(event.getPlayer());
        }
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.BLUE + "ProShield Claim Manager")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            if (clicked.getType() == Material.GRASS_BLOCK) {
                plugin.getPlotManager().createClaim(player);
                player.closeInventory();
            } else if (clicked.getType() == Material.PAPER) {
                plugin.getPlotManager().getClaimInfo(player);
                player.closeInventory();
            } else if (clicked.getType() == Material.BARRIER) {
                plugin.getPlotManager().removeClaim(player);
                player.closeInventory();
            }
        }
    }
}
