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
 * Handles all claimed Plots in memory and persistence.
 */
public class PlotManager {

    private final ProShield plugin;
    private final ClaimRoleManager roleManager;

    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.roleManager = roleManager;
    }

    public Plot getPlot(Chunk chunk) {
        return plots.values().stream()
                .filter(p -> p.getChunk().getX() == chunk.getX()
                        && p.getChunk().getZ() == chunk.getZ()
                        && Objects.equals(p.getChunk().getWorld(), chunk.getWorld()))
                .findFirst().orElse(null);
    }

    public Plot getPlot(Location loc) {
        return getPlot(loc.getChunk());
    }

    public Plot getPlotById(UUID id) {
        return plots.get(id);
    }

    public void addPlot(Plot plot) {
        plots.put(plot.getId(), plot);
        saveAsync(plot);
    }

    public void removePlot(Plot plot) {
        plots.remove(plot.getId());
        // cleanup roles
        for (UUID trusted : new HashSet<>(plot.getTrusted().keySet())) {
            roleManager.clearRole(plot.getId(), trusted);
        }
        saveAll();
    }

    public Collection<Plot> getAllPlots() {
        return plots.values();
    }

    public void saveAsync(Plot plot) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // TODO: persist to disk/db
        });
    }

    public void saveAll() {
        for (Plot p : plots.values()) {
            saveAsync(p);
        }
    }

    /**
     * Utility: is this UUID trusted or owner at a given Location.
     */
    public boolean isTrustedOrOwner(UUID player, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return false;
        if (plot.isOwner(player)) return true;
        return plot.getTrusted().containsKey(player);
    }
}
