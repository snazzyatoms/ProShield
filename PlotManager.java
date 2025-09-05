package com.snazzyatoms.proshield;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlotManager {

    private final Set<UUID> protectedPlots;

    public PlotManager() {
        this.protectedPlots = new HashSet<>();
    }

    public void protectPlot(UUID plotId) {
        protectedPlots.add(plotId);
    }

    public void unprotectPlot(UUID plotId) {
        protectedPlots.remove(plotId);
    }

    public boolean isProtected(UUID plotId) {
        return protectedPlots.contains(plotId);
    }
}
