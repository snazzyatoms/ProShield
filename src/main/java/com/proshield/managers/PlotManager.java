package com.proshield.managers;

import org.bukkit.Location;
import java.util.HashSet;
import java.util.Set;

public class PlotManager {

    private final Set<Location> protectedPlots = new HashSet<>();

    public boolean isPlotProtected(Location location) {
        return protectedPlots.contains(location);
    }

    public void addProtectedPlot(Location location) {
        protectedPlots.add(location);
    }

    public void removeProtectedPlot(Location location) {
        protectedPlots.remove(location);
    }
}
