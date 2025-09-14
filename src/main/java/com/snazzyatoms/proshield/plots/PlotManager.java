// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlotManager
 * ----------------
 * Handles storage and lookup of all plots in the world.
 * Each plot is tied to a chunk and owned by a player.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>();

    /**
     * Constructor now correctly accepts ProShield (plugin instance).
     * This preserves all prior logic and ensures ProShield.java compiles cleanly.
     */
    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Generate a chunk key (world:x:z) for identifying plots.
     */
    private String chunkKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getChunk().getX() + ":" + loc.getChunk().getZ();
    }

    /**
     * Claim a chunk for a player.
     */
    public Plot claimPlot(UUID playerId, Location loc) {
        String key = chunkKey(loc);
        Plot plot = new Plot(playerId, loc.getWorld().getName(),
                loc.getChunk().getX(), loc.getChunk().getZ());
        plots.put(key, plot);
        return plot;
    }

    /**
     * Unclaim a chunk.
     */
    public void unclaimPlot(Location loc) {
        plots.remove(chunkKey(loc));
    }

    /**
     * Retrieve a plot for a given location.
     */
    public Plot getPlot(Location loc) {
        return plots.get(chunkKey(loc));
    }

    /**
     * Expand an existing claim radius for a player.
     */
    public void expandClaim(UUID playerId, int extraRadius) {
        for (Plot plot : plots.values()) {
            if (plot.getOwner().equals(playerId)) {
                plot.expand(extraRadius);
            }
        }
    }

    /**
     * Get all plots.
     */
    public Map<String, Plot> getPlots() {
        return plots;
    }
}
