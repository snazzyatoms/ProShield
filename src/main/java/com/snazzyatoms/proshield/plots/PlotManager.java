// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotManager {
    private final ProShield plugin;

    // Store plots by chunk-keyed UUID
    private final Map<UUID, Plot> plots = new HashMap<>();
    private final Map<String, UUID> playerNames = new HashMap<>();

    // Track expanded radius per player
    private final Map<UUID, Integer> claimRadii = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    public Plot getPlot(Location loc) {
        if (loc == null) return null;
        Chunk chunk = loc.getChunk();
        UUID id = new UUID(chunk.getX(), chunk.getZ());
        return plots.get(id);
    }

    public Plot getPlot(UUID id) {
        return plots.get(id);
    }

    public void createPlot(Player owner, Location loc) {
        Chunk chunk = loc.getChunk();
        UUID id = new UUID(chunk.getX(), chunk.getZ());
        Plot plot = new Plot(id, owner.getUniqueId());

        plots.put(id, plot);
        playerNames.put(owner.getName(), owner.getUniqueId());

        // Assign default radius
        int defaultRadius = plugin.getConfig().getInt("claims.default-radius", 50);
        claimRadii.put(owner.getUniqueId(), defaultRadius);
    }

    public void removePlot(Location loc) {
        Chunk chunk = loc.getChunk();
        UUID id = new UUID(chunk.getX(), chunk.getZ());
        plots.remove(id);
    }

    // ========================
    // Expansion handling
    // ========================
    public void expandClaim(UUID playerId, int extraRadius) {
        int current = claimRadii.getOrDefault(playerId, plugin.getConfig().getInt("claims.default-radius", 50));
        claimRadii.put(playerId, current + extraRadius);
    }

    public int getClaimRadius(UUID playerId) {
        return claimRadii.getOrDefault(playerId, plugin.getConfig().getInt("claims.default-radius", 50));
    }

    // ========================
    // Save/load stubs
    // ========================
    public void saveAll() {
        // TODO: persist plots + radii to disk
    }

    public String getPlayerName(UUID id) {
        for (Map.Entry<String, UUID> entry : playerNames.entrySet()) {
            if (entry.getValue().equals(id)) return entry.getKey();
        }
        return "Unknown";
    }
}
