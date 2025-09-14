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
    private final Map<UUID, Integer> claimRadii = new HashMap<>(); // ✅ track per-player radius

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

        // ✅ set default radius from config
        int defaultRadius = plugin.getConfig().getInt("claims.default-radius", 50);
        claimRadii.put(owner.getUniqueId(), defaultRadius);
    }

    public void removePlot(Location loc) {
        Chunk chunk = loc.getChunk();
        UUID id = new UUID(chunk.getX(), chunk.getZ());
        plots.remove(id);
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

    // ======================================================
    // ✅ Expansion Support
    // ======================================================

    public int getClaimRadius(UUID playerId) {
        return claimRadii.getOrDefault(
            playerId,
            plugin.getConfig().getInt("claims.default-radius", 50)
        );
    }

    public void expandClaim(UUID playerId, int extraRadius) {
        int current = getClaimRadius(playerId);
        int newRadius = current + extraRadius;

        // ✅ Update memory
        claimRadii.put(playerId, newRadius);

        // ✅ Respect instant-apply toggle
        if (plugin.getConfig().getBoolean("claims.instant-apply", true)) {
            plugin.getLogger().info("Expanded claim radius for " + playerId + " to " + newRadius + " blocks instantly.");
        } else {
            plugin.getLogger().info("Expansion queued for " + playerId + " (restart required). New radius: " + newRadius);
        }
    }
}
