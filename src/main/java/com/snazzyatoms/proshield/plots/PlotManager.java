package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles creation, loading, saving, and management of plots (claims).
 * Each plot is bound to a chunk and stores settings + ownership + trust.
 */
public class PlotManager {

    private final ProShield plugin;
    private final Map<String, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadPlots();
    }

    /**
     * Loads all plots and their settings from config.yml
     */
    public void loadPlots() {
        plots.clear();
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection section = config.getConfigurationSection("claims");
        if (section == null) return;

        ConfigurationSection claimsSection = section.getConfigurationSection("data");
        if (claimsSection == null) return;

        for (String key : claimsSection.getKeys(false)) {
            ConfigurationSection plotSec = claimsSection.getConfigurationSection(key);
            if (plotSec == null) continue;

            UUID owner = UUID.fromString(plotSec.getString("owner"));
            Plot plot = new Plot(owner, key);

            // === Load claim-specific settings ===
            PlotSettings settings = plot.getSettings();
            settings.setKeepItemsEnabled(plotSec.getBoolean("settings.keep-items", false));
            settings.setPvpEnabled(plotSec.getBoolean("settings.pvp", false));
            settings.setExplosionsEnabled(plotSec.getBoolean("settings.explosions", false));
            settings.setFireEnabled(plotSec.getBoolean("settings.fire", false));
            settings.setMobGriefEnabled(plotSec.getBoolean("settings.mob-grief", false));

            // Extended flags
            settings.setRedstoneEnabled(plotSec.getBoolean("settings.redstone", true));
            settings.setContainerAccessEnabled(plotSec.getBoolean("settings.container-access", true));
            settings.setAnimalInteractEnabled(plotSec.getBoolean("settings.animal-interact", true));

            plot.setSettings(settings);

            // === Trusted players ===
            ConfigurationSection trustSec = plotSec.getConfigurationSection("trusted");
            if (trustSec != null) {
                for (String uuidStr : trustSec.getKeys(false)) {
                    plot.addTrusted(UUID.fromString(uuidStr), trustSec.getString(uuidStr));
                }
            }

            plots.put(key, plot);
        }
    }

    /**
     * Saves all plots and their settings to config.yml
     */
    public void savePlots() {
        FileConfiguration config = plugin.getConfig();

        // Reset section
        config.set("claims.data", null);

        for (Map.Entry<String, Plot> entry : plots.entrySet()) {
            String key = entry.getKey();
            Plot plot = entry.getValue();

            String basePath = "claims.data." + key;

            config.set(basePath + ".owner", plot.getOwner().toString());

            // === Save settings ===
            PlotSettings settings = plot.getSettings();
            config.set(basePath + ".settings.keep-items", settings.isKeepItemsEnabled());
            config.set(basePath + ".settings.pvp", settings.isPvpEnabled());
            config.set(basePath + ".settings.explosions", settings.isExplosionsEnabled());
            config.set(basePath + ".settings.fire", settings.isFireEnabled());
            config.set(basePath + ".settings.mob-grief", settings.isMobGriefEnabled());

            // Extended flags
            config.set(basePath + ".settings.redstone", settings.isRedstoneEnabled());
            config.set(basePath + ".settings.container-access", settings.isContainerAccessEnabled());
            config.set(basePath + ".settings.animal-interact", settings.isAnimalInteractEnabled());

            // === Trusted players ===
            for (Map.Entry<UUID, String> trustEntry : plot.getTrusted().entrySet()) {
                config.set(basePath + ".trusted." + trustEntry.getKey().toString(), trustEntry.getValue());
            }
        }

        plugin.saveConfig();
    }

    /**
     * Returns a plot by chunk, or null if not claimed.
     */
    public Plot getPlot(Chunk chunk) {
        String key = chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
        return plots.get(key);
    }

    /**
     * Creates a new plot.
     */
    public Plot createPlot(UUID owner, Chunk chunk) {
        String key = chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
        Plot plot = new Plot(owner, key);
        plots.put(key, plot);
        savePlots();
        return plot;
    }

    /**
     * Removes a plot.
     */
    public void removePlot(Chunk chunk) {
        String key = chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
        plots.remove(key);
        savePlots();
    }

    /**
     * Checks if a chunk is already claimed.
     */
    public boolean isClaimed(Chunk chunk) {
        return getPlot(chunk) != null;
    }

    /**
     * Saves everything on shutdown.
     */
    public void shutdown() {
        savePlots();
    }
}
