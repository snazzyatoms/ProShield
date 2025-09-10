// ===========================================
// SpawnGuardListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpawnGuardListener implements Listener {
    private final PlotManager plots;

    public SpawnGuardListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!plots.canClaimHere(e.getPlayer().getLocation())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYou cannot claim near spawn!");
        }
    }
}
