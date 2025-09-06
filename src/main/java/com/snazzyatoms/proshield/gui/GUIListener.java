// path: src/main/java/com/snazzyatoms/proshield/gui/GUIListener.java
package com.snazzyatoms.proshield.gui;

import com.snazzyatoms.proshield.plots.PlotManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    private final PlotManager plotManager;

    public GUIListener(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.equalsIgnoreCase("ProShield GUI")) return;

        event.setCancelled(true);
        Location loc = player.getLocation();

        switch (event.getCurrentItem().getType()) {
            case GRASS_BLOCK -> {
                if (plotManager.createClaim(player.getUniqueId(), loc)) {
                    player.sendMessage(ChatColor.GREEN + "You claimed this chunk!");
                } else {
                    player.sendMessage(ChatColor.RED + "This chunk is already claimed.");
                }
            }
            case BARRIER -> {
                if (plotManager.removeClaim(player.getUniqueId(), loc)) {
                    player.sendMessage(ChatColor.GREEN + "You unclaimed this chunk!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don’t own this claim.");
                }
            }
            default -> {
                // ignore other clicks
            }
        }
    }
}
