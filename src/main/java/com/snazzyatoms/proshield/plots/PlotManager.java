package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;

/**
 * Manages all plots (claims) in the server.
 * Provides helper methods to query, create, and manage claims.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>(); // chunkKey -> plot

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /* -------------------------------
     * Core claim management
     * ------------------------------- */

    public Plot getPlot(Chunk chunk) {
        return plots.get(getChunkKey(chunk));
    }

    public Plot getClaim(Location loc) {
        return getPlot(loc.getChunk());
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public Plot createClaim(UUID owner, Location loc) {
        Chunk chunk = loc.getChunk();
        String key = getChunkKey(chunk);
        if (plots.containsKey(key)) {
            return plots.get(key);
        }
        Plot newPlot = new Plot(owner, "Claim-" + key);
        plots.put(key, newPlot);
        return newPlot;
    }

    public void unclaim(Location loc) {
        plots.remove(getChunkKey(loc.getChunk()));
    }

    public String getClaimName(Location loc) {
        Plot plot = getClaim(loc);
        return (plot != null) ? plot.getName() : "Wilderness";
    }

    public boolean isOwner(UUID uuid, Location loc) {
        Plot plot = getClaim(loc);
        return plot != null && plot.isOwner(uuid);
    }

    public boolean isTrustedOrOwner(UUID uuid, Location loc) {
        Plot plot = getClaim(loc);
        if (plot == null) return false;
        return plot.isOwner(uuid) || plot.isTrusted(uuid);
    }

    public boolean hasAnyClaim(UUID uuid) {
        return plots.values().stream().anyMatch(p -> p.isOwner(uuid));
    }

    public void transferOwnership(Plot plot, UUID newOwner) {
        if (plot == null) return;
        Plot updated = new Plot(newOwner, plot.getName());
        updated.getSettings().setFlag("pvp", plot.getSettings().isPvpEnabled()); // example copy
        plots.put(getChunkKeyByPlot(plot), updated);
    }

    public int purgeExpired(int days, boolean dryRun) {
        // Future: implement expiry tracking
        return 0;
    }

    /* -------------------------------
     * Helpers
     * ------------------------------- */

    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    private String getChunkKeyByPlot(Plot plot) {
        // In practice you'd track chunk keys inside Plot itself
        return plot.getName();
    }

    public void reloadFromConfig() {
        // TODO: Sync plots from config if persistence is implemented
    }
}
