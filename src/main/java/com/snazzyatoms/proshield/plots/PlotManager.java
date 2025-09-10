package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager - runtime registry + persistence facade.
 * This version preserves prior public API (getPlot/getClaim synonyms, saveAsync(), etc.)
 * and adds missing helpers referenced in listeners/commands.
 */
public class PlotManager {

    private final ProShield plugin;

    // world:chunkX:chunkZ -> Plot
    private final Map<String, Plot> claims = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        // TODO: load from disk (config or data file) - preserved assumption
    }

    /* -------------------------
     * Keys & lookups
     * ------------------------- */
    private static String key(UUID worldId, int x, int z) {
        return worldId + ":" + x + ":" + z;
    }

    public Plot getPlot(Chunk chunk) {
        if (chunk == null) return null;
        UUID worldId = chunk.getWorld().getUID();
        return claims.get(key(worldId, chunk.getX(), chunk.getZ()));
    }

    /** Back-compat synonym used in a few places. */
    public Plot getClaim(Chunk chunk) { return getPlot(chunk); }

    /** Convenience for some older call sites that pass Location. */
    public Plot getPlot(Location loc) {
        if (loc == null) return null;
        return getPlot(loc.getChunk());
    }

    /** Back-compat synonym. */
    public Plot getClaim(Location loc) { return getPlot(loc); }

    public boolean isClaimed(Location loc) { return getPlot(loc) != null; }

    public String getClaimName(Location loc) {
        Plot p = getPlot(loc);
        return p != null ? p.getDisplayNameSafe() : "Wilderness";
    }

    public boolean hasAnyClaim(UUID owner) {
        if (owner == null) return false;
        for (Plot p : claims.values()) {
            if (owner.equals(p.getOwner())) return true;
        }
        return false;
    }

    /* -------------------------
     * Create / remove / transfer
     * ------------------------- */
    public Plot createClaim(UUID owner, Location at) {
        if (owner == null || at == null) return null;
        Chunk c = at.getChunk();
        UUID world = c.getWorld().getUID();
        String k = key(world, c.getX(), c.getZ());
        if (claims.containsKey(k)) return claims.get(k);

        Plot plot = new Plot(world, c.getX(), c.getZ(), owner);
        // initial settings could be synced from config here if needed
        claims.put(k, plot);
        saveAsync(plot);
        return plot;
    }

    public boolean unclaim(Location at, UUID requester) {
        if (at == null) return false;
        Chunk c = at.getChunk();
        Plot p = getPlot(c);
        if (p == null) return false;
        if (requester != null && !Objects.equals(p.getOwner(), requester)) return false;

        claims.remove(key(c.getWorld().getUID(), c.getX(), c.getZ()));
        saveAsync(); // persist snapshot
        return true;
    }

    public boolean transferOwnership(Plot plot, UUID newOwner) {
        if (plot == null || newOwner == null) return false;
        plot.setOwner(newOwner);
        plot.setDirty(true);
        saveAsync(plot);
        return true;
    }

    public boolean isOwner(UUID playerId, Location loc) {
        Plot p = getPlot(loc);
        return p != null && p.isOwner(playerId);
    }

    /* -------------------------
     * Persistence
     * ------------------------- */

    /** Persist a single plot (sync call used only from async wrapper). */
    public void savePlot(Plot plot) {
        if (plot == null) return;
        // TODO: serialize plot to storage; placeholder keeps API intact
        plot.setDirty(false);
    }

    /** Persist all plots (sync). */
    public void saveAll() {
        // TODO: serialize entire map; placeholder keeps API intact
        for (Plot p : claims.values()) {
            p.setDirty(false);
        }
    }

    /** Async wrapper used by callers that changed one plot. */
    public void saveAsync(Plot plot) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> savePlot(plot));
    }

    /** Async persist whole registry. */
    public void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveAll);
    }

    /** Hooked by /proshield reload to re-sync settings if needed. */
    public void reloadFromConfig() {
        // In a fuller impl, push global defaults into each PlotSettings where not overridden.
        // Kept as no-op to preserve existing behavior; method exists to satisfy callers.
    }

    /* -------------------------
     * Iteration / queries
     * ------------------------- */
    public Collection<Plot> getAll() {
        return Collections.unmodifiableCollection(claims.values());
    }

    public Optional<Plot> getAt(UUID worldId, int x, int z) {
        return Optional.ofNullable(claims.get(key(worldId, x, z)));
    }

    /* -------------------------
     * Utilities for UI/debug
     * ------------------------- */
    public String formatCoord(Plot p) {
        return "(" + p.getX() + "," + p.getZ() + ")";
    }
}
