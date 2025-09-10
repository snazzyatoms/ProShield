package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles all plots (claims) for ProShield.
 * - Preserves old logic (trust, roles, expiry, save/load)
 * - Extended with per-claim flags (PvP, explosions, fire, containers, redstone, animals, keep-items)
 * - Syncs with config.yml defaults
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new ConcurrentHashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadPlots();
    }

    /**
     * Get a plot by chunk.
     */
    public Plot getPlot(Chunk chunk) {
        return plots.get(toKey(chunk));
    }

    /**
     * Create a plot for a player.
     */
    public Plot createPlot(UUID owner, Chunk chunk) {
        String key = toKey(chunk);
        if (plots.containsKey(key)) return plots.get(key);

        Plot plot = new Plot(owner, chunk);
        // Apply global defaults from config
        ConfigurationSection globalFlags = plugin.getConfig().getConfigurationSection("claims.flags");
        if (globalFlags != null) {
            plot.getSettings().loadFromConfig(null, globalFlags);
        }
        plots.put(key, plot);
        savePlots();
        return plot;
    }

    /**
     * Remove a plot (unclaim).
     */
    public void removePlot(Chunk chunk) {
        plots.remove(toKey(chunk));
        savePlots();
    }

    /**
     * Returns all plots owned by a player.
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
     * Save plots to config.yml
     */
    public void savePlots() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection claims = config.createSection("claims.data");

        for (Map.Entry<String, Plot> entry : plots.entrySet()) {
            String key = entry.getKey();
            Plot plot = entry.getValue();
            ConfigurationSection section = claims.createSection(key);

            section.set("owner", plot.getOwner().toString());

            // === Save plot settings ===
            ConfigurationSection settingsSection = section.createSection("settings");
            plot.getSettings().saveToConfig(settingsSection);

            // === Save trusted players ===
            List<String> trusted = new ArrayList<>();
            for (UUID uuid : plot.getTrustedPlayers()) {
                trusted.add(uuid.toString());
            }
            section.set("trusted", trusted);
        }

        plugin.saveConfig();
    }

    /**
     * Load plots from config.yml
     */
    public void loadPlots() {
        plots.clear();
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection claims = config.getConfigurationSection("claims.data");
        ConfigurationSection globalFlags = config.getConfigurationSection("claims.flags");

        if (claims == null) return;

        for (String key : claims.getKeys(false)) {
            ConfigurationSection section = claims.getConfigurationSection(key);
            if (section == null) continue;

            String ownerStr = section.getString("owner");
            if (ownerStr == null) continue;

            UUID owner = UUID.fromString(ownerStr);
            Chunk chunk = fromKey(key);
            if (chunk == null) continue;

            Plot plot = new Plot(owner, chunk);

            // === Load settings (with global fallback) ===
            ConfigurationSection settingsSection = section.getConfigurationSection("settings");
            plot.getSettings().loadFromConfig(settingsSection, globalFlags);

            // === Load trusted players ===
            List<String> trustedList = section.getStringList("trusted");
            for (String trusted : trustedList) {
                try {
                    plot.addTrusted(UUID.fromString(trusted));
                } catch (IllegalArgumentException ignored) {
                }
            }

            plots.put(key, plot);
        }
    }

    /**
     * Converts a chunk to a storage key.
     */
    private String toKey(Chunk chunk) {
        return chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
    }

    /**
     * Converts a storage key back into a chunk.
     */
    private Chunk fromKey(String key) {
        try {
            String[] parts = key.split(";");
            if (parts.length != 3) return null;
            String worldName = parts[0];
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            return Bukkit.getWorld(worldName).getChunkAt(x, z);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse plot key: " + key);
            return null;
        }
    }
}
