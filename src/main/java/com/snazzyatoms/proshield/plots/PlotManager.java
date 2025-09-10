package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles storage, retrieval, and management of plots (claims).
 * - Preserves all existing claim logic
 * - Extended with support for per-claim settings (PvP, keep items, explosions, fire, mob grief)
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadPlots();
    }

    /**
     * Build a unique ID for a chunk.
     */
    private String chunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }

    /**
     * Get a plot by chunk.
     */
    public Plot getPlot(Chunk chunk) {
        return plots.get(chunkKey(chunk));
    }

    /**
     * Claim a chunk for a player.
     */
    public Plot claimChunk(Chunk chunk, UUID owner) {
        String key = chunkKey(chunk);
        Plot plot = new Plot(owner, chunk);
        plots.put(key, plot);
        savePlots();
        return plot;
    }

    /**
     * Unclaim a chunk.
     */
    public void unclaimChunk(Chunk chunk) {
        plots.remove(chunkKey(chunk));
        savePlots();
    }

    /**
     * Load plots from config.yml
     */
    public void loadPlots() {
        plots.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("claims");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection claimSec = section.getConfigurationSection(key);
            if (claimSec == null) continue;

            UUID owner = UUID.fromString(claimSec.getString("owner", ""));
            String[] parts = key.split(",");
            if (parts.length < 3) continue;

            String world = parts[0];
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            Plot plot = new Plot(owner, world, x, z);

            // Load old fields if they exist (trust etc.)
            plot.loadFromConfig(claimSec);

            // Extended: load new settings safely with defaults
            PlotSettings settings = plot.getSettings();
            settings.setKeepItemsEnabled(claimSec.getBoolean("settings.keep-items", false));
            settings.setPvpEnabled(claimSec.getBoolean("settings.pvp", false));
            settings.setExplosionsEnabled(claimSec.getBoolean("settings.explosions", false));
            settings.setFireEnabled(claimSec.getBoolean("settings.fire", false));
            settings.setMobGriefEnabled(claimSec.getBoolean("settings.mob-grief", false));

            plots.put(key, plot);
        }
    }

    /**
     * Save plots to config.yml
     */
    public void savePlots() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.createSection("claims");

        for (Map.Entry<String, Plot> entry : plots.entrySet()) {
            String key = entry.getKey();
            Plot plot = entry.getValue();

            ConfigurationSection claimSec = section.createSection(key);
            claimSec.set("owner", plot.getOwner().toString());

            // Save trust, role, or other old metadata
            plot.saveToConfig(claimSec);

            // Save new settings
            PlotSettings settings = plot.getSettings();
            claimSec.set("settings.keep-items", settings.isKeepItemsEnabled());
            claimSec.set("settings.pvp", settings.isPvpEnabled());
            claimSec.set("settings.explosions", settings.isExplosionsEnabled());
            claimSec.set("settings.fire", settings.isFireEnabled());
            claimSec.set("settings.mob-grief", settings.isMobGriefEnabled());
        }

        plugin.saveConfig();
    }

    public Map<String, Plot> getPlots() {
        return plots;
    }
}
