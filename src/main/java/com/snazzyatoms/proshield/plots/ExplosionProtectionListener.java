// ===========================================
// ExplosionProtectionListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionProtectionListener implements Listener {
    private final PlotManager plots;

    public ExplosionProtectionListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> !plots.canExplode(block.getLocation(), e.getEntity()));
    }
}
