// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * PlotManager
 * - Stores and manages all plots
 * - Handles creation, lookup, saving/loading
 * - Provides name/owner resolution for use in GUIs & messages
 * - Starts entity repel tasks if enabled in config
 *
 * Consolidated for v1.2.5+
 */
public class PlotManager {

    private final ProShield plugin;

    // Plots stored by ID
    private final Map<UUID, Plot> plots = new HashMap<>();

    // Chunk mapping: world+chunkX+chunkZ -> plotId
    private final Map<String, UUID> chunkMap = new HashMap<>();

    // Players in bypass mode
    private final Set<UUID> bypassing = new HashSet<>();

    // Repel tasks
    private EntityMobRepelTask mobRepelTask;
    private EntityBorderRepelTask borderRepelTask;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;

        // Start repel tasks if enabled
        if (plugin.getConfig().getBoolean("protection.mobs.repel.enabled", true)) {
            mobRepelTask = new EntityMobRepelTask(plugin, this);
            mobRepelTask.runTaskTimer(plugin, 20L, 40L); // every 2 seconds
        }

        if (plugin.getConfig().getBoolean("protection.mobs.border-repel.enabled", true)) {
            borderRepelTask = new EntityBorderRepelTask(plugin, this);
            borderRepelTask.runTaskTimer(plugin, 20L, 40L);
        }
    }

    /* ======================================================
     * PLOTS
     * ====================================================== */
    public Plot createPlot(UUID ownerId, Chunk chunk) {
        UUID id = UUID.randomUUID();
        Plot plot = new Plot(id, ownerId, chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        plots.put(id, plot);
        chunkMap.put(chunkKey(chunk), id);
        return plot;
    }

    public void removePlot(Plot plot) {
        plots.remove(plot.getId());
        chunkMap.remove(chunkKey(plot.getWorld(), plot.getX(), plot.getZ()));
    }

    public Plot getPlot(UUID plotId) {
        return plots.get(plotId);
    }

    public Plot getPlot(Chunk chunk) {
        UUID id = chunkMap.get(chunkKey(chunk));
        return id != null ? plots.get(id) : null;
    }

    public Plot getPlot(Location loc) {
        if (loc == null) return null;
        Chunk chunk = loc.getChunk();
        return getPlot(chunk);
    }

    /* ======================================================
     * CLAIM NAME HELPERS
     * ====================================================== */
    public String getClaimName(Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return "Wilderness";
        return plot.getDisplayNameSafe();
    }

    /* ======================================================
     * PLAYER HELPERS
     * ====================================================== */
    public String getPlayerName(UUID uuid) {
        OfflinePlayer offline = plugin.getServer().getOfflinePlayer(uuid);
        return (offline != null && offline.getName() != null) ? offline.getName() : uuid.toString();
    }

    /* ======================================================
     * BYPASS MODE
     * ====================================================== */
    public boolean toggleBypass(UUID playerId) {
        if (bypassing.contains(playerId)) {
            bypassing.remove(playerId);
            return false;
        } else {
            bypassing.add(playerId);
            return true;
        }
    }

    public boolean isBypassing(UUID playerId) {
        return bypassing.contains(playerId);
    }

    /* ======================================================
     * SAVE / LOAD
     * ====================================================== */
    public void saveAll() {
        // TODO: serialize plots to disk (YAML or JSON)
    }

    public void loadAll() {
        // TODO: load plots from disk
    }

    /* ======================================================
     * INTERNAL HELPERS
     * ====================================================== */
    private String chunkKey(Chunk chunk) {
        return chunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    private String chunkKey(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }
}
