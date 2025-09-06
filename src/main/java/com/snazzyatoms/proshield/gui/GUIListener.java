package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PlotManager plotManager;

    public GUIListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !event.getView().getTitle().equals(ChatColor.DARK_GREEN + "ProShield Menu")) {
            return;
        }

        event.setCancelled(true);
        Location loc = player.getLocation();

        switch (clicked.getType()) {
            case GRASS_BLOCK:
                if (plotManager.createClaim(player.getUniqueId(), loc)) {
                    player.sendMessage(ChatColor.GREEN + "You claimed this chunk!");
                } else {
                    player.sendMessage(ChatColor.RED + "This chunk is already claimed.");
                }
                break;

            case BARRIER:
                if (plotManager.removeClaim(player.getUniqueId(), loc)) {
                    player.sendMessage(ChatColor.GREEN + "You unclaimed this chunk!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't own this claim.");
                }
                break;

            default:
                // Ignore other clicks
                break;
        }
    }
}
