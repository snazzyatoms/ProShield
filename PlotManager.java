package com.proshield.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    // Stores player UUID -> their plot
    private final Map<UUID, Plot> plots = new HashMap<>();

    /**
     * Claim a new plot for a player
     *
     * @param player player claiming the plot
     * @param center center location of the plot
     * @param radius radius of the plot
     */
    public void claimPlot(Player player, Location center, int radius) {
        plots.put(player.getUniqueId(), new Plot(center, radius));
    }

    /**
     * Check if a player is inside their own plot
     *
     * @param player   player to check
     * @param location location being tested
     * @return true if inside own plot, false otherwise
     */
    public boolean isInsideOwnPlot(Player player, Location location) {
        Plot plot = plots.get(player.getUniqueId());
        if (plot == null) return false;
        return plot.isInside(location);
    }

    /**
     * Get a player’s plot
     *
     * @param player the player
     * @return their plot, or null if none
     */
    public Plot getP
