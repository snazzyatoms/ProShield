// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager
 * - Manages all plots in the world.
 * - Handles creation, lookup, saving, and trusted role access.
 * - Unified with flag management.
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

    /** Alias for backwards compatibility */
    public Plot getPlotAt(Chunk chunk) {
        return getPlot(chunk);
    }

    /** Get safe display name (or Wilderness if none). */
    public String getClaimName(Location location) {
        Plot plot = getPlot(location);
        return (plot != null) ? plot.getDisplayNameSafe() : "Wilderness";
    }

    public Plot createPlot(UUID owner, Chunk chunk) {
        Plot plot = new Plot(chunk, owner);

        // Load default flags from config
        ConfigurationSection defaults = plugin.getConfig().getConfigurationSection("claims.default-flags");
        if (defaults != null) {
            for (String key : defaults.getKeys(false)) {
                boolean val = defaults.getBoolean(key, false);
                plot.setFlag(key, val);
            }
        }

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

    /* -------------------------------------------------------
     * Flag management
     * ------------------------------------------------------- */

    public boolean getFlag(Location loc, String flag) {
        Plot plot = getPlot(loc);
        if (plot == null) return false;
        return plot.getFlag(flag);
    }

    public void setFlag(Location loc, String flag, boolean state) {
        Plot plot = getPlot(loc);
        if (plot != null) {
            plot.setFlag(flag, state);
            saveAsync(plot);
        }
    }

    public boolean toggleFlag(Location loc, String flag) {
        Plot plot = getPlot(loc);
        if (plot == null) return false;
        boolean newState = plot.toggleFlag(flag);
        saveAsync(plot);
        return newState;
    }

    /* -------------------------------------------------------
     * Permission helpers
     * ------------------------------------------------------- */

    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true; // not claimed
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
