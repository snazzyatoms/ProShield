// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotManager {
    private final ProShield plugin;

    // Each claim is keyed by its UUID
    private final Map<UUID, Plot> plots = new HashMap<>();
    private final Map<String, UUID> playerNames = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    /**
     * Finds a plot if the location is inside any registered claim.
     */
    public Plot getPlot(Location loc) {
        if (loc == null) return null;

        World world = loc.getWorld();
        if (world == null) return null;

        for (Plot plot : plots.values()) {
            if (isInside(loc, plot, world)) {
                return plot;
            }
        }
        return null;
    }

    public Plot getPlot(UUID id) {
        return plots.get(id);
    }

    /**
     * Creates a claim centered on the player's location with default radius from config.
     */
    public void createPlot(Player owner, Location loc) {
        int radius = plugin.getConfig().getInt("claims.default-radius", 16);

        UUID id = UUID.randomUUID(); // unique claim ID
        Plot plot = new Plot(id, owner.getUniqueId());

        // Store boundaries
        plot.setCenter(loc.getBlockX(), loc.getBlockZ());
        plot.setRadius(radius);

        plots.put(id, plot);
        playerNames.put(owner.getName(), owner.getUniqueId());
    }

    /**
     * Removes a claim if the location is inside it.
     */
    public void removePlot(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        UUID toRemove = null;
        for (Map.Entry<UUID, Plot> entry : plots.entrySet()) {
            if (isInside(loc, entry.getValue(), world)) {
                toRemove = entry.getKey();
                break;
            }
        }
        if (toRemove != null) plots.remove(toRemove);
    }

    /**
     * Save claims to disk (future feature).
     */
    public void saveAll() {
        // TODO: persist to disk
    }

    public String getPlayerName(UUID id) {
        for (Map.Entry<String, UUID> entry : playerNames.entrySet()) {
            if (entry.getValue().equals(id)) return entry.getKey();
        }
        return "Unknown";
    }

    // --- Helpers ---

    private boolean isInside(Location loc, Plot plot, World world) {
        if (plot.getWorld() == null || !plot.getWorld().equals(world.getName())) return false;

        int cx = plot.getCenterX();
        int cz = plot.getCenterZ();
        int r = plot.getRadius();

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        return (x >= cx - r && x <= cx + r) && (z >= cz - r && z <= cz + r);
    }
}
