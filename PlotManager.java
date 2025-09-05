package com.snazzyatoms.proshield;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages claimed plots for players.
 */
public class PlotManager {

    private final ProShield plugin;

    // Stores player UUID -> Plot data
    private final Map<UUID, Plot> plots = new HashMap<>();

    // Config values
    private final int defaultRadius;
    private final int maxRadius;
    private final int minGap;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;

        // Load settings from config.yml
        this.defaultRadius = plugin.getConfig().getInt("protection.default-radius", 10);
        this.maxRadius = plugin.getConfig().getInt("protection.max-radius", 50);
        this.minGap = plugin.getConfig().getInt("protection.min-gap", 10);
    }

    /**
     * Attempts to claim a plot for a player at their current location.
     */
    public boolean claimPlot(Player player, int radius) {
        if (radius <= 0) {
            radius = defaultRadius;
        }

        if (radius > maxRadius) {
            player.sendMessage("§cThe maximum plot radius allowed is " + maxRadius + ".");
            return false;
        }

        Location center = player.getLocation();

        // Check for overlap with existing plots
        for (Plot existing : plots.values()) {
            if (center.distance(existing.getCenter()) < radius + existing.getRadius() + minGap) {
                player.sendMessage("§cYour plot must be at least " + minGap + " blocks away from other plots.");
                return false;
            }
        }

        // Save new plot
        plots.put(player.getUniqueId(), new Plot(center, radius));
        player.sendMessage("§aPlot claimed successfully with radius " + radius + "!");
        return true;
    }

    /**
     * Check if a player can build at a given location.
     */
    public boolean canBuild(Player player, Location location) {
        Plot plot = plots.get(player.getUniqueId());
        if (plot == null) return false;

        return location.distance(plot.getCenter()) <= plot.getRadius();
    }

    /**
     * Inner class representing a single plot.
     */
    private static class Plot {
        private final Location center;
        private final int radius;

        public Plot(Location center, int radius) {
            this.center = center;
            this.radius = radius;
        }

        public Location getCenter() {
            return center;
        }

        public int getRadius() {
            return radius;
        }
    }
}
