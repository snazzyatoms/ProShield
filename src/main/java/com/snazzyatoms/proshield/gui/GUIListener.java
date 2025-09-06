package com.snazzytom.proshield.gui;

import com.snazzytom.proshield.plots.PlotManager;
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
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Location location = player.getLocation();

        String title = event.getView().getTitle();
        if (title.equalsIgnoreCase("ProShield Menu")) {
            event.setCancelled(true);

            switch (event.getCurrentItem().getType()) {
                case GRASS_BLOCK:
                    plotManager.createClaim(player, location); // ✅ fixed
                    player.sendMessage("Plot created from GUI!");
                    break;
                case BARRIER:
                    plotManager.removeClaim(player, location); // ✅ fixed
                    player.sendMessage("Plot removed from GUI!");
                    break;
                default:
                    break;
            }
        }
    }
}
