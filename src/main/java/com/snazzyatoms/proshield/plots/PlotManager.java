// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotManager {
    private final ProShield plugin;
    private final Map<UUID, Plot> plots = new HashMap<>();
    private final Map<String, UUID> playerNames = new HashMap<>();

    private static final int CLAIM_RADIUS = 50; // ✅ default radius in blocks

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public Plot getPlot(Location loc) {
        if (loc == null) return null;

        for (Plot plot : plots.values()) {
            Location center = plot.getCenter();
            if (center != null && center.getWorld().equals(loc.getWorld())) {
                double dx = loc.getX() - center.getX();
                double dz = loc.getZ() - center.getZ();
                if (Math.sqrt(dx * dx + dz * dz) <= CLAIM_RADIUS) {
                    return plot;
                }
            }
        }
        return null;
    }

    public Plot getPlot(UUID id) {
        return plots.get(id);
    }

    public void createPlot(Player owner, Location loc) {
        UUID id = UUID.randomUUID();
        Plot plot = new Plot(id, owner.getUniqueId(), loc); // ✅ new Plot with center location
        plots.put(id, plot);
        playerNames.put(owner.getName(), owner.getUniqueId());
    }

    public void removePlot(Location loc) {
        Plot plot = getPlot(loc);
        if (plot != null) {
            plots.remove(plot.getId());
        }
    }

    public void saveAll() {
        // TODO: persist to disk (future feature)
    }

    public String getPlayerName(UUID id) {
        for (Map.Entry<String, UUID> entry : playerNames.entrySet()) {
            if (entry.getValue().equals(id)) return entry.getKey();
        }
        return "Unknown";
    }

    public static int getClaimRadius() {
        return CLAIM_RADIUS;
    }
}
