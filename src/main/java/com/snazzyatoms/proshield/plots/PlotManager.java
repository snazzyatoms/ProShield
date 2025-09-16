package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Plot> plots = new HashMap<>();
    private final File plotsFile;
    private FileConfiguration plotsConfig;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotsFile = new File(plugin.getDataFolder(), "plots.yml");
        loadAll();
    }

    public void loadAll() {
        if (!plotsFile.exists()) {
            try {
                plotsFile.getParentFile().mkdirs();
                plotsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create plots.yml", e);
            }
        }

        this.plotsConfig = YamlConfiguration.loadConfiguration(plotsFile);
        plots.clear();

        if (plotsConfig.isConfigurationSection("plots")) {
            for (String key : plotsConfig.getConfigurationSection("plots").getKeys(false)) {
                try {
                    ConfigurationSection section = plotsConfig.getConfigurationSection("plots." + key);
                    if (section == null) continue;

                    UUID id = UUID.fromString(key);
                    UUID owner = UUID.fromString(section.getString("owner"));
                    String world = section.getString("world");
                    int x = section.getInt("x");
                    int z = section.getInt("z");
                    int radius = section.getInt("radius", plugin.getConfig().getInt("claims.default-radius", 50));

                    // Safely convert flags
                    Map<String, Object> rawFlags = section.getConfigurationSection("flags") != null
                            ? section.getConfigurationSection("flags").getValues(false)
                            : new HashMap<>();
                    Map<String, String> flags = new HashMap<>();
                    for (Map.Entry<String, Object> e : rawFlags.entrySet()) {
                        flags.put(e.getKey(), String.valueOf(e.getValue()));
                    }

                    // Create Plot with correct constructor
                    Plot plot = new Plot(owner, world, x, z, id, radius);
                    plot.getFlags().putAll(flags);
                    plots.put(id, plot);

                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load plot " + key, ex);
                }
            }
        }

        plugin.getLogger().info("Loaded " + plots.size() + " plots.");
    }

    public void saveAll() {
        plotsConfig.set("plots", null);
        for (Plot plot : plots.values()) {
            String path = "plots." + plot.getId();
            plotsConfig.set(path + ".owner", plot.getOwner().toString());
            plotsConfig.set(path + ".world", plot.getWorld());
            plotsConfig.set(path + ".x", plot.getX());
            plotsConfig.set(path + ".z", plot.getZ());
            plotsConfig.set(path + ".radius", plot.getRadius());

            // Save flags
            plotsConfig.createSection(path + ".flags", plot.getFlags());
        }

        try {
            plotsConfig.save(plotsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save plots.yml", e);
        }
    }

    public Plot createPlot(UUID owner, Location loc) {
        UUID id = UUID.randomUUID();
        String world = loc.getWorld().getName();
        int x = loc.getChunk().getX();
        int z = loc.getChunk().getZ();
        int radius = plugin.getConfig().getInt("claims.default-radius", 50);

        Plot plot = new Plot(owner, world, x, z, id, radius);
        plots.put(id, plot);
        saveAll();
        return plot;
    }

    public Plot getPlot(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        String world = loc.getWorld().getName();
        int x = loc.getChunk().getX();
        int z = loc.getChunk().getZ();

        for (Plot plot : plots.values()) {
            if (plot.getWorld().equalsIgnoreCase(world)
                    && plot.getX() == x
                    && plot.getZ() == z) {
                return plot;
            }
        }
        return null;
    }

    public Collection<Plot> getAllPlots() {
        return Collections.unmodifiableCollection(plots.values());
    }

    public void removePlot(UUID id) {
        plots.remove(id);
        saveAll();
    }
}
