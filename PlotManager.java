package com.proshield.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles storing and checking player plot claims.
 * For now this is an in-memory system (no database yet).
 */
public class PlotManager {

    private final Map<UUID, Plot> plots = new HashMap<>();

    public static class Plot {
        private final UUID owner;
        private final Location center;
        private final int radius;

        public Plot(UUID owner, Location center, int radius) {
            this.owner = owner;
            this.center = center;
            this.radius = radius;
        }

        public UUID getOwner() {
            return owner;
        }

        public Location getCenter() {
            return center;
        }

        public int getRadius() {
            return radius;
        }

        public boolean isInside(Location loc) {
            if (!loc.getWorld().equals(center.getWorld())) return false;
            double dx = loc.getX() - center.getX();
            double dz = loc.getZ() - center.getZ();
            return (dx * dx + dz * dz) <= (radius * radius);
        }
    }

    /**
     * Try to claim a new plot for a player.
     */
    public boolean claimPlot(Player player, int radius) {
        if (plots.containsKey(player.getUniqueId())) {
            return false; // already has a claim
        }
        plots.put(player.getUniqueId(),
                new Plot(player.getUniqueId(), player.getLocation(), radius));
        return true;
    }

    /**
     * Get the player's plot.
     */
    public Plot getPlot(Player player) {
        return plots.get(player.getUniqueId());
    }

    /**
     * Check if a location is protected by someone's claim.
     */
    public boolean isClaimed(Location loc) {
        for (Plot plot : plots.values()) {
            if (plot.isInside(loc)) return true;
        }
        return false;
    }
}
