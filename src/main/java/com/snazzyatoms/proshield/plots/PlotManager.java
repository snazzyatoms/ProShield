// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotManager {
    private final ProShield plugin;
    private final Map<UUID, Plot> plots = new HashMap<>();
    private final Map<String, UUID> playerNames = new HashMap<>();

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

    /**
     * Create a claim around the player's current chunk.
     * Radius is configurable in config.yml ("claims.default-radius").
     */
    public void createPlot(Player owner, Location loc) {
        Chunk center = loc.getChunk();
        int radius = plugin.getConfig().getInt("claims.default-radius", 3); // default = 3 chunks (~48 blocks)

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Chunk chunk = center.getWorld().getChunkAt(center.getX() + dx, center.getZ() + dz);
                UUID id = new UUID(chunk.getX(), chunk.getZ());
                Plot plot = new Plot(id, owner.getUniqueId());
                plots.put(id, plot);
            }
        }
        playerNames.put(owner.getName(), owner.getUniqueId());
    }

    public void removePlot(Location loc) {
        Chunk chunk = loc.getChunk();
        UUID id = new UUID(chunk.getX(), chunk.getZ());
        plots.remove(id);
    }

    public void saveAll() {
        // TODO: persist to disk (future update)
    }

    public String getPlayerName(UUID id) {
        for (Map.Entry<String, UUID> entry : playerNames.entrySet()) {
            if (entry.getValue().equals(id)) return entry.getKey();
        }
        return "Unknown";
    }
}
