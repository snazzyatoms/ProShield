// ===========================================
// FireProtectionListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class FireProtectionListener implements Listener {
    private final PlotManager plots;

    public FireProtectionListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent e) {
        if (!plots.canIgnite(e.getBlock().getLocation(), e.getCause())) {
            e.setCancelled(true);
        }
    }
}
