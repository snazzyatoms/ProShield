package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;

public class PlotManager {

    private final ProShield plugin;
    private final ClaimRoleManager roleManager;

    private final Map<UUID, Plot> plots = new HashMap<>();

    public PlotManager(ProShield plugin, ClaimRoleManager roleManager) {
        this.plugin = plugin;
        this.roleManager = roleManager;
    }

    public Plot createPlot(Chunk chunk, UUID owner) {
        Plot plot = new Plot(chunk, owner);
        plots.put(plot.getId(), plot);
        return plot;
    }

    public void removePlot(Plot plot) {
        plots.remove(plot.getId());
    }

    public Plot getPlotById(UUID id) {
        return plots.get(id);
    }

    public Plot getPlotAt(Location loc) {
        for (Plot plot : plots.values()) {
            if (plot.contains(loc)) return plot;
        }
        return null;
    }

    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlotAt(loc);
        if (plot == null) return true; // not claimed
        if (plot.isOwner(playerId)) return true;
        return plot.isTrusted(playerId.toString());
    }

    public Collection<Plot> getAllPlots() {
        return plots.values();
    }

    public void saveAll() {
        // TODO: Hook into file/database save
        plugin.getLogger().info("Saving " + plots.size() + " plots...");
    }
}
