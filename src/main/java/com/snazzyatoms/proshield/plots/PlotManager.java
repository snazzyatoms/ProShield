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

public class PlotManager {

    private final ProShield plugin;
    private final ClaimRoleManager roleManager;

    // plotId -> Plot
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();

    // Admin bypass set
    private final Set<UUID> bypass = ConcurrentHashMap.newKeySet();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.roleManager = null;
    }

    public ProShield getPlugin() { return plugin; }

    public Collection<Plot> getAllPlots() {
        return Collections.unmodifiableCollection(plots.values());
    }

    public Plot getPlotById(UUID id) { return plots.get(id); }

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

    public Plot getPlotAt(Chunk chunk) { return getPlot(chunk); }

    public String getClaimName(Location location) {
        Plot plot = getPlot(location);
        return (plot != null) ? plot.getDisplayNameSafe() : "Wilderness";
    }

    public Plot createPlot(UUID owner, Chunk chunk) {
        Plot existing = getPlot(chunk);
        if (existing != null) return existing;
        Plot plot = new Plot(chunk, owner);
        plots.put(plot.getId(), plot);
        saveAsync(plot);
        return plot;
    }

    public void removePlot(Plot plot) {
        if (plot == null) return;
        plots.remove(plot.getId());
        // TODO persist removal
    }

    public void saveAsync(Plot plot) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // TODO persist plot
        });
    }

    public void saveAll() {
        for (Plot p : plots.values()) saveAsync(p);
    }

    // -----------------------------
    // Trust / Access helpers
    // -----------------------------
    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true;
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

    // -----------------------------
    // Admin bypass
    // -----------------------------
    public boolean isBypass(UUID playerId) { return bypass.contains(playerId); }

    public boolean toggleBypass(UUID playerId) {
        if (bypass.contains(playerId)) {
            bypass.remove(playerId);
            return false;
        } else {
            bypass.add(playerId);
            return true;
        }
    }
}
