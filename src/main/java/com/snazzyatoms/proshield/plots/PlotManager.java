package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
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
 * - Adds saveAsync(plot) + saveAsync() overloads
 * - Restores getX(), getZ() usage from Plot
 * - Fixes world lookups (use names/UUID, not Optional)
 * - Adds purgeExpired for /proshield purge
 * - Standardized getPlot/getClaim overloads for Location + Chunk
 * - Added helpers for ownership and trust lookups with both Location and Chunk
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

    /** Overload for Location (to fix mismatches). */
    public Plot getPlot(Location loc) {
        if (loc == null) return null;
        return getPlot(loc.getWorld().getName(), loc.getChunk().getX(), loc.getChunk().getZ());
    }

    public Plot getPlot(String world, int x, int z) {
        return claims.getOrDefault(world, Collections.emptyMap()).get(key(x, z));
    }

    /** Alias for readability when working with claims. */
    public Plot getClaim(Location loc) {
        return getPlot(loc);
    }

    public boolean isClaimed(Location loc) {
        return getPlot(loc) != null;
    }

    public boolean isClaimed(Chunk chunk) {
        return getPlot(chunk) != null;
    }

    /** Ownership check by Location. */
    public boolean isOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        return plot != null && playerId != null && playerId.equals(plot.getOwner());
    }

    /** Ownership check by Chunk. */
    public boolean isOwner(UUID playerId, Chunk chunk) {
        Plot plot = getPlot(chunk);
        return plot != null && playerId != null && playerId.equals(plot.getOwner());
    }

    /** Trusted or owner check (Location). */
    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null || playerId == null) return false;
        return playerId.equals(plot.getOwner()) || plot.getTrusted().containsKey(playerId);
    }

    /** Trusted or owner check (Chunk). */
    public boolean isTrustedOrOwner(UUID playerId, Chunk chunk) {
        Plot plot = getPlot(chunk);
        if (plot == null || playerId == null) return false;
        return playerId.equals(plot.getOwner()) || plot.getTrusted().containsKey(playerId);
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
        for (String key : new HashSet<>(yaml.getKeys(false))) {
            yaml.set(key, null); // wipe
        }
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
        // Currently no dynamic values per claim, placeholder for future
        plugin.getLogger().info("[ProShield] PlotManager reloaded settings.");
    }

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
}
