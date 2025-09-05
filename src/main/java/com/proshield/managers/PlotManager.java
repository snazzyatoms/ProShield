package com.proshield.managers;

import com.proshield.ProShield;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;

    // Stores claimed plots in memory: <Owner UUID, Plot>
    private final Map<UUID, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        loadPlots(); // load from config when plugin starts
    }

    /**
     * Attempts to claim a new plot for a player
     */
    public boolean claimPlot(Player player, int radius) {
        UUID uuid = player.getUniqueId();

        if (plots.containsKey(uuid)) {
            player.sendMessage("§cYou already own a plot!");
            return false;
        }

        Location center = player.getLocation();
        Plot plot = new Plot(uuid, center, radius);

        plots.put(uuid, plot);
        savePlot(plot);

        player.sendMessage("§aSuccessfully claimed a plot with radius " + radius + "!");
        return true;
    }

    /**
     * Saves a single plot to config
     */
    private void savePlot(Plot plot) {
        FileConfiguration config = plugin.getConfig();
        String path = "plots." + plot.getOwner().toString();

        config.set(path + ".world", plot.getCenter().getWorld().getName());
        config.set(path + ".x", plot.getCenter().getX());
        config.set(path + ".y", plot.getCenter().getY());
        config.set(path + ".z", plot.getCenter().getZ());
        config.set(path + ".radius", plot.getRadius());

        plugin.saveConfig();
    }

    /**
     * Loads plots from config into memory
     */
    private void loadPlots() {
        FileConfiguration config = plugin.getConfig();
        if (!config.isConfigurationSection("plots")) return;

        for (String key : config.getConfigurationSection("plots").getKeys(false)) {
            UUID uuid = UUID.fromString(key);

            String world = config.getString("plots." + key + ".world");
            double x = config.getDouble("plots." + key + ".x");
            double y = config.getDouble("plots." + key + ".y");
            double z = config.getDouble("plots." + key + ".z");
            int radius = config.getInt("plots." + key + ".radius");

            Location center = new Location(Bukkit.getWorld(world), x, y, z);
            plots.put(uuid, new Plot(uuid, center, radius));
        }
    }

    /**
     * Gets a player’s plot
     */
    public Plot getPlot(UUID uuid) {
        return plots.get(uuid);
    }

    /**
     * Checks if a location is inside a plot
     */
    public boolean isInPlot(Location loc, UUID owner) {
        Plot plot = plots.get(owner);
        if (plot == null) return false;
        return plot.isInside(loc);
    }

    /**
     * Inner class for Plot data
     */
    public static class Plot {
        private final UUID owner;
        private final Location center;
        private final int radius;

        public Plot(UUID owner, Location center, int radius) {
            this.owner = owner;
            this.center = center;
            this.radius = radius;
        }

        public UUID getOwner() { return owner; }
        public Location getCenter() { return center; }
        public int getRadius() { return radius; }

        public boolean isInside(Location loc) {
            if (!loc.getWorld().equals(center.getWorld())) return false;
            return loc.distance(center) <= radius;
        }
    }
}
