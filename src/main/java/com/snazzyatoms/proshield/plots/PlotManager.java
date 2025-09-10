package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Manages all plots (claims) for ProShield.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Load plots from config.
     */
    public void loadPlots() {
        plots.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection claimsSection = config.getConfigurationSection("claims");

        if (claimsSection == null) return;

        for (String key : claimsSection.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                ConfigurationSection section = claimsSection.getConfigurationSection(key);
                if (section == null) continue;

                Plot plot = Plot.fromConfig(id, section);
                plots.put(id, plot);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load plot: " + key + " (" + ex.getMessage() + ")");
            }
        }

        plugin.getLogger().info("Loaded " + plots.size() + " plots.");
    }

    /**
     * Save plots into config.
     */
    public void savePlots() {
        FileConfiguration config = plugin.getConfig();
        config.set("claims", null); // reset

        ConfigurationSection claimsSection = config.createSection("claims");
        for (Map.Entry<UUID, Plot> entry : plots.entrySet()) {
            ConfigurationSection section = claimsSection.createSection(entry.getKey().toString());
            entry.getValue().save(section);
        }

        plugin.saveConfig();
    }

    /**
     * Register a new plot.
     */
    public Plot createPlot(UUID owner, Chunk chunk) {
        UUID id = UUID.randomUUID();
        Plot plot = new Plot(id, owner, chunk);
        plots.put(id, plot);
        return plot;
    }

    /**
     * Unregister and delete a plot.
     */
    public void removePlot(UUID id) {
        plots.remove(id);
    }

    /**
     * Get a plot by UUID.
     */
    public Plot getPlot(UUID id) {
        return plots.get(id);
    }

    /**
     * Get a plot at a chunk.
     */
    public Plot getPlot(Chunk chunk) {
        for (Plot plot : plots.values()) {
            if (plot.getWorldName().equals(chunk.getWorld().getName())
                    && plot.getChunkX() == chunk.getX()
                    && plot.getChunkZ() == chunk.getZ()) {
                return plot;
            }
        }
        return null;
    }

    /**
     * Get all plots owned by a player.
     */
    public List<Plot> getPlotsByOwner(UUID owner) {
        List<Plot> result = new ArrayList<>();
        for (Plot plot : plots.values()) {
            if (plot.getOwner().equals(owner)) {
                result.add(plot);
            }
        }
        return result;
    }

    /**
     * Check if PvP is enabled for this plot.
     */
    public boolean isPvpEnabled(Plot plot) {
        if (plot == null) return false;
        return plot.getSettings().isPvpEnabled();
    }

    /**
     * Check if keep-items is enabled for this plot.
     */
    public boolean isKeepItemsEnabled(Plot plot) {
        if (plot == null) return false;

        // fallback: if per-claim setting is off, check global config
        boolean globalKeep = plugin.getConfig().getBoolean("claims.keep-items.enabled", false);
        return globalKeep || plot.getSettings().isKeepItemsEnabled();
    }

    /**
     * Utility: get plot at location by world name + chunk coords.
     */
    public Plot getPlot(String worldName, int x, int z) {
        for (Plot plot : plots.values()) {
            if (plot.getWorldName().equals(worldName)
                    && plot.getChunkX() == x
                    && plot.getChunkZ() == z) {
                return plot;
            }
        }
        return null;
    }

    /**
     * Debug utility: dump all plots.
     */
    public void debugDump() {
        plugin.getLogger().info("== ProShield Plots ==");
        for (Plot plot : plots.values()) {
            plugin.getLogger().info(" - " + plot.getOwner() +
                    " @ " + plot.getWorldName() +
                    " (" + plot.getChunkX() + "," + plot.getChunkZ() + ")" +
                    " PvP=" + plot.getSettings().isPvpEnabled() +
                    " KeepItems=" + plot.getSettings().isKeepItemsEnabled());
        }
    }
}
