package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all plots (claims) in the server.
 * Handles loading, saving, and runtime cache of plots & settings.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadPlots();
    }

    public ProShield getPlugin() {
        return plugin;
    }

    public Plot getPlot(Chunk chunk) {
        String key = chunkToKey(chunk);
        return plots.get(key);
    }

    public Plot getOrCreatePlot(Chunk chunk, UUID owner) {
        String key = chunkToKey(chunk);
        return plots.computeIfAbsent(key, k -> new Plot(owner, new PlotSettings()));
    }

    public void removePlot(Chunk chunk) {
        String key = chunkToKey(chunk);
        plots.remove(key);
    }

    public Map<String, Plot> getAllPlots() {
        return plots;
    }

    private String chunkToKey(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }

    /**
     * Load plots + settings from config.yml
     */
    public void loadPlots() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection claimsSection = config.getConfigurationSection("claims");

        if (claimsSection == null) {
            plugin.getLogger().info("No claims found in config.");
            return;
        }

        for (String key : claimsSection.getKeys(false)) {
            ConfigurationSection claimSection = claimsSection.getConfigurationSection(key);
            if (claimSection == null) continue;

            String ownerStr = claimSection.getString("owner");
            if (ownerStr == null) continue;

            UUID owner = UUID.fromString(ownerStr);

            // Load per-claim settings safely
            ConfigurationSection settingsSection = claimSection.getConfigurationSection("settings");
            PlotSettings settings = PlotSettings.fromConfig(settingsSection);

            Plot plot = new Plot(owner, settings);
            plot.loadTrusted(claimSection.getConfigurationSection("trusted"));

            plots.put(key, plot);
        }

        plugin.getLogger().info("Loaded " + plots.size() + " plots.");
    }

    /**
     * Save plots + settings to config.yml
     */
    public void savePlots() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection claimsSection = config.createSection("claims");

        for (Map.Entry<String, Plot> entry : plots.entrySet()) {
            String key = entry.getKey();
            Plot plot = entry.getValue();

            ConfigurationSection claimSection = claimsSection.createSection(key);
            claimSection.set("owner", plot.getOwner().toString());

            // Save trusted players
            plot.saveTrusted(claimSection.createSection("trusted"));

            // Save per-claim settings
            ConfigurationSection settingsSection = claimSection.createSection("settings");
            plot.getSettings().saveToConfig(settingsSection);
        }

        plugin.saveConfig();
    }
}
