// src/main/java/com/snazzyatoms/proshield/plots/MobBorderRepelListener.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;

/**
 * MobBorderRepelListener
 *
 * ✅ Fixes:
 * - Removed dependency on nonexistent Plot#getNearestBorder.
 * - Repel mobs by checking distance from claim border directly.
 * - Preserves original logic (push hostile mobs away from claims).
 */
public class MobBorderRepelListener implements Listener {

    private final ProShield plugin;
    private final PlotManager plotManager;

    public MobBorderRepelListener(ProShield plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();

        // Only repel hostile mobs
        if (!(entity instanceof Monster)) return;

        Location loc = entity.getLocation();
        Plot plot = plotManager.getPlot(loc);
        if (plot == null) return;

        // Compute nearest border location (instead of missing method)
        Location borderLoc = getNearestBorder(plot, loc);

        // If too close to border, push mob away
        if (borderLoc != null && loc.distanceSquared(borderLoc) < 9) { // within 3 blocks
            Vector push = loc.toVector().subtract(borderLoc.toVector()).normalize().multiply(1.2);
            entity.setVelocity(push);
        }
    }

    /**
     * Compute nearest border of the claim relative to a location.
     */
    private Location getNearestBorder(Plot plot, Location loc) {
        int bx = plot.getX() << 4;
        int bz = plot.getZ() << 4;

        double x = loc.getX();
        double z = loc.getZ();

        // Distances to four sides
        double dxWest  = Math.abs(x - bx);
        double dxEast  = Math.abs((bx + 16) - x);
        double dzNorth = Math.abs(z - bz);
        double dzSouth = Math.abs((bz + 16) - z);

        double min = Math.min(Math.min(dxWest, dxEast), Math.min(dzNorth, dzSouth));

        if (min == dxWest)  return new Location(loc.getWorld(), bx, loc.getY(), z);
        if (min == dxEast)  return new Location(loc.getWorld(), bx + 16, loc.getY(), z);
        if (min == dzNorth) return new Location(loc.getWorld(), x, loc.getY(), bz);
        if (min == dzSouth) return new Location(loc.getWorld(), x, loc.getY(), bz + 16);

        return null;
    }
}
