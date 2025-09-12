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
 * Manages all plots in the world.
 * Handles creation, lookup, saving, and trusted role access.
 */
public class PlotManager {

    private final ProShield plugin;
    private final ClaimRoleManager roleManager;

    // Map of plotId -> Plot
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.roleManager = roleManager;
    }

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
        Chunk chunk = location.getChunk();
        return getPlot(chunk);
    }

    public Plot getPlot(Chunk chunk) {
        for (Plot plot : plots.values()) {
            if (plot.getChunk().equals(chunk)) {
                return plot;
            }
        }
        return null;
    }

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
            // TODO: save plot to disk/database
        });
    }

    public void saveAll() {
        for (Plot plot : plots.values()) {
            saveAsync(plot);
        }
    }

    /**
     * Checks if a player is trusted or the owner of a claim.
     */
    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true; // not claimed, free to interact
        if (plot.isOwner(playerId)) return true;

        ClaimRole role = plot.getRole(playerId);
        return role != null && role != ClaimRole.NONE && role != ClaimRole.VISITOR;
    }

    /**
     * Checks if a player can interact within a plot.
     */
    public boolean canInteract(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true;

        ClaimRole role = plot.getRole(playerId);
        return role != null && role.canInteract();
    }

    /**
     * Checks if a player can manage a plot.
     */
    public boolean canManage(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return false;

        ClaimRole role = plot.getRole(playerId);
        return role != null && role.canManage();
    }
}
