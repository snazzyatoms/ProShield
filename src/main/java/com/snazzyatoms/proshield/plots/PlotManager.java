// src/main/java/com/snazzyatoms/proshield/plots/PlotManager.java
package com.snazzyatoms.proshield.plots;

import com.snazzyatoms.proshield.ProShield;
import com.snazzyatoms.proshield.expansion.ExpansionRequest;
import com.snazzyatoms.proshield.expansion.ExpansionQueue;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotManager {

    private final ProShield plugin;
    private final Map<UUID, Plot> plots = new HashMap<>();
    private final Map<String, UUID> playerNames = new HashMap<>();
    private final Map<UUID, Integer> claimRadii = new HashMap<>(); // ✅ per-plot radius

    private final int defaultRadius;
    private final boolean instantApply;

    public PlotManager(ProShield plugin) {
        this.plugin = plugin;
        this.defaultRadius = plugin.getConfig().getInt("claims.default-radius", 50);
        this.instantApply = plugin.getConfig().getBoolean("claims.instant-apply", true);
    }

    /**
     * Get claim radius for a plot (default or expanded).
     */
    public int getClaimRadius(UUID plotId) {
        return claimRadii.getOrDefault(plotId, defaultRadius);
    }

    /**
     * Create a new claim.
     */
    public Plot createPlot(Player owner, Location loc) {
        if (loc == null) return null;

        Chunk chunk = loc.getChunk();
        UUID id = new UUID(chunk.getX(), chunk.getZ());
        Plot plot = new Plot(id, owner.getUniqueId());

        plots.put(id, plot);
        claimRadii.put(id, defaultRadius);
        playerNames.put(owner.getName(), owner.getUniqueId());

        return plot;
    }

    /**
     * Remove a claim at a location.
     */
    public void removePlot(Location loc) {
        if (loc == null) return;
        Chunk chunk = loc.getChunk();
        UUID id = new UUID(chunk.getX(), chunk.getZ());

        plots.remove(id);
        claimRadii.remove(id);
    }

    /**
     * Approve an expansion request.
     */
    public void approveExpansion(ExpansionRequest request) {
        for (Plot plot : plots.values()) {
            if (plot.getOwner().equals(request.getPlayerId())) {
                int newRadius = getClaimRadius(plot.getId()) + request.getExtraRadius();

                if (instantApply) {
                    claimRadii.put(plot.getId(), newRadius);
                    plugin.getMessagesUtil().broadcastToAdmins("&a[ProShield] Expansion approved for " +
                            plugin.getServer().getOfflinePlayer(request.getPlayerId()).getName() +
                            " → New radius: " + newRadius + " blocks");
                } else {
                    // Store as pending until restart
                    ExpansionQueue.markApproved(request);
                }
                break;
            }
        }
    }

    /**
     * Get a claim at a location.
     */
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
     * Save claims to disk (future feature).
     */
    public void saveAll() {
        // TODO: persist to disk (future implementation)
    }

    public String getPlayerName(UUID id) {
        for (Map.Entry<String, UUID> entry : playerNames.entrySet()) {
            if (entry.getValue().equals(id)) return entry.getKey();
        }
        return "Unknown";
    }
}
