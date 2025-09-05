package com.yourname.proshield;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final ProShield plugin;

    // Store player plots (UUID -> Plot)
    private final Map<UUID, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to claim a new plot for a player
     */
    public boolean claimPlot(Player player, int requestedRadius) {
        UUID uuid = player.getUniqueId();

        if (plots.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You already own a plot!");
            return false; // Player already has a plot
        }

        // Load radius limits from config
        int maxRadius = plugin.getConfig().getInt("protection.max-radius", 50);
        int minGap = plugin.getConfig().getInt("protection.min-gap", 10);
        int finalRadius = Math.min(requestedRadius, maxRadius);

        Location center = player.getLocation();
        Plot newPlot = new Plot(center, finalRadius);

        // Check for overlap with existing plots (respecting min-gap)
        for (Plot existing : plots.values()) {
            if (newPlot.overlaps(existing, minGap)) {
                player.sendMessage(ChatColor.RED + "This area is too close to another player’s plot! "
                        + "You must leave at least " + minGap + " blocks of space.");
                return false;
            }
        }

        plots.put(uuid, newPlot);

        Bukkit.getLogger().info(ChatColor.GREEN + "[ProShield] " + player.getName()
                + " claimed a plot with radius " + finalRadius + ".");
        player.sendMessage(ChatColor.GREEN + "You successfully claimed a plot with radius " + finalRadius + "!");
        return true;
    }

    /**
     * Check if the player already has a plot
     */
    public boolean hasPlot(Player player) {
        return plots.containsKey(player.getUniqueId());
    }

    /**
     * Get plot info for a player
     */
    public String getPlotInfo(Player player) {
        Plot plot = plots.get(player.getUniqueId());
        if (plot == null) {
            return "No plot claimed.";
        }
        Location center = plot.getCenter();
        return "Center: " + center.getBlockX() + ", " + center.getBlockY() + ", " + center.getBlockZ() +
                " | Radius: " + plot.getRadius();
    }

    /**
     * Check if a location is inside a player’s plot
     */
    public boolean isInsideOwnPlot(Player player, Location loc) {
        Plot plot = plots.get(player.getUniqueId());
        if (plot == null) return false;
        return plot.contains(loc);
    }

    // Inner class representing a plot
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
            return loc.getWorld().equals(center.getWorld()) &&
                    Math.abs(loc.getBlockX() - center.getBlockX()) <= radius &&
                    Math.abs(loc.getBlockZ() - center.getBlockZ()) <= radius;
        }

        public boolean overlaps(Plot other, int minGap) {
            if (!center.getWorld().equals(other.getCenter().getWorld())) {
                return false; // Different worlds can't overlap
            }

            int dx = Math.abs(center.getBlockX() - other.center.getBlockX());
            int dz = Math.abs(center.getBlockZ() - other.center.getBlockZ());

            int distance = Math.max(dx, dz); // Chebyshev distance for square plots
            return distance <= (radius + other.radius + minGap);
        }
    }
}
