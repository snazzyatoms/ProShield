package com.snazzyatoms.proshield.plots;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Passive helper to keep naming consistent and available.
 * Actual block of claiming near spawn is enforced in PlotManager#createClaim()
 * by reading config: spawn.block-claiming + spawn.radius.
 */
public class SpawnGuardListener implements Listener {

    private final PlotManager plots;

    public SpawnGuardListener(PlotManager plots) {
        this.plots = plots;
    }

    // If you ever need to visualize or debug, you can add ad-hoc events here.
    // For now, no events are required since PlotManager already denies createClaim() in spawn radius.

    // Convenience method (used by PlotManager) to compute distance to world spawn.
    public static boolean withinSpawnRadius(Location loc, int radius) {
        if (loc == null) return false;
        World w = loc.getWorld();
        if (w == null) return false;
        Location s = w.getSpawnLocation();
        return s.getWorld() != null && s.getWorld().equals(w) && s.distanceSquared(loc) <= (radius * radius);
    }
}
