package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    private String key(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    /**
     * Get a plot by player location (chunk-based).
     */
    public Plot getPlot(Location loc) {
        return plots.get(key(loc.getChunk()));
    }

    /**
     * Get a plot directly by chunk.
     */
    public Plot getPlot(Chunk chunk) {
        return plots.get(key(chunk));
    }

    /**
     * Claim a chunk for a player.
     */
    public void claimPlot(UUID owner, Chunk chunk) {
        String k = key(chunk);
        if (!plots.containsKey(k)) {
            plots.put(k, new Plot(owner, chunk));
        }
    }

    /**
     * Unclaim a chunk.
     */
    public void unclaimPlot(Chunk chunk) {
        plots.remove(key(chunk));
    }

    /**
     * Get a plot owned by a specific UUID.
     * (Added for Expansion Approvals in GUIManager)
     */
    public Plot getPlot(UUID ownerId) {
        for (Plot plot : plots.values()) {
            if (plot.getOwner().equals(ownerId)) {
                return plot;
            }
        }
        return null;
    }

    /**
     * Expand an existing claim by a radius.
     */
    public void expandClaim(UUID ownerId, int extraRadius) {
        Plot plot = getPlot(ownerId);
        if (plot == null) return;

        plot.expand(extraRadius);
    }

    /**
     * Check if a chunk is already claimed.
     */
    public boolean isClaimed(Chunk chunk) {
        return plots.containsKey(key(chunk));
    }
}
