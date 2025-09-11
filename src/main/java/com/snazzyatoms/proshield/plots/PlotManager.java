package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager - central storage and persistence for plots.
 *
 * Preserves prior logic:
 * - Loads/saves plots
 * - Async saving
 * - Lookups by Location/Chunk
 * - Claim/unclaim support
 * - Helpers for ownership/trust checks
 * - Expired purge & reload hooks
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new ConcurrentHashMap<>();
    private final File file;
    private final FileConfiguration config;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "plots.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        loadAll();
    }

    /* -------------------------------------------------------
     * Keys & Helpers
     * ------------------------------------------------------- */

    private String key(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + "," + loc.getChunk().getX() + "," + loc.getChunk().getZ();
    }

    /* -------------------------------------------------------
     * Getters
     * ------------------------------------------------------- */

    public Plot getPlot(Chunk chunk) {
        return plots.get(key(chunk));
    }

    public Plot getPlot(Location loc) {
        return plots.get(key(loc));
    }

    /** Alias for backwards compatibility */
    public Plot getClaim(Location loc) {
        return getPlot(loc);
    }

    public boolean hasAnyClaim(UUID playerId) {
        for (Plot plot : plots.values()) {
            if (plot.isOwner(playerId)) return true;
        }
        return false;
    }

    public String getClaimName(Location loc) {
        Plot plot = getPlot(loc);
        return (plot != null) ? plot.getDisplayNameSafe() : null;
    }

    /* -------------------------------------------------------
     * Ownership / Trust helpers
     * ------------------------------------------------------- */

    public boolean isOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        return plot != null && plot.isOwner(playerId);
    }

    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        return plot != null && (plot.isOwner(playerId) || plot.getTrusted().containsKey(playerId));
    }

    /* -------------------------------------------------------
     * Claim / Unclaim
     * ------------------------------------------------------- */

    public Plot createClaim(UUID owner, Location loc) {
        Chunk chunk = loc.getChunk();
        String k = key(chunk);
        if (plots.containsKey(k)) return plots.get(k);

        Plot plot = new Plot(chunk, owner);
        plots.put(k, plot);
        saveAsync(plot);
        return plot;
    }

    public void unclaim(Chunk chunk) {
        String k = key(chunk);
        plots.remove(k);
        config.set(k, null);
        saveFile();
    }

    /* -------------------------------------------------------
     * Persistence
     * ------------------------------------------------------- */

    public void saveAsync(Plot plot) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> save(plot));
    }

    private void save(Plot plot) {
        String k = plot.getWorldName() + "," + plot.getX() + "," + plot.getZ();
        config.set(k, plot.serialize());
        saveFile();
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAll() {
        if (config.getKeys(false).isEmpty()) return;
        for (String key : config.getKeys(false)) {
            String[] parts = key.split(",");
            if (parts.length < 3) continue;
            String worldName = parts[0];
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            Chunk chunk = world.getChunkAt(x, z);
            Plot plot = Plot.deserialize(config.getConfigurationSection(key));
            if (plot != null) {
                plots.put(key, plot);
            }
        }
    }

    public Collection<Plot> getAllPlots() {
        return plots.values();
    }

    /* -------------------------------------------------------
     * Extra Management
     * ------------------------------------------------------- */

    /** Purge claims older than X days */
    public int purgeExpired(int days, boolean save) {
        long cutoff = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L);
        int removed = 0;
        Iterator<Map.Entry<String, Plot>> it = plots.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Plot> e = it.next();
            Plot plot = e.getValue();
            if (plot.getCreated() < cutoff) {
                it.remove();
                config.set(e.getKey(), null);
                removed++;
            }
        }
        if (save) saveFile();
        return removed;
    }

    /** Reload from config (stub for expansion) */
    public void reloadFromConfig() {
        // Reload default settings from config.yml if required
    }
}
