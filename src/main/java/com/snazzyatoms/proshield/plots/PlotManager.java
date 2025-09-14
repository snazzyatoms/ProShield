package com.snazzyatoms.proshield.plots;

import org.bukkit.Location;

import java.util.*;

public class PlotManager {

    private final Map<UUID, Plot> plots = new HashMap<>();

    public Plot getPlot(Location loc) {
        if (loc == null) return null;
        for (Plot plot : plots.values()) {
            if (plot.getWorldName().equals(loc.getWorld().getName())) {
                int cx = loc.getChunk().getX();
                int cz = loc.getChunk().getZ();
                if (plot.getX() == cx && plot.getZ() == cz) {
                    return plot;
                }
            }
        }
        return null;
    }

    public Plot getPlotById(UUID id) {
        return plots.get(id);
    }

    public void addPlot(Plot plot) {
        plots.put(plot.getId(), plot);
    }

    public void removePlot(UUID id) {
        plots.remove(id);
    }

    public Collection<Plot> getAllPlots() {
        return plots.values();
    }
}
