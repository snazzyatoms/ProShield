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
 * - Future-proofed with aliases for legacy method names.
 * - Now supports claim expiry & auto-purging (configurable).
 */
public class PlotManager {

    private final ProShield plugin;
    private final ClaimRoleManager roleManager;

    // Map of plotId -> Plot
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();

    /**
     * Modern constructor (preferred).
     */
    public PlotManager(ProShield plugin, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.roleManager = roleManager;
    }

    /**
     * Legacy constructor for compatibility.
     * RoleManager will be null if not passed.
     */
    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.roleManager = null;
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
        plot.updateActivity(); // mark creation as activity
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

    /* ======================================================
     * CLAIM EXPIRY & PURGE
     * ====================================================== */

    /**
     * Purge expired claims based on config expiry-days.
     */
    public void purgeExpiredClaims() {
        int expiryDays = plugin.getConfig().getInt("claims.expiry-days", 30);
        long expiryMillis = expiryDays * 24L * 60L * 60L * 1000L;

        long now = System.currentTimeMillis();
        int purged = 0;

        Iterator<Plot> it = plots.values().iterator();
        while (it.hasNext()) {
            Plot plot = it.next();
            long lastActive = plot.getLastActive(); // requires Plot support
            if (now - lastActive >= expiryMillis) {
                it.remove();
                purged++;
                plugin.getLogger().info("Purged expired claim: " + plot.getDisplayNameSafe());
            }
        }

        if (purged > 0) {
            int finalPurged = purged;
            Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.broadcastMessage("§c" + finalPurged + " expired claims were purged.")
            );
        }
    }
}
