package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Manages all player plots (claims).
 * Handles storage, lookup, and persistence.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>(); // key = world:x:z

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    private String getKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    public Plot getPlot(Chunk chunk) {
        return plots.get(getKey(chunk));
    }

    public boolean isClaimed(Chunk chunk) {
        return plots.containsKey(getKey(chunk));
    }

    public void addPlot(Plot plot) {
        plots.put(getKey(plot.getChunk()), plot);
    }

    public void removePlot(Chunk chunk) {
        plots.remove(getKey(chunk));
    }

    public Collection<Plot> getAllPlots() {
        return Collections.unmodifiableCollection(plots.values());
    }

    // -------------------------------------------------
    // SAVE / LOAD
    // -------------------------------------------------

    public void saveAll() {
        plugin.getConfig().set("claims", null); // clear old

        ConfigurationSection claimsSec = plugin.getConfig().createSection("claims");

        for (Plot plot : plots.values()) {
            Chunk chunk = plot.getChunk();
            String key = getKey(chunk);
            ConfigurationSection sec = claimsSec.createSection(key);
            plot.save(sec);
        }

        plugin.saveConfig();
    }

    public void loadAll() {
        plots.clear();

        ConfigurationSection claimsSec = plugin.getConfig().getConfigurationSection("claims");
        if (claimsSec == null) return;

        for (String key : claimsSec.getKeys(false)) {
            try {
                String[] parts = key.split(":");
                if (parts.length != 3) continue;

                String worldName = parts[0];
                int x = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("World not found for claim: " + key);
                    continue;
                }

                Chunk chunk = world.getChunkAt(x, z);
                ConfigurationSection sec = claimsSec.getConfigurationSection(key);
                if (sec == null) continue;

                Plot plot = Plot.load(sec, chunk);
                plots.put(key, plot);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load claim: " + key);
                e.printStackTrace();
            }
        }
    }

    // -------------------------------------------------
    // FLAG HELPERS
    // -------------------------------------------------

    /**
     * Get flag for a specific chunk.
     * Returns global default if plot not found.
     */
    public boolean getFlag(Chunk chunk, String flag, boolean globalDefault) {
        Plot plot = getPlot(chunk);
        if (plot == null) return globalDefault;
        return plot.isFlagEnabled(flag);
    }

    /**
     * Set a flag inside a claim.
     */
    public void setFlag(Chunk chunk, String flag, boolean value) {
        Plot plot = getPlot(chunk);
        if (plot == null) return;
        plot.setFlag(flag, value);
        saveAll();
    }
}
