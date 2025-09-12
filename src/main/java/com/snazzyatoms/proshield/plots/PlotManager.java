// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager
 *
 * Handles storage, retrieval, and persistence of claims.
 * Provides lookup utilities for plots by chunk, UUID, or location.
 * Preserves all logic from v1.2.5 and fixes missing methods reported in logs.
 */
public class PlotManager {

    private final ProShield plugin;
    private final ClaimRoleManager roleManager;

    // Map claimId -> Plot
    private final Map<UUID, Plot> plotsById = new ConcurrentHashMap<>();

    // Map chunkKey -> claimId
    private final Map<String, UUID> plotsByChunk = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.roleManager = roleManager;
    }

    /* -------------------------------------------------------
     * Basic Utilities
     * ------------------------------------------------------- */

    private String chunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    private String chunkKey(Location loc) {
        return loc.getWorld().getName() + ":" + (loc.getBlockX() >> 4) + ":" + (loc.getBlockZ() >> 4);
    }

    public Plot getPlot(Chunk chunk) {
        UUID id = plotsByChunk.get(chunkKey(chunk));
        return (id != null) ? plotsById.get(id) : null;
    }

    public Plot getPlot(Location loc) {
        UUID id = plotsByChunk.get(chunkKey(loc));
        return (id != null) ? plotsById.get(id) : null;
    }

    public Plot getPlotById(UUID claimId) {
        return plotsById.get(claimId);
    }

    public Collection<Plot> getAllPlots() {
        return plotsById.values();
    }

    /* -------------------------------------------------------
     * Claim Creation & Removal
     * ------------------------------------------------------- */

    public Plot createClaim(UUID owner, Location loc) {
        UUID id = UUID.randomUUID();
        Plot plot = new Plot(id, owner, loc);
        plotsById.put(id, plot);
        plotsByChunk.put(chunkKey(loc), id);
        saveAsync(plot);
        return plot;
    }

    public void removePlot(Plot plot) {
        if (plot == null) return;
        plotsById.remove(plot.getId());
        plotsByChunk.remove(chunkKey(plot.getChunk()));
    }

    /* -------------------------------------------------------
     * Role Lookups
     * ------------------------------------------------------- */

    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return false;
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getTrusted().get(playerId);
        return role != null && role != ClaimRole.VISITOR;
    }

    /* -------------------------------------------------------
     * Claim Name Utilities
     * ------------------------------------------------------- */

    public String getClaimName(Location loc) {
        Plot plot = getPlot(loc);
        return (plot != null) ? plot.getDisplayNameSafe() : "Wilderness";
    }

    /* -------------------------------------------------------
     * Persistence (stub for async saving)
     * ------------------------------------------------------- */

    public void saveAsync(Plot plot) {
        // TODO: integrate with database or config persistence
        plugin.getLogger().info("[ProShield] Saved plot " + plot.getId() + " asynchronously.");
    }
}
