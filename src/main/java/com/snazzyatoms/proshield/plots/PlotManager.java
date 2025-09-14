// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

/**
 * PlotManager
 * -----------
 * Manages all claimed plots on the server.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>(); // key = world:x:z

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    private String key(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }

    public Plot getPlot(Location loc) {
        Chunk chunk = loc.getChunk();
        return plots.get(key(loc.getWorld().getName(), chunk.getX(), chunk.getZ()));
    }

    public boolean isClaimed(Location loc) {
        return getPlot(loc) != null;
    }

    public Plot createPlot(UUID owner, Location loc) {
        Chunk chunk = loc.getChunk();
        String world = loc.getWorld().getName();
        Plot plot = new Plot(owner, world, chunk.getX(), chunk.getZ());
        plots.put(key(world, chunk.getX(), chunk.getZ()), plot);
        return plot;
    }

    public void removePlot(Location loc) {
        Chunk chunk = loc.getChunk();
        plots.remove(key(loc.getWorld().getName(), chunk.getX(), chunk.getZ()));
    }

    public Collection<Plot> getAllPlots() {
        return plots.values();
    }

    public void clear() {
        plots.clear();
    }

    /**
     * Utility: lookup plot by internal ID (for role manager & admin tools).
     */
    public Plot getPlotById(UUID plotId) {
        for (Plot p : plots.values()) {
            if (p.getId().equals(plotId)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Optional: get player-friendly name for a UUID.
     */
    public String getPlayerName(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }
}
