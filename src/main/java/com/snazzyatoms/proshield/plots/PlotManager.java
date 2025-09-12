package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.roles.ClaimRole;
import com.snazzyatoms.proshield.roles.ClaimRoleManager;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Plot> plots = new ConcurrentHashMap<>();
    private final ClaimRoleManager roleManager;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.roleManager = new ClaimRoleManager(this);
    }

    public Plot getPlot(Chunk chunk) {
        return plots.get(getChunkKey(chunk));
    }

    public Plot getPlot(Location loc) {
        return getPlot(loc.getChunk());
    }

    public Plot getPlotById(UUID id) {
        return plots.get(id);
    }

    public void addPlot(Plot plot) {
        plots.put(plot.getId(), plot);
    }

    public void removePlot(Plot plot) {
        plots.remove(plot.getId());
    }

    public void saveAsync(Plot plot) {
        // TODO: implement persistence logic
    }

    private UUID getChunkKey(Chunk chunk) {
        return new UUID(chunk.getX(), chunk.getZ());
    }

    public boolean isTrustedOrOwner(UUID playerId, Location loc) {
        Plot plot = getPlot(loc);
        if (plot == null) return true;
        if (plot.isOwner(playerId)) return true;
        ClaimRole role = plot.getTrusted().get(playerId);
        return role != null && role != ClaimRole.VISITOR;
    }
}
