// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager
 * - Handles all claim/plot lifecycle
 * - Manages trust, roles, and claim checks
 * - Unified version (1.2.0 → 1.2.5)
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public Collection<Plot> getAllPlots() {
        return Collections.unmodifiableCollection(plots.values());
    }

    public Plot getPlotById(UUID id) {
        return plots.get(id);
    }

    public Plot getPlot(Location location) {
        if (location == null) return null;
        return getPlot(location.getChunk());
    }

    public Plot getPlot(Chunk chunk) {
        for (Plot plot : plots.values()) {
            if (plot.getChunk().equals(chunk)) return plot;
        }
        return null;
    }

    /** Alias for older code. */
    public Plot getPlotAt(Chunk chunk) {
        return getPlot(chunk);
    }

    /** Get claim display name or "Wilderness". */
    public String getClaimName(Location location) {
        Plot plot = getPlot(location);
        return (plot != null) ? plot.getDisplayNameSafe() : "Wilderness";
    }

    /* ------------------------------------------------------
     * Claim lifecycle
     * ------------------------------------------------------ */
    public Plot createPlot(UUID owner, Chunk chunk) {
        Plot plot = new Plot(chunk, owner);
        plots.put(plot.getId(), plot);
        saveAsync(plot);
        return plot;
    }

    public void removePlot(Plot plot) {
        if (plot == null) return;
        plots.remove(plot.getId());
        // TODO: persist removal to storage
    }

    public void saveAsync(Plot plot) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // TODO: write to disk/database
        });
    }

    public void saveAll() {
        plots.values().forEach(this::saveAsync);
    }

    /* ------------------------------------------------------
     * Permission checks
     * ------------------------------------------------------ */
    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true; // Wilderness
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getRole(playerId);
        return role != null && role != ClaimRole.NONE && role != ClaimRole.VISITOR;
    }

    public boolean canInteract(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true;
        ClaimRole role = plot.getRole(playerId);
        return role != null && role.canInteract();
    }

    public boolean canManage(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return false;
        ClaimRole role = plot.getRole(playerId);
        return role != null && role.canManage();
    }
}
