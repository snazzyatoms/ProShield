package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager handles all claim storage, lookup, and persistence.
 *
 * Preserves prior logic and enhances:
 * - Uses world name (String) not World object for keys
 * - Provides saveAsync(plot) and saveAsync() overloads
 * - Adds purgeExpired and getClaimName
 * - Ensures Plot.getX()/getZ() are available
 */
public class PlotManager {

    private final ProShield plugin;

    // Claims indexed by world + chunk coords
    private final Map<String, Map<String, Plot>> claims = new ConcurrentHashMap<>();

    private final File dataFile;
    private final YamlConfiguration yaml;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "claims.yml");
        this.yaml = YamlConfiguration.loadConfiguration(dataFile);
        loadClaims();
    }

    /* -------------------------------------------------------
     * Claim CRUD
     * ------------------------------------------------------- */

    public Plot getPlot(Chunk chunk) {
        if (chunk == null) return null;
        return getPlot(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public Plot getPlot(String world, int x, int z) {
        return claims.getOrDefault(world, Collections.emptyMap()).get(key(x, z));
    }

    public Plot getClaim(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        return getPlot(loc.getWorld().getName(), loc.getChunk().getX(), loc.getChunk().getZ());
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public Collection<Plot> getClaims() {
        List<Plot> list = new ArrayList<>();
        for (Map<String, Plot> perWorld : claims.values()) {
            list.addAll(perWorld.values());
        }
        return list;
    }

    public void addPlot(Plot plot) {
        claims.computeIfAbsent(plot.getWorldName(), w -> new ConcurrentHashMap<>())
                .put(key(plot.getX(), plot.getZ()), plot);
        saveAsync(plot);
    }

    public void removePlot(Plot plot) {
        Map<String, Plot> worldClaims = claims.get(plot.getWorldName());
        if (worldClaims != null) {
            worldClaims.remove(key(plot.getX(), plot.getZ()));
        }
        saveAsync();
    }

    /* -------------------------------------------------------
     * Persistence
     * ------------------------------------------------------- */

    private void loadClaims() {
        if (!dataFile.exists()) return;

        for (String world : yaml.getKeys(false)) {
            for (String chunkKey : yaml.getConfigurationSection(world).getKeys(false)) {
                Plot plot = Plot.deserialize(yaml.getConfigurationSection(world + "." + chunkKey));
                if (plot != null) {
                    claims.computeIfAbsent(world, w -> new ConcurrentHashMap<>())
                            .put(chunkKey, plot);
                }
            }
        }
        plugin.getLogger().info("[ProShield] Loaded " + getClaims().size() + " claims.");
    }

    public void saveAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAll();
            }
        }.runTaskAsynchronously(plugin);
    }

    public void saveAsync(Plot plot) {
        if (plot == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                savePlot(plot);
            }
        }.runTaskAsynchronously(plugin);
    }

    private synchronized void saveAll() {
        // clear file then write
        yaml.getKeys(false).forEach(yaml::set);
        for (Map.Entry<String, Map<String, Plot>> worldEntry : claims.entrySet()) {
            String world = worldEntry.getKey();
            for (Plot plot : worldEntry.getValue().values()) {
                yaml.createSection(world + "." + key(plot.getX(), plot.getZ()), plot.serialize());
            }
        }
        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[ProShield] Failed to save claims.yml: " + e.getMessage());
        }
    }

    private synchronized void savePlot(Plot plot) {
        String path = plot.getWorldName() + "." + key(plot.getX(), plot.getZ());
        yaml.createSection(path, plot.serialize());
        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[ProShield] Failed to save claim: " + e.getMessage());
        }
    }

    /* -------------------------------------------------------
     * Maintenance
     * ------------------------------------------------------- */

    public void reloadFromConfig() {
        plugin.getLogger().info("[ProShield] PlotManager reloaded settings.");
    }

    /**
     * Purge expired claims.
     */
    public int purgeExpired(int daysOld, boolean unowned) {
        int purged = 0;
        long cutoff = (daysOld > 0) ? (System.currentTimeMillis() - (daysOld * 86_400_000L)) : 0L;

        for (Map<String, Plot> worldClaims : claims.values()) {
            Iterator<Plot> it = worldClaims.values().iterator();
            while (it.hasNext()) {
                Plot plot = it.next();
                boolean remove = false;

                if (unowned && plot.getOwner() == null) {
                    remove = true;
                } else if (daysOld > 0 && plot.getCreated() < cutoff) {
                    remove = true;
                }

                if (remove) {
                    it.remove();
                    purged++;
                }
            }
        }

        if (purged > 0) saveAsync();
        return purged;
    }

    /* -------------------------------------------------------
     * Helpers
     * ------------------------------------------------------- */

    private String key(int x, int z) {
        return x + "," + z;
    }

    public String getClaimName(Location loc) {
        Plot plot = getClaim(loc);
        return (plot != null) ? plot.getDisplayNameSafe() : null;
    }
}
