package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * PlotManager
 * -----------
 * - Keeps an index of plots by ID and by (world, chunkX, chunkZ)
 * - Utility methods used across commands, GUI, roles, and expansion system
 * - Provides convenience overloads (by Location) to keep older call sites working
 *
 * NOTE: This manager does not send chat messages; callers decide UX.
 */
public class PlotManager {

    private final ProShield plugin;

    /** Primary storage by unique plot ID */
    private final Map<UUID, Plot> plotsById = new HashMap<>();
    /** Secondary index from world:cx:cz -> plotId */
    private final Map<String, UUID> byChunk = new HashMap<>();
    /** Optional reverse index for owner -> plot IDs (supports multi-plot owners) */
    private final Map<UUID, Set<UUID>> plotsByOwner = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* ======================
     * Basic indexing helpers
     * ====================== */
    private String key(String worldName, int cx, int cz) {
        return worldName + ":" + cx + ":" + cz;
    }

    /* =====================
     * Lookup / read methods
     * ===================== */

    /** Get a plot at a given Location (null if chunk is unclaimed). */
    public Plot getPlot(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        Chunk c = loc.getChunk();
        String k = key(loc.getWorld().getName(), c.getX(), c.getZ());
        UUID id = byChunk.get(k);
        return (id == null) ? null : plotsById.get(id);
    }

    /** Get a plot by its unique ID (null if not found). */
    public Plot getPlotById(UUID plotId) {
        if (plotId == null) return null;
        return plotsById.get(plotId);
    }

    /** Convenience: find first plot owned by a specific player (may be null). */
    public Plot getAnyPlotOwnedBy(UUID ownerId) {
        Set<UUID> ids = plotsByOwner.get(ownerId);
        if (ids == null || ids.isEmpty()) return null;
        for (UUID id : ids) {
            Plot p = plotsById.get(id);
            if (p != null) return p;
        }
        return null;
    }

    /** Resolve a player's last known name (best-effort). */
    public String getPlayerName(UUID playerId) {
        if (playerId == null) return "unknown";
        OfflinePlayer off = Bukkit.getOfflinePlayer(playerId);
        return (off != null && off.getName() != null) ? off.getName() : playerId.toString();
    }

    /* =========================
     * Create / remove / mutate
     * ========================= */

    /**
     * Create a plot at the player's current chunk.
     * Returns the created plot, or null if that chunk is already claimed.
     */
    public Plot createPlot(UUID ownerId, Location loc) {
        if (ownerId == null || loc == null || loc.getWorld() == null) return null;

        Chunk c = loc.getChunk();
        String world = loc.getWorld().getName();
        int cx = c.getX();
        int cz = c.getZ();
        String k = key(world, cx, cz);

        if (byChunk.containsKey(k)) {
            // Already claimed; caller should decide what to tell the user
            return null;
        }

        Plot plot = new Plot(ownerId, world, cx, cz);

        plotsById.put(plot.getId(), plot);
        byChunk.put(k, plot.getId());
        plotsByOwner.computeIfAbsent(ownerId, _x -> new HashSet<>()).add(plot.getId());
        return plot;
    }

    /**
     * Remove the plot in the given chunk.
     * Returns true if a plot existed and was removed.
     */
    public boolean removePlot(Location loc) {
        Plot p = getPlot(loc);
        if (p == null) return false;
        return removePlot(p);
    }

    /**
     * Remove the provided plot object.
     * Returns true when removed from all indexes.
     */
    public boolean removePlot(Plot plot) {
        if (plot == null) return false;

        UUID id = plot.getId();
        Plot removed = plotsById.remove(id);
        if (removed == null) return false;

        String k = key(plot.getWorldName(), plot.getX(), plot.getZ());
        byChunk.remove(k);

        Set<UUID> owned = plotsByOwner.get(plot.getOwner());
        if (owned != null) {
            owned.remove(id);
            if (owned.isEmpty()) plotsByOwner.remove(plot.getOwner());
        }
        return true;
    }

    /**
     * Expand (add extra radius) for one of the owner's plots.
     * If owner has multiple plots, we expand the first found.
     * Returns true if a plot was found & expanded.
     */
    public boolean expandClaim(UUID ownerId, int extraRadius) {
        if (ownerId == null || extraRadius <= 0) return false;
        Plot p = getAnyPlotOwnedBy(ownerId);
        if (p == null) return false;
        p.expand(extraRadius);
        return true;
    }

    /**
     * Transfer ownership of a plot by ID (utility kept for completeness).
     * Returns true if success.
     */
    public boolean transferOwnership(UUID plotId, UUID newOwnerId) {
        Plot p = getPlotById(plotId);
        if (p == null || newOwnerId == null) return false;

        UUID oldOwner = p.getOwner();
        if (Objects.equals(oldOwner, newOwnerId)) return true; // no change

        // Update reverse index
        if (oldOwner != null) {
            Set<UUID> set = plotsByOwner.get(oldOwner);
            if (set != null) {
                set.remove(p.getId());
                if (set.isEmpty()) plotsByOwner.remove(oldOwner);
            }
        }
        p.setOwner(newOwnerId);
        plotsByOwner.computeIfAbsent(newOwnerId, _x -> new HashSet<>()).add(p.getId());
        return true;
    }

    /* ==================
     * Persistence hooks
     * ==================
     * These are placeholders to be implemented (YAML/JSON/DB). We keep
     * the stubs so nothing breaks and we can add serialization later.
     */

    public void loadAll() {
        // TODO: Load plotsById, byChunk, plotsByOwner from disk.
        // Keep stub for now; caller expects method to exist.
    }

    public void saveAll() {
        // TODO: Save plotsById, byChunk, plotsByOwner to disk.
        // Keep stub for now; caller expects method to exist.
    }

    /* ===============
     * Debug utilities
     * =============== */

    public int getTotalPlots() {
        return plotsById.size();
    }
}
