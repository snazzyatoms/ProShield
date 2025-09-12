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
 * - Manages all plots in the world.
 * - Handles creation, lookup, saving, and trusted role access.
 * - Unified from v1.2.0 → v1.2.5 with dynamic flag initialization.
 */
public class PlotManager {

    private final ProShield plugin;
    private final ClaimRoleManager roleManager;

    // Map of plotId -> Plot
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.roleManager = null; // fallback if role manager not injected
    }

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

    /**
     * Alias for backwards compatibility.
     */
    public Plot getPlotAt(Chunk chunk) {
        return getPlot(chunk);
    }

    /**
     * Get a safe display name for the claim at this location.
     * Returns "Wilderness" if unclaimed.
     */
    public String getClaimName(Location location) {
        Plot plot = getPlot(location);
        return (plot != null) ? plot.getDisplayNameSafe() : "Wilderness";
    }

    /**
     * Create a new plot and initialize with default flags from config.yml.
     */
    public Plot createPlot(UUID owner, Chunk chunk) {
        Plot plot = new Plot(chunk, owner);

        // Initialize flags from config (claims.default-flags.*)
        if (plugin.getConfig().isConfigurationSection("claims.default-flags")) {
            for (String key : plugin.getConfig().getConfigurationSection("claims.default-flags").getKeys(false)) {
                boolean def = plugin.getConfig().getBoolean("claims.default-flags." + key, false);
                plot.setFlag(key, def);
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
            // TODO: save plot to disk or database
        });
    }

    public void saveAll() {
        for (Plot plot : plots.values()) {
            saveAsync(plot);
        }
    }

    /* -------------------------------------------------------
     * Role / Trust Checks
     * ------------------------------------------------------- */

    /**
     * Checks if a player is trusted or the owner of a claim.
     */
    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true; // not claimed
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
