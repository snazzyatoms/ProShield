// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager
 * - Manages all plots in the world.
 * - Handles creation, lookup, saving, and trusted role access.
 * - Now includes expiry & purge system (config-driven).
 */
public class PlotManager {

    private final ProShield plugin;
    private final ClaimRoleManager roleManager;

    // Map of plotId -> Plot
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();

    // Expiry (in days) loaded from config
    private final int expiryDays;

    /**
     * Modern constructor (preferred).
     */
    public PlotManager(ProShield plugin, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.roleManager = roleManager;
        this.expiryDays = plugin.getConfig().getInt("claims.expiry-days", 0);

        // Start expiry task if enabled
        if (expiryDays > 0) {
            long ticks = 20L * 60L * 60L; // every 1 hour
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::purgeExpiredClaims, ticks, ticks);
            plugin.getLogger().info("Claim expiry enabled: " + expiryDays + " days");
        } else {
            plugin.getLogger().info("Claim expiry is disabled.");
        }
    }

    /**
     * Legacy constructor for compatibility.
     * RoleManager will be null if not passed.
     */
    public PlotManager(ProShield plugin) {
        this(plugin, null);
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

    /**
     * Alias for backwards compatibility.
     * Older code calls getPlotAt(...).
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

    public Plot createPlot(UUID owner, Chunk chunk) {
        Plot plot = new Plot(chunk, owner);
        plot.setLastActive(Instant.now()); // mark as active when created
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
     * Mark a claim as active (e.g., when player interacts with it).
     */
    public void markActive(Plot plot) {
        if (plot != null) {
            plot.setLastActive(Instant.now());
        }
    }

    /**
     * Purge expired claims based on expiry-days in config.
     */
    public void purgeExpiredClaims() {
        if (expiryDays <= 0) return; // disabled

        long cutoff = Instant.now().minusSeconds(expiryDays * 86400L).toEpochMilli();
        int purgedCount = 0;

        for (Iterator<Plot> it = plots.values().iterator(); it.hasNext(); ) {
            Plot plot = it.next();
            if (plot.getLastActive() != null && plot.getLastActive().toEpochMilli() < cutoff) {
                it.remove();
                purgedCount++;
                plugin.getLogger().info("Expired claim purged: " + plot.getDisplayNameSafe());
            }
        }

        if (purgedCount > 0) {
            plugin.getLogger().info("Purged " + purgedCount + " expired claims.");
        }
    }

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
