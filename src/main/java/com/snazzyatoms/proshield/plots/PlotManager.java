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

    private String key(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + "," + loc.getChunk().getX() + "," + loc.getChunk().getZ();
    }

    public Plot getPlot(Chunk chunk) {
        return plots.get(key(chunk));
    }

    public Plot getPlot(Location loc) {
        return plots.get(key(loc));
    }

    public Collection<Plot> getAllPlots() {
        return plots.values();
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
            Plot plot = Plot.deserialize(config.getConfigurationSection(key));
            if (plot != null) {
                plots.put(key, plot);
            }
        }
    }

    public void reloadFromConfig() {
        plots.clear();
        try {
            config.load(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to reload plots.yml: " + e.getMessage());
        }
        loadAll();
    }

    public void saveAll() {
        for (Plot plot : plots.values()) {
            save(plot);
        }
    }

    /* -------------------------------------------------------
     * New: lookup by claim UUID (plot id)
     * ------------------------------------------------------- */
    public Plot getPlotById(UUID id) {
        for (Plot plot : plots.values()) {
            if (id.equals(plot.getId())) {
                return plot;
            }
        }
        return null;
    }
}
