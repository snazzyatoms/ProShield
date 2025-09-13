package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * PlotManager
 *
 * ✅ Updated for v1.2.5:
 * - Stores plots by a stable key: world:chunkX:chunkZ
 * - Uses new Plot constructor with worldName, chunkX, chunkZ
 */
public class PlotManager {
    private final ProShield plugin;

    // Use string key instead of UUID to ensure uniqueness across worlds
    private final Map<String, Plot> plots = new HashMap<>();
    private final Map<String, UUID> playerNames = new HashMap<>();

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
    }

    private String makeKey(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }

    public Plot getPlot(Location loc) {
        if (loc == null) return null;
        Chunk chunk = loc.getChunk();
        return plots.get(makeKey(loc.getWorld().getName(), chunk.getX(), chunk.getZ()));
    }

    public Plot getPlot(String world, int x, int z) {
        return plots.get(makeKey(world, x, z));
    }

    public void createPlot(Player owner, Location loc) {
        Chunk chunk = loc.getChunk();
        String key = makeKey(loc.getWorld().getName(), chunk.getX(), chunk.getZ());

        Plot plot = new Plot(
                UUID.randomUUID(),                // unique ID
                owner.getUniqueId(),              // owner
                loc.getWorld().getName(),         // world name
                chunk.getX(),
                chunk.getZ()
        );

        plots.put(key, plot);
        playerNames.put(owner.getName(), owner.getUniqueId());
    }

    public void removePlot(Location loc) {
        Chunk chunk = loc.getChunk();
        plots.remove(makeKey(loc.getWorld().getName(), chunk.getX(), chunk.getZ()));
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
}
