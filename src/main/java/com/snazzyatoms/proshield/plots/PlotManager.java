package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();
    private final File plotsFile;
    private FileConfiguration plotsConfig;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.plotsFile = new File(plugin.getDataFolder(), "plots.yml");
        load();
    }

    public void load() {
        if (!plotsFile.exists()) {
            try {
                plotsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.plotsConfig = YamlConfiguration.loadConfiguration(plotsFile);

        plots.clear();

        if (plotsConfig.contains("plots")) {
            for (String key : plotsConfig.getConfigurationSection("plots").getKeys(false)) {
                UUID id = UUID.fromString(key);
                String world = plotsConfig.getString("plots." + key + ".world");
                int x = plotsConfig.getInt("plots." + key + ".x");
                int z = plotsConfig.getInt("plots." + key + ".z");
                UUID owner = UUID.fromString(plotsConfig.getString("plots." + key + ".owner", id.toString()));
                int radius = plotsConfig.getInt("plots." + key + ".radius",
                        plugin.getConfig().getInt("claims.default-radius", 50));

                Plot plot = new Plot(owner, world, x, z, id, radius);

                // Restore flags
                if (plotsConfig.contains("plots." + key + ".flags")) {
                    Map<String, Object> flags = plotsConfig.getConfigurationSection("plots." + key + ".flags").getValues(false);
                    for (Map.Entry<String, Object> entry : flags.entrySet()) {
                        plot.setFlag(entry.getKey(), Boolean.parseBoolean(entry.getValue().toString()));
                    }
                }

                plots.put(id, plot);
            }
        }
    }

    public void saveAll() {
        plotsConfig.set("plots", null); // clear old

        for (Map.Entry<UUID, Plot> entry : plots.entrySet()) {
            Plot plot = entry.getValue();
            String base = "plots." + entry.getKey();

            plotsConfig.set(base + ".world", plot.getWorld());
            plotsConfig.set(base + ".x", plot.getX());
            plotsConfig.set(base + ".z", plot.getZ());
            plotsConfig.set(base + ".owner", plot.getOwner().toString());
            plotsConfig.set(base + ".radius", plot.getRadius());

            // Save flags
            Map<String, Boolean> flags = plot.getFlags();
            for (Map.Entry<String, Boolean> flag : flags.entrySet()) {
                plotsConfig.set(base + ".flags." + flag.getKey(), flag.getValue());
            }
        }

        try {
            plotsConfig.save(plotsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Plot getPlot(UUID id) {
        return plots.get(id);
    }

    public Plot getPlotAt(Location loc) {
        if (loc == null) return null;
        for (Plot plot : plots.values()) {
            if (plot.isInPlot(loc)) {
                return plot;
            }
        }
        return null;
    }

    public Collection<Plot> getPlots() {
        return plots.values();
    }

    public Plot createPlot(UUID owner, Location center) {
        UUID id = UUID.randomUUID();
        String world = center.getWorld().getName();
        int x = center.getBlockX();
        int z = center.getBlockZ();
        int radius = plugin.getConfig().getInt("claims.default-radius", 50);

        Plot plot = new Plot(owner, world, x, z, id, radius);
        plots.put(id, plot);
        saveAll();
        return plot;
    }

    public void deletePlot(UUID id) {
        plots.remove(id);
        plotsConfig.set("plots." + id, null);
        saveAll();
    }

    public void expandPlot(UUID id, int extraRadius) {
        Plot plot = plots.get(id);
        if (plot != null) {
            plot.setRadius(plot.getRadius() + extraRadius);
            saveAll();
        }
    }

    public boolean isInClaim(Location loc, UUID owner) {
        Plot plot = getPlotAt(loc);
        return plot != null && plot.getOwner().equals(owner);
    }

    public World getWorld(String name) {
        return Bukkit.getWorld(name);
    }
}
