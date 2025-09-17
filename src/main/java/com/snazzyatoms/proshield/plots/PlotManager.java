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

        if (!plotsConfig.contains("plots")) {
            plugin.getLogger().info("No plots found to load.");
            return;
        }

        for (String key : plotsConfig.getConfigurationSection("plots").getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                String world = plotsConfig.getString("plots." + key + ".world");
                String ownerStr = plotsConfig.getString("plots." + key + ".owner");

                // Skip if missing required data
                if (world == null || ownerStr == null) {
                    plugin.getLogger().warning("Skipping invalid plot entry: " + key);
                    continue;
                }

                int x = plotsConfig.getInt("plots." + key + ".x");
                int z = plotsConfig.getInt("plots." + key + ".z");
                UUID owner = UUID.fromString(ownerStr);
                int radius = plotsConfig.getInt("plots." + key + ".radius",
                        plugin.getConfig().getInt("claims.default-radius", 50));

                Plot plot = new Plot(owner, world, x, z, id, radius);

                // Flags
                if (plotsConfig.contains("plots." + key + ".flags")) {
                    Map<String, Object> flags = plotsConfig.getConfigurationSection("plots." + key + ".flags").getValues(false);
                    for (Map.Entry<String, Object> entry : flags.entrySet()) {
                        plot.setFlag(entry.getKey(), Boolean.parseBoolean(String.valueOf(entry.getValue())));
                    }
                }

                // Trusted
                String base = "plots." + key + ".trusted";
                if (plotsConfig.isList(base)) {
                    List<String> uuids = plotsConfig.getStringList(base);
                    String defRole = plugin.getConfig().getString("roles.default", "member");
                    for (String s : uuids) {
                        try {
                            plot.getTrusted().put(UUID.fromString(s), defRole);
                        } catch (Exception ignored) {}
                    }
                } else if (plotsConfig.isConfigurationSection(base)) {
                    for (String u : plotsConfig.getConfigurationSection(base).getKeys(false)) {
                        String role = plotsConfig.getString(base + "." + u,
                                plugin.getConfig().getString("roles.default", "member"));
                        try {
                            plot.getTrusted().put(UUID.fromString(u), role);
                        } catch (Exception ignored) {}
                    }
                }

                plots.put(id, plot);

            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to load plot: " + key + " (" + ex.getMessage() + ")");
            }
        }
    }

    public void saveAll() {
        plotsConfig.set("plots", null);
        for (Map.Entry<UUID, Plot> entry : plots.entrySet()) {
            Plot plot = entry.getValue();
            String base = "plots." + entry.getKey();

            plotsConfig.set(base + ".world", plot.getWorld());
            plotsConfig.set(base + ".x", plot.getX());
            plotsConfig.set(base + ".z", plot.getZ());
            plotsConfig.set(base + ".owner", plot.getOwner().toString());
            plotsConfig.set(base + ".radius", plot.getRadius());

            for (Map.Entry<String, Boolean> flag : plot.getFlags().entrySet()) {
                plotsConfig.set(base + ".flags." + flag.getKey(), flag.getValue());
            }

            for (Map.Entry<UUID, String> t : plot.getTrusted().entrySet()) {
                plotsConfig.set(base + ".trusted." + t.getKey(), t.getValue());
            }
        }

        try {
            plotsConfig.save(plotsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Plot getPlot(UUID id) { return plots.get(id); }

    public Plot getPlotAt(Location loc) {
        if (loc == null) return null;
        for (Plot plot : plots.values()) {
            if (plot.isInPlot(loc)) return plot;
        }
        return null;
    }

    public Plot getPlot(Location loc) { return getPlotAt(loc); }

    public Collection<Plot> getPlots() { return plots.values(); }

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

    public World getWorld(String name) { return Bukkit.getWorld(name); }
}
