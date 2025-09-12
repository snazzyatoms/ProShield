// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * PlotManager
 * - Stores and manages all plots
 * - Handles creation, lookup, saving/loading
 * - Provides name/owner resolution for use in GUIs & messages
 *
 * Fixed for v1.2.5:
 *  • Use Plot(Chunk, UUID) constructor
 *  • Provide getPlot(String world, int x, int z) overload (some tasks call it)
 *  • Use worldName/x/z getters from Plot
 *  • Preserve bypass helpers and name resolution
 */
public class PlotManager {

    private final ProShield plugin;

    // Plots stored by ID
    private final Map<UUID, Plot> plots = new HashMap<>();

    // Chunk mapping: world:x:z -> plotId
    private final Map<String, UUID> chunkMap = new HashMap<>();

    // Players in bypass mode
    private final Set<UUID> bypassing = new HashSet<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* ======================================================
     * PLOTS
     * ====================================================== */
    public Plot createPlot(UUID ownerId, Chunk chunk) {
        Plot plot = new Plot(chunk, ownerId);
        plots.put(plot.getId(), plot);
        chunkMap.put(chunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()), plot.getId());
        return plot;
    }

    public void removePlot(Plot plot) {
        if (plot == null) return;
        plots.remove(plot.getId());
        chunkMap.remove(chunkKey(plot.getWorldName(), plot.getX(), plot.getZ()));
    }

    public Plot getPlot(UUID plotId) {
        return plots.get(plotId);
    }

    public Plot getPlot(Chunk chunk) {
        if (chunk == null) return null;
        UUID id = chunkMap.get(chunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
        return id != null ? plots.get(id) : null;
    }

    public Plot getPlot(Location loc) {
        if (loc == null) return null;
        Chunk chunk = loc.getChunk();
        return getPlot(chunk);
    }

    /** Overload used by some tasks/utilities (world + chunk coords). */
    public Plot getPlot(String worldName, int x, int z) {
        UUID id = chunkMap.get(chunkKey(worldName, x, z));
        return id != null ? plots.get(id) : null;
    }

    public Collection<Plot> getAllPlots() {
        return Collections.unmodifiableCollection(plots.values());
    }

    /* ======================================================
     * CLAIM NAME HELPERS
     * ====================================================== */
    public String getClaimName(Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return "Wilderness";
        return plot.getDisplayNameSafe();
    }

    /* ======================================================
     * PLAYER HELPERS
     * ====================================================== */
    public String getPlayerName(UUID uuid) {
        OfflinePlayer offline = plugin.getServer().getOfflinePlayer(uuid);
        return (offline != null && offline.getName() != null) ? offline.getName() : uuid.toString();
    }

    /* ======================================================
     * BYPASS MODE
     * ====================================================== */
    public boolean toggleBypass(UUID playerId) {
        if (bypassing.contains(playerId)) {
            bypassing.remove(playerId);
            return false;
        } else {
            bypassing.add(playerId);
            return true;
        }
    }

    public boolean isBypassing(UUID playerId) {
        return bypassing.contains(playerId);
    }

    /* ======================================================
     * SAVE / LOAD (stubs left as-is for 1.2.5)
     * ====================================================== */
    public void saveAll() {
        // TODO: serialize plots to disk (YAML/JSON)
    }

    public void loadAll() {
        // TODO: load plots from disk
    }

    /* ======================================================
     * INTERNAL HELPERS
     * ====================================================== */
    private String chunkKey(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }
}
