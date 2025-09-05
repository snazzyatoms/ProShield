package com.proshield.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final Map<UUID, Plot> plots = new HashMap<>();

    /**
     * Attempts to claim a plot for the player.
     * If the player already owns a plot, it will overwrite it.
     *
     * @param player Player claiming the plot
     * @param center Center location of the plot
     * @param radius Radius of the plot
     */
    public void claimPlot(Player player, Location center, int radius) {
        plots.put(player.getUniqueId(), new Plot(center, radius));
    }

    /**
     * Retrieves the plot owned by a player.
     *
     * @param player Player
     * @return Plot instance, or null if none
     */
    public Plot getPlot(Player player) {
        return plots.get(player.getUniqueId());
    }

    /**
     * Checks whether the player is inside their own plot.
     *
     * @param player Player
     * @param loc Location to check
     * @return true if inside their own plot
     */
    public boolean isInsideOwnPlot(Player player, Location loc) {
        Plot plot = getPlot(player);
        if (plot == null) return false;

        Location center = plot.getCenter();

        return loc.getWorld().equals(center.getWorld()) &&
                loc.distance(center) <= plot.getRadius();
    }

    /**
     * Checks if a location is inside any claimed plot.
     *
     * @param loc Location
     * @return true if location is inside any plot
     */
    public boolean isInsideAnyPlot(Location loc) {
        for (Plot plot : plots.values()) {
            if (plot.getCenter().getWorld().equals(loc.getWorld()) &&
                    plot.getCenter().distance(loc) <= plot.getRadius()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Internal plot data structure.
     */
    public static class Plot {
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
