package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Handles creation, storage, and lookup of plots (claims).
 * Now supports per-claim settings (PvP, keep-drops, item rules).
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadClaims();
    }

    /**
     * Create a claim at the given chunk.
     */
    public Plot createPlot(Chunk chunk, UUID owner) {
        String key = serializeChunk(chunk);
        Plot plot = new Plot(chunk, owner, plugin);
        plots.put(key, plot);
        saveClaims();
        return plot;
    }

    /**
     * Remove a claim at the given chunk.
     */
    public void removePlot(Chunk chunk) {
        String key = serializeChunk(chunk);
        plots.remove(key);
        saveClaims();
    }

    /**
     * Lookup claim at chunk.
     */
    public Plot getPlot(Chunk chunk) {
        return plots.get(serializeChunk(chunk));
    }

    /**
     * Get all plots owned by a player.
     */
    public List<Plot> getPlots(UUID owner) {
        List<Plot> result = new ArrayList<>();
        for (Plot plot : plots.values()) {
            if (plot.getOwner().equals(owner)) {
                result.add(plot);
            }
        }
        return result;
    }

    /**
     * Check if chunk is claimed.
     */
    public boolean isClaimed(Chunk chunk) {
        return plots.containsKey(serializeChunk(chunk));
    }

    /**
     * Serialize chunk location.
     */
    private String serializeChunk(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + "," + chunk.getZ();
    }

    // ==================================================
    // Persistence
    // ==================================================

    public void saveClaims() {
        FileConfiguration config = plugin.getConfig();
        config.set("claims", null); // clear

        for (Map.Entry<String, Plot> entry : plots.entrySet()) {
            String key = entry.getKey();
            Plot plot = entry.getValue();

            String path = "claims." + key;
            config.set(path + ".owner", plot.getOwner().toString());
            config.set(path + ".trusted", serializeTrusted(plot.getTrusted()));

            // Save per-claim settings
            plot.getSettings().saveToConfig(config, path + ".settings");
        }

        plugin.saveConfig();
    }

    public void loadClaims() {
        plots.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("claims");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String path = "claims." + key;
            String[] parts = key.split(":");
            if (parts.length != 2) continue;

            String world = parts[0];
            String[] coords = parts[1].split(",");
            if (coords.length != 2) continue;

            Chunk chunk = Bukkit.getWorld(world).getChunkAt(
                    Integer.parseInt(coords[0]),
                    Integer.parseInt(coords[1])
            );
            UUID owner = UUID.fromString(config.getString(path + ".owner"));

            Plot plot = new Plot(chunk, owner, plugin);

            // Load trusted players
            List<String> trustedList = config.getStringList(path + ".trusted");
            for (String uuid : trustedList) {
                plot.addTrusted(UUID.fromString(uuid));
            }

            // Load per-claim settings
            plot.getSettings().loadFromConfig(config, path + ".settings");

            plots.put(key, plot);
        }
    }

    private List<String> serializeTrusted(Set<UUID> trusted) {
        List<String> list = new ArrayList<>();
        for (UUID uuid : trusted) {
            list.add(uuid.toString());
        }
        return list;
    }
}
