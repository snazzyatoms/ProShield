// ===========================================
// EntityGriefListener.java
// ===========================================
package com.snazzyatoms.proshield.plots;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.Ravager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityGriefListener implements Listener {
    private final PlotManager plots;

    public EntityGriefListener(PlotManager plots) {
        this.plots = plots;
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (e.getEntity() instanceof Enderman || e.getEntity() instanceof Ravager) {
            if (!plots.canEntityGrief(e.getBlock().getLocation(), e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }
}
