package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all plots (claims) and integrates with Plot + PlotSettings.
 * Preserves all existing logic while extended for new flags in v1.2.5.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Load all plots from config.yml into memory.
     */
    public void loadPlots() {
        plots.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("claims");

        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                ConfigurationSection plotSection = section.getConfigurationSection(key);
                if (plotSection == null) continue;

                Plot plot = Plot.deserialize(plugin, plotSection);
                plots.put(key, plot);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load plot: " + key + " (" + e.getMessage() + ")");
            }
        }

        plugin.getLogger().info("Loaded " + plots.size() + " plots.");
    }

    /**
     * Save all plots back to config.yml.
     */
    public void savePlots() {
        FileConfiguration config = plugin.getConfig();
        config.set("claims", null); // clear old data

        for (Map.Entry<String, Plot> entry : plots.entrySet()) {
            String key = entry.getKey();
            Plot plot = entry.getValue();
            config.set("claims." + key, plot.serialize());
        }

        plugin.saveConfig();
    }

    /**
     * Get the unique key for a chunk.
     */
    private String chunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }

    /**
     * Get the plot for a chunk, or null if unclaimed.
     */
    public Plot getPlot(Chunk chunk) {
        return plots.get(chunkKey(chunk));
    }

    /**
     * Claim a chunk for a player.
     */
    public boolean claimPlot(UUID owner, Chunk chunk) {
        String key = chunkKey(chunk);
        if (plots.containsKey(key)) return false; // already claimed

        Plot plot = new Plot(owner, chunk, new PlotSettings(plugin)); // default settings
        plots.put(key, plot);
        savePlots();
        return true;
    }

    /**
     * Unclaim a chunk.
     */
    public boolean unclaimPlot(Chunk chunk) {
        String key = chunkKey(chunk);
        if (!plots.containsKey(key)) return false;

        plots.remove(key);
        savePlots();
        return true;
    }

    /**
     * Transfer ownership of a plot.
     */
    public boolean transferPlot(Chunk chunk, UUID newOwner) {
        Plot plot = getPlot(chunk);
        if (plot == null) return false;

        plot.setOwner(newOwner);
        savePlots();
        return true;
    }

    /**
     * Reload plots from config.yml (safe).
     */
    public void reloadPlots() {
        loadPlots();
    }

    /**
     * Get all plots owned by a player.
     */
    public List<Plot> getPlotsByOwner(UUID owner) {
        List<Plot> owned = new ArrayList<>();
        for (Plot plot : plots.values()) {
            if (plot.getOwner().equals(owner)) {
                owned.add(plot);
            }
        }
        return owned;
    }

    /**
     * Check if a player owns any plots.
     */
    public boolean ownsPlot(UUID owner) {
        for (Plot plot : plots.values()) {
            if (plot.getOwner().equals(owner)) return true;
        }
        return false;
    }

    /**
     * Extend logic: Update plot settings (per-claim flags).
     */
    public void updatePlotSettings(Chunk chunk, PlotSettings settings) {
        Plot plot = getPlot(chunk);
        if (plot != null) {
            plot.setSettings(settings);
            savePlots();
        }
    }

    /**
     * Get all plots for debugging/admin.
     */
    public Collection<Plot> getAllPlots() {
        return plots.values();
    }
}
