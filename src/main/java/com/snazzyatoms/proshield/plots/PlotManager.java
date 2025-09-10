package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlotManager – claim storage, lookup, helpers, and persistence.
 * Preserves earlier behavior + reintroduces helpers used by listeners/commands.
 */
public class PlotManager {

    private final ProShield plugin;

    // worldName -> "x,z" -> plot
    private final Map<String, Map<String, Plot>> claims = new ConcurrentHashMap<>();

    private final File dataFile;
    private final YamlConfiguration yaml;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "claims.yml");
        this.yaml = YamlConfiguration.loadConfiguration(dataFile);
        loadClaims();
    }

    /* ---------------------- Queries ---------------------- */

    public Plot getPlot(Chunk chunk) {
        if (chunk == null) return null;
        return getPlot(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public Plot getPlot(String world, int x, int z) {
        return claims.getOrDefault(world, Collections.emptyMap()).get(key(x, z));
    }

    /** Compatibility alias used widely in the codebase. */
    public Plot getClaim(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        return getPlot(loc.getWorld().getName(), loc.getChunk().getX(), loc.getChunk().getZ());
    }

    public boolean isClaimed(Location loc) {
        return getClaim(loc) != null;
    }

    public boolean hasAnyClaim(UUID playerId) {
        if (playerId == null) return false;
        for (Plot p : getAllClaims()) {
            if (playerId.equals(p.getOwner())) return true;
            if (p.getTrusted().containsKey(playerId)) return true;
        }
        return false;
    }

    public boolean isOwner(UUID playerId, Location loc) {
        Plot p = getClaim(loc);
        return p != null && p.isOwner(playerId);
    }

    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot p = getClaim(loc);
        if (p == null || playerId == null) return false;
        if (p.isOwner(playerId)) return true;
        return p.getTrusted().containsKey(playerId);
    }

    public String getClaimName(Location loc) {
        Plot p = getClaim(loc);
        return p != null ? p.getDisplayNameSafe() : null;
    }

    public Collection<Plot> getAllClaims() {
        List<Plot> out = new ArrayList<>();
        for (Map<String, Plot> perWorld : claims.values()) out.addAll(perWorld.values());
        return out;
    }

    /* ---------------------- CRUD ---------------------- */

    public Plot createClaim(UUID owner, Location loc) {
        if (owner == null || loc == null || loc.getWorld() == null) return null;
        Plot existing = getClaim(loc);
        if (existing != null) return existing;

        Plot plot = new Plot(loc.getChunk(), owner);
        claims.computeIfAbsent(plot.getWorldName(), w -> new ConcurrentHashMap<>())
                .put(key(plot.getX(), plot.getZ()), plot);
        saveAsync(plot);
        return plot;
    }

    public void addPlot(Plot plot) {
        if (plot == null) return;
        claims.computeIfAbsent(plot.getWorldName(), w -> new ConcurrentHashMap<>())
                .put(key(plot.getX(), plot.getZ()), plot);
        saveAsync(plot);
    }

    public void removePlot(Plot plot) {
        if (plot == null) return;
        Map<String, Plot> perWorld = claims.get(plot.getWorldName());
        if (perWorld != null) perWorld.remove(key(plot.getX(), plot.getZ()));
        saveAsync();
    }

    /* ---------------------- Persistence ---------------------- */

    private void loadClaims() {
        if (!dataFile.exists()) return;

        for (String world : yaml.getKeys(false)) {
            ConfigurationSection worldSec = yaml.getConfigurationSection(world);
            if (worldSec == null) continue;
            for (String chunkKey : worldSec.getKeys(false)) {
                ConfigurationSection sec = worldSec.getConfigurationSection(chunkKey);
                if (sec == null) continue;
                Plot plot = Plot.deserialize(sec);
                if (plot != null) {
                    claims.computeIfAbsent(world, w -> new ConcurrentHashMap<>())
                            .put(chunkKey, plot);
                }
            }
        }
        plugin.getLogger().info("[ProShield] Loaded " + getAllClaims().size() + " claims.");
    }

    public void saveAsync() {
        new BukkitRunnable() {
            @Override public void run() { saveAll(); }
        }.runTaskAsynchronously(plugin);
    }

    public void saveAsync(Plot plot) {
        if (plot == null) return;
        new BukkitRunnable() {
            @Override public void run() { savePlot(plot); }
        }.runTaskAsynchronously(plugin);
    }

    private synchronized void saveAll() {
        // Clear current
        for (String k : new HashSet<>(yaml.getKeys(false))) {
            yaml.set(k, null);
        }
        for (Map.Entry<String, Map<String, Plot>> e : claims.entrySet()) {
            String world = e.getKey();
            for (Plot p : e.getValue().values()) {
                yaml.createSection(world + "." + key(p.getX(), p.getZ()), p.serialize());
            }
        }
        try {
            yaml.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("[ProShield] Failed to save claims.yml: " + ex.getMessage());
        }
    }

    /* NOTE: keep package-private so TransferCommand can call via saveAsync(plot) instead */
    synchronized void savePlot(Plot plot) {
        String path = plot.getWorldName() + "." + key(plot.getX(), plot.getZ());
        yaml.set(path, null);
        yaml.createSection(path, plot.serialize());
        try {
            yaml.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("[ProShield] Failed to save claim: " + ex.getMessage());
        }
    }

    /* ---------------------- Maintenance ---------------------- */

    public void reloadFromConfig() {
        // Placeholder: flags are per-plot; nothing to reload globally yet.
        plugin.getLogger().info("[ProShield] PlotManager reloaded.");
    }

    public int purgeExpired(int daysOld, boolean unowned) {
        int purged = 0;
        long cutoff = (daysOld > 0) ? (System.currentTimeMillis() - daysOld * 86_400_000L) : 0L;

        for (Map<String, Plot> perWorld : claims.values()) {
            Iterator<Plot> it = perWorld.values().iterator();
            while (it.hasNext()) {
                Plot p = it.next();
                boolean remove = false;
                if (unowned && p.getOwner() == null) remove = true;
                if (cutoff > 0 && p.getCreated() < cutoff) remove = true;

                if (remove) {
                    it.remove();
                    purged++;
                }
            }
        }
        if (purged > 0) saveAsync();
        return purged;
    }

    /* ---------------------- Helpers ---------------------- */

    private String key(int x, int z) {
        return x + "," + z;
    }
}
