package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager
 * - Stores/loads plots
 * - Query helpers
 * - Preserves previous behavior; extended with missing helpers to satisfy listeners/commands.
 */
public class PlotManager {

    private final ProShield plugin;

    // Chunk key -> Plot
    private final Map<Long, Plot> plots = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadFromConfig(plugin.getConfig());
    }

    /* =======================
     * Loading / Saving
     * ======================= */

    public synchronized void reloadFromConfig() {
        plots.clear();
        loadFromConfig(plugin.getConfig());
    }

    public synchronized void saveAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveNow);
    }

    private synchronized void saveNow() {
        // Write into config.yml under "claims"
        FileConfiguration cfg = plugin.getConfig();
        // serialize plots into a map
        Map<String, Object> claims = new LinkedHashMap<>();
        for (Plot p : plots.values()) {
            claims.put(p.getChunkKeyString(), p.serialize());
        }
        cfg.set("claims", claims);
        plugin.saveConfig();
    }

    private void loadFromConfig(FileConfiguration cfg) {
        // read "claims" section; tolerant if missing
        Object raw = cfg.get("claims");
        if (!(raw instanceof Map<?, ?> map)) return;

        for (Map.Entry<?, ?> e : map.entrySet()) {
            String key = String.valueOf(e.getKey());
            Object val = e.getValue();
            if (val instanceof Map<?, ?> data) {
                Plot p = Plot.deserialize(key, data, plugin);
                if (p != null) plots.put(p.getChunkKey(), p);
            }
        }
    }

    /* =======================
     * Core CRUD
     * ======================= */

    public Plot getPlot(Chunk chunk) {
        return plots.get(chunkKey(chunk));
    }

    /** Alias used by many listeners */
    public Plot getClaim(Location loc) {
        if (loc == null) return null;
        return getPlot(loc.getChunk());
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public boolean hasAnyClaim(UUID owner) {
        if (owner == null) return false;
        for (Plot p : plots.values()) {
            if (owner.equals(p.getOwner())) return true;
        }
        return false;
    }

    public Plot createClaim(UUID owner, Location at) {
        if (owner == null || at == null || at.getWorld() == null) return null;
        Chunk chunk = at.getChunk();
        long key = chunkKey(chunk);
        if (plots.containsKey(key)) return null;

        Plot p = Plot.create(owner, chunk, plugin);
        plots.put(key, p);
        saveAsync();
        return p;
    }

    public boolean unclaim(Location at) {
        Plot p = getClaim(at);
        if (p == null) return false;
        plots.remove(p.getChunkKey());
        saveAsync();
        return true;
    }

    public boolean transferOwnership(Plot plot, String newOwnerName) {
        if (plot == null || newOwnerName == null || newOwnerName.isEmpty()) return false;
        plot.setOwnerName(newOwnerName); // resolves UUID async inside Plot (preserved behavior)
        saveAsync();
        return true;
    }

    /* =======================
     * Helpers referenced in build log
     * ======================= */

    public String getClaimName(Location loc) {
        Plot p = getClaim(loc);
        if (p == null) return null;
        return p.getName(); // Plot#getName() provided in Plot.java
    }

    public boolean isOwner(UUID uuid, Location loc) {
        Plot p = getClaim(loc);
        return p != null && p.isOwner(uuid);
    }

    public boolean isTrustedOrOwner(UUID uuid, Location loc) {
        Plot p = getClaim(loc);
        if (p == null || uuid == null) return false;
        if (p.isOwner(uuid)) return true;
        ClaimRole r = p.getTrustedRole(uuid);
        return r != null && r != ClaimRole.VISITOR;
    }

    /* =======================
     * Utility
     * ======================= */

    private static long chunkKey(Chunk c) {
        return ((long) c.getX() & 0xffffffffL) << 32 | ((long) c.getZ() & 0xffffffffL);
    }
}
