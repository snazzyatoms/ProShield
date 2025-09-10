package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all plots and integrates with global + per-claim settings.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<Chunk, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public Plot getPlot(Chunk chunk) {
        return plots.get(chunk);
    }

    public Plot createPlot(Chunk chunk, UUID owner) {
        Plot plot = new Plot(owner, chunk);
        plots.put(chunk, plot);
        return plot;
    }

    public void removePlot(Chunk chunk) {
        plots.remove(chunk);
    }

    public boolean isClaimed(Chunk chunk) {
        return plots.containsKey(chunk);
    }

    // === Global + per-claim config lookups ===

    /**
     * Whether PvP is allowed in this chunk.
     */
    public boolean isPvpAllowed(Chunk chunk) {
        FileConfiguration config = plugin.getConfig();
        boolean global = config.getBoolean("protection.pvp-in-claims", false);

        Plot plot = getPlot(chunk);
        if (plot != null) {
            return plot.getSettings().isPvpEnabled();
        }
        return global;
    }

    /**
     * Whether explosions are allowed in this chunk.
     */
    public boolean isExplosionsAllowed(Chunk chunk) {
        FileConfiguration config = plugin.getConfig();
        boolean global = config.getBoolean("protection.explosions.enabled", true);

        Plot plot = getPlot(chunk);
        if (plot != null) {
            return plot.getSettings().isExplosionsEnabled();
        }
        return global;
    }

    /**
     * Whether fire spread is allowed in this chunk.
     */
    public boolean isFireSpreadAllowed(Chunk chunk) {
        FileConfiguration config = plugin.getConfig();
        boolean global = config.getBoolean("protection.fire.spread", true);

        Plot plot = getPlot(chunk);
        if (plot != null) {
            return plot.getSettings().isFireSpreadEnabled();
        }
        return global;
    }

    /**
     * Whether item drops should be kept inside claims (global + per-claim).
     */
    public boolean isKeepItemsEnabled(Chunk chunk) {
        FileConfiguration config = plugin.getConfig();
        boolean global = config.getBoolean("claims.keep-items.enabled", false);

        Plot plot = getPlot(chunk);
        if (plot != null) {
            return plot.getSettings().isKeepItemsEnabled();
        }
        return global;
    }

    /**
     * Whether item protection is enabled in this chunk.
     */
    public boolean isItemProtectionEnabled(Chunk chunk) {
        FileConfiguration config = plugin.getConfig();
        boolean global = config.getBoolean("protection.items.enabled", true);

        Plot plot = getPlot(chunk);
        if (plot != null) {
            return plot.getSettings().isItemProtectionEnabled();
        }
        return global;
    }

    /**
     * Expose plots for iteration (e.g., saving).
     */
    public Map<Chunk, Plot> getAllPlots() {
        return plots;
    }
}
