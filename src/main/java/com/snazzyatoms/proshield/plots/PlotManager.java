// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager
 * - Manages all claimed plots.
 * - Unified & condensed (v1.2.0 → v1.2.5).
 * - Handles creation, lookup, trust/role checks, and persistence.
 */
public class PlotManager {

    private final ProShield plugin;
    private final ClaimRoleManager roleManager;

    // Map of plotId -> Plot
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();

    /**
     * Preferred constructor
     */
    public PlotManager(ProShield plugin, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.roleManager = roleManager;
    }

    /**
     * Legacy constructor for compatibility
     */
    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.roleManager = null;
    }

    /* -------------------------------------------------------
     * Accessors
     * ------------------------------------------------------- */
    public ProShield getPlugin() {
        return plugin;
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
            if (plot.getChunk().equals(chunk)) {
                return plot;
            }
        }
        return null;
    }

    /**
     * Alias for older code
     */
    public Plot getPlotAt(Chunk chunk) {
        return getPlot(chunk);
    }

    /**
     * Returns claim display name or "Wilderness" if unclaimed
     */
    public String getClaimName(Location location) {
        Plot plot = getPlot(location);
        return (plot != null) ? plot.getDisplayNameSafe() : "Wilderness";
    }

    /* -------------------------------------------------------
     * Create / Remove
     * ------------------------------------------------------- */
    public Plot createPlot(UUID owner, Chunk chunk) {
        Plot plot = new Plot(chunk, owner);
        plots.put(plot.getId(), plot);
        saveAsync(plot);
        return plot;
    }

    public void removePlot(Plot plot) {
        if (plot == null) return;
        plots.remove(plot.getId());
        // TODO: persist removal
    }

    /* -------------------------------------------------------
     * Save / Persistence (async stubs)
     * ------------------------------------------------------- */
    public void saveAsync(Plot plot) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // TODO: Save plot to disk/db
        });
    }

    public void saveAll() {
        for (Plot plot : plots.values()) {
            saveAsync(plot);
        }
    }

    /* -------------------------------------------------------
     * Permissions / Trust
     * ------------------------------------------------------- */
    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true; // free land
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

    public boolean canUnclaim(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return false;

        ClaimRole role = plot.getRole(playerId);
        return role != null && role.canUnclaim();
    }
}
