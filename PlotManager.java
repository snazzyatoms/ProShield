package com.proshield.managers;

import com.proshield.ProShield;
import org.bukkit.Bukkit;
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
            if (arePlotsTooClose(center, radius, existing)) {
                player.sendMessage("§cYou must claim at least " + minGap + " blocks away from other plots.");
                return false;
            }
        }

        // Store the new plot
        plots.put(player.getUniqueId(), new Plot(center, radius));
        player.sendMessage("§aSuccessfully claimed a plot with radius " + radius + ".");
        return true;
    }

    /**
     * Checks if the player is inside their own claimed plot.
     */
    public boolean isInsideOwnPlot(Player player, Location loc) {
        Plot plot = plots.get(player.getUniqueId());
        return plot != null && plot.contains(loc);
    }

    /**
     * Checks if two plots are overlapping or too close.
     */
    private boolean arePlotsTooClose(Location center, int radius, Plot other) {
        double distance = center.distance(other.getCenter());
        int minAllowed = radius + other.getRadius() + minGap;
        return distance < minAllowed;
    }

    /**
     * Inner class representing a plot.
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

        public boolean contains(Location loc) {
            return center.getWorld().equals(loc.getWorld()) &&
                   center.distance(loc) <= radius;
        }
    }
}
